package com.ue.gobang.ai;

import android.graphics.Point;

import com.ue.gobang.util.Roles;

import java.util.Random;
import java.util.Stack;

/**
 * Internal game state representation for the AI.
 */
public class State {

    /**
     * This object stores a 2D board array representing the status of each
     * field (intersection) on the Gomoku board. A field can either be empty
     * (0), 1/2 (occupied by a player) or 3 which is an out of bounds field,
     * used for detecting that a field is at/near the edge of the board.
     * <p>
     * Board directions are stored in a 4D array. This 4D array maps each field
     * on the board to a set of neighbouring fields, 4 on each side of the
     * stone, forming a star pattern:
     * <p>
     * *       *        *
     * *     *      *
     * *   *    *
     * * *  *
     * * * * * [X] * * * *
     * *  *  *
     * *    *    *
     * *      *      *
     * *        *        *
     * <p>
     * To get the neighbouring fields, we index as follows:
     * [x][y][direction][field #]
     * <p>
     * [0][0-9] -> Diagonal from top left to bottom right
     * [1][0-9] -> Diagonal from top right to bottom left
     * [2][0-9] -> Vertical from top to bottom
     * [3][0-9] -> Horizontal from left to right
     */
    protected final Field[][] board;
    protected final Field[][][][] directions;

    // The current player
    protected int currentIndex;

    // Zobrist hashing, for using the state in a hash data structure
    // https://en.wikipedia.org/wiki/Zobrist_hashing
    private long zobristHash;
    private final long[][][] zobristKeys;

    // Keep track of the moves made on this state
    private Stack<Point> moveStack;

    private Random random;

    /**
     * Create a new state.
     */
    public State(int currentIndex) {
        this.board = new Field[Roles.LEN][Roles.LEN];
        random = new Random(Long.MAX_VALUE);

        for (int i = 0; i < Roles.LEN; i++) {
            for (int j = 0; j < Roles.LEN; j++) {
                board[i][j] = new Field(i, j);
            }
        }
        this.directions = new Field[Roles.LEN][Roles.LEN][4][9];
        this.currentIndex = currentIndex;
        this.zobristKeys = new long[2][Roles.LEN][Roles.LEN];
        this.zobristHash = 0;
        this.moveStack = new Stack<>();
        this.generateDirections(board);

        // Generate Zobrist keys
        for (int i = 0; i < zobristKeys.length; i++) {
            for (int j = 0; j < zobristKeys[0].length; j++) {
                for (int k = 0; k < zobristKeys[0][0].length; k++) {
//                    zobristKeys[i][j][k] = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
                    zobristKeys[i][j][k] = random.nextLong();
                }
            }
        }
    }

    /**
     * Return the Zobrist hash for this state, a unique 64-bit long value
     * representing this state. Updated automatically as moves are made/unmade.
     *
     * @return Hash value (Long)
     */
    public long getZobristHash() {
        return zobristHash;
    }

    /**
     * Apply a move to this state.
     *
     * @param move Move to apply
     */
    public void makeMove(Point move) {
        moveStack.push(move);
        this.board[move.x][move.y].index = currentIndex;
        this.zobristHash ^= zobristKeys[board[move.x][move.y].index - Roles.DIFF][move.x][move.y];
        this.currentIndex = this.currentIndex == Roles.BLACK ? Roles.WHITE : Roles.BLACK;
    }

    /**
     * Undo a move on this state.
     *
     * @param move Point to undo
     */
    public void undoMove(Point move) {
        moveStack.pop();
        this.zobristHash ^= zobristKeys[board[move.x][move.y].index - Roles.DIFF][move.x][move.y];
        this.board[move.x][move.y].index = Roles.EMPTY;
        this.currentIndex = this.currentIndex == Roles.BLACK ? Roles.WHITE : Roles.BLACK;
    }

    /**
     * Return whether or not this field has occupied fields around it, within
     * some given distance. Used to determine if a field on the board is
     * worth evaluating as a possible move.
     *
     * @param row      Field x
     * @param col      Field y
     * @param distance How far to look in each direction, limit 4
     * @return
     */
    protected boolean hasAdjacent(int row, int col, int distance) {
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j <= distance; j++) {
                if (directions[row][col][i][4 + j].index == Roles.BLACK
                        || directions[row][col][i][4 - j].index == Roles.BLACK
                        || directions[row][col][i][4 + j].index == Roles.WHITE
                        || directions[row][col][i][4 - j].index == Roles.WHITE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate the 4D board directions array, by iterating
     * diagonally/horizontally/vertically and storing the references to the
     * neighbouring fields.
     *
     * @param board Field array
     */
    private void generateDirections(Field[][] board) {
        for (int row = 0; row < Roles.LEN; row++) {
            for (int col = 0; col < Roles.LEN; col++) {
                directions[row][col][0][4] = board[row][col];
                directions[row][col][1][4] = board[row][col];
                directions[row][col][2][4] = board[row][col];
                directions[row][col][3][4] = board[row][col];

                for (int k = 0; k < 5; k++) {
                    // Diagonal 1, top left
                    if (row - k >= 0 && col - k >= 0) {
                        directions[row][col][0][4 - k] = board[row - k][col - k];
                    } else {
                        directions[row][col][0][4 - k] = new Field();
                    }

                    // Diagonal 1, bottom right
                    if (row + k < Roles.LEN && col + k < Roles.LEN) {
                        directions[row][col][0][4 + k] = board[row + k][col + k];
                    } else {
                        directions[row][col][0][4 + k] = new Field();
                    }

                    // Diagonal 2, top right
                    if (row - k >= 0 && col + k < Roles.LEN) {
                        directions[row][col][1][4 - k] = board[row - k][col + k];
                    } else {
                        directions[row][col][1][4 - k] = new Field();
                    }

                    // Diagonal 2, bottom left
                    if (row + k < Roles.LEN && col - k >= 0) {
                        directions[row][col][1][4 + k] = board[row + k][col - k];
                    } else {
                        directions[row][col][1][4 + k] = new Field();
                    }

                    // Vertical top
                    if (row - k >= 0) {
                        directions[row][col][2][4 - k] = board[row - k][col];
                    } else {
                        directions[row][col][2][4 - k] = new Field();
                    }

                    // Vertical bottom
                    if (row + k < Roles.LEN) {
                        directions[row][col][2][4 + k] = board[row + k][col];
                    } else {
                        directions[row][col][2][4 + k] = new Field();
                    }

                    // Horizontal left
                    if (col - k >= 0) {
                        directions[row][col][3][4 - k] = board[row][col - k];
                    } else {
                        directions[row][col][3][4 - k] = new Field();
                    }

                    // Horizontal right
                    if (col + k < Roles.LEN) {
                        directions[row][col][3][4 + k] = board[row][col + k];
                    } else {
                        directions[row][col][3][4 + k] = new Field();
                    }
                }
            }
        }
    }

    /**
     * Determine if this state is terminal
     *
     * @return 0 if not terminal, index (1/2) of the player who has won, or 3
     * if the board is full
     */
    protected int terminal() {
        Point move = moveStack.peek();
        int row = move.x;
        int col = move.y;
        int lastIndex = currentIndex == Roles.BLACK ? Roles.WHITE : Roles.BLACK;

        // Check around the last move placed to see if it formed a five
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                if (directions[row][col][i][j].index == lastIndex) {
                    int count = 0;
                    for (int k = 1; k < 5; k++) {
                        if (directions[row][col][i][j + k].index == lastIndex) {
                            count++;
                        } else {
                            break;
                        }
                    }
                    if (count == 4) return lastIndex;
                }
            }
        }
        return moveStack.size() == Roles.LEN * Roles.LEN ? Roles.ERROR : Roles.ING;
    }

    /**
     * Get the total number of moves made on this state
     *
     * @return # of moves
     */
    protected int getMoves() {
        return moveStack.size();
    }

    /**
     * Get a field instance on the board at a given x/y position. For
     * unit testing purposes.
     *
     * @param row Row
     * @param col Column
     * @return Field instance at given position
     */
    public Field getField(int row, int col) {
        return this.board[row][col];
    }

}
