package com.ue.gobang.ai;

import android.graphics.Point;
import android.util.Log;

import com.ue.gobang.util.Roles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Negamax player, with alpha-beta pruning and further optimisations
 */
public class NegamaxPlayer {
    private final ThreatUtils reducer;
    private final Evaluator evaluator;
    private final Cache<Long, MoveEntry> moveTable;

    private long time;
    private long startTime;

    private int totalNodeCount;
    private int nonLeafCount;
    private int branchesExploredSum;

    private State state;

    public NegamaxPlayer(int aiLevel) {
        Log.e("NegamaxPlayer", "NegamaxPlayer: aiLevel=" + aiLevel);
        this.reducer = new ThreatUtils();
        this.evaluator = Evaluator.getInstance();
        this.time = (long) aiLevel * 1000000000;//在java上1秒的结果android上要8秒
        this.moveTable = new Cache<>(1000000);
    }

    /**
     * Generate a list of sorted and pruned moves for this state. Moves are
     * pruned when they are too far away from existing stones, and also when
     * threats are found which require an immediate response.
     *
     * @param state State to get moves for
     * @return A list of moves, sorted and pruned
     */
    private List<Point> getSortedMoves(State state) {
        // Board is empty, return a move in the middle of the board
        if (state.getMoves() == 0) {
            List<Point> moves = new ArrayList<>();
            moves.add(new Point(Roles.LEN / 2, Roles.LEN / 2));
            return moves;
        }

        int playerIndex = state.currentIndex;
        int opponentIndex = state.currentIndex == Roles.BLACK ? Roles.WHITE : Roles.BLACK;

        HashSet<Point> fours = new HashSet<>();
        HashSet<Point> refutations = new HashSet<>();

        HashSet<Point> opponentFours = new HashSet<>();
        HashSet<Point> opponentThrees = new HashSet<>();

        // Check for threats first and respond to them if they exist
        for (int i = 0; i < Roles.LEN; i++) {
            for (int j = 0; j < Roles.LEN; j++) {
                if (state.board[i][j].index == opponentIndex) {
                    opponentFours.addAll(reducer.getFours(state, state.board[i][j], opponentIndex));
                    opponentThrees.addAll(reducer.getThrees(state, state.board[i][j], opponentIndex));
                } else if (state.board[i][j].index == playerIndex) {
                    fours.addAll(reducer.getFours(state, state.board[i][j], playerIndex));
                    refutations.addAll(reducer.getRefutations(state, state.board[i][j], playerIndex));
                }
            }
        }

        // We have a four on the board, play it
        if (!fours.isEmpty()) {
            return new ArrayList<>(fours);
        }
        // Opponent has a four, defend against it
        if (!opponentFours.isEmpty()) {
            return new ArrayList<>(opponentFours);
        }
        // Opponent has a three, defend against it and add refutation moves
        if (!opponentThrees.isEmpty()) {
            opponentThrees.addAll(refutations);
            return new ArrayList<>(opponentThrees);
        }

        List<ScoredMove> scoredMoves = new ArrayList<>();

        MoveEntry entry = moveTable.get(state.getZobristHash());
        // Grab closest moves
        List<Point> moves = new ArrayList<>();
        for (int i = 0; i < Roles.LEN; i++) {
            for (int j = 0; j < Roles.LEN; j++) {
                // Ignore hash move
                if (entry != null && (i == entry.move.x && j == entry.move.y)) {
                    continue;
                }
                if (state.board[i][j].index == Roles.EMPTY) {
                    if (state.hasAdjacent(i, j, 2)) {
                        int score = evaluator.evaluateField(state, i, j, state.currentIndex);
                        scoredMoves.add(new ScoredMove(new Point(i, j), score));
                    }
                }
            }
        }

        // Sort based on move score
        Collections.sort(scoredMoves);
        for (ScoredMove move : scoredMoves) {
            moves.add(move.move);
        }
        return moves;
    }

    /**
     * Run the negamax algorithm for a node in the game tree.
     *
     * @param state Node to search
     * @param depth Depth to search to
     * @param alpha Alpha bound
     * @param beta  Beta bound
     * @return Score of the node
     * @throws InterruptedException Timeout or interrupted by the user
     */
    private int negamax(State state, int depth, int alpha, int beta) throws InterruptedException {
        totalNodeCount++;
        if (Thread.interrupted() || (System.nanoTime() - startTime) > time) {
            throw new InterruptedException();
        }
        if (state.terminal() != Roles.ING || depth == 0) {
            return evaluator.evaluateState(state, depth);
        }
        nonLeafCount++;

        int value;
        int best = Integer.MIN_VALUE;
        int count = 0;

        Point bestMove = null;

        // Try the move from a previous search
        MoveEntry hashMoveEntry = moveTable.get(state.getZobristHash());
        if (hashMoveEntry != null) {
            count++;
            state.makeMove(hashMoveEntry.move);
            value = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(hashMoveEntry.move);
            if (value > best) {
                bestMove = hashMoveEntry.move;
                best = value;
            }
            if (best > alpha) alpha = best;
            if (best >= beta) return best;
        }

        // No cut-off from hash move, get sorted moves
        List<Point> moves = getSortedMoves(state);

        for (Point move : moves) {
            count++;
            state.makeMove(move);
            value = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(move);
            if (value > best) {
                bestMove = move;
                best = value;
            }
            if (best > alpha) alpha = best;
            if (best >= beta) {
                break;
            }
        }
        branchesExploredSum += count;
        putMoveEntry(state.getZobristHash(), bestMove, depth);
        return best;
    }

    /**
     * Place the best move found from a state into the hash table, replacing
     * an existing entry if the state was searched to a higher depth
     *
     * @param key   Hash key of the state
     * @param move  Point to save
     * @param depth Depth of the search
     */
    private void putMoveEntry(long key, Point move, int depth) {
        MoveEntry moveEntry = moveTable.get(key);
        if (moveEntry == null) {
            moveTable.put(key, new MoveEntry(move, depth));
        } else if (depth > moveEntry.depth) {
            moveTable.put(key, new MoveEntry(move, depth));
        }
    }

    /**
     * Run a depth-limited negamax search on a set of moves, sorting them by
     * score.
     *
     * @param depth Depth to search to
     * @return Original move list sorted by best score first
     */
    private List<Point> searchMoves(State state, List<Point> moves, int depth)
            throws InterruptedException {

        List<ScoredMove> scoredMoves = new ArrayList<>();
        for (Point move : moves) {
            scoredMoves.add(new ScoredMove(move, Integer.MIN_VALUE));
        }

        int alpha = -11000;
        int beta = 11000;
        int best = Integer.MIN_VALUE;

        for (ScoredMove move : scoredMoves) {
            state.makeMove(move.move);
            move.score = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(move.move);
            if (move.score > best) best = move.score;
            if (best > alpha) alpha = best;
            if (best >= beta) break;
        }
        Collections.sort(scoredMoves);
        // TODO: 2017/11/14 正式运行时注释以下一句
//        printSearchInfo(scoredMoves.get(0).move, scoredMoves.get(0).score, depth);

        moves.clear();
        for (ScoredMove move : scoredMoves) moves.add(move.move);
        return moves;
    }

    /**
     * Run negamax for an increasing depth, sorting the moves after every
     * completed search
     *
     * @param startDepth Start depth
     * @param endDepth   Maximum depth
     * @return Best move found
     */
    private Point iterativeDeepening(int startDepth, int endDepth) {
        this.startTime = System.nanoTime();
        List<Point> moves = getSortedMoves(state);
        if (moves.size() == 1) return moves.get(0);
        for (int i = startDepth; i <= endDepth; i++) {
            try {
                moves = searchMoves(state, moves, i);
            } catch (InterruptedException e) {
                break;
            }
        }
        return moves.get(0);
    }

    public Point getMove(List<Point> moves, int currentIndex) {
        // Reset performance counts, clear the hash table
        this.totalNodeCount = 0;
        this.nonLeafCount = 0;
        this.branchesExploredSum = 0;
        moveTable.clear();

        // Create a new internal state object, sync with the game state
        this.state = new State(currentIndex);
        for (Point move : moves) {
            state.makeMove(move);
        }

        // Run a depth increasing search
        Point best = iterativeDeepening(2, 10);
        // TODO: 2017/11/14 正式运行时注释以下一句
//        printPerformanceInfo();
        return best;
    }

    /**
     * Print performance information, including the amount of nodes traversed
     * in the game tree and the nodes traversed per millisecond.
     */
    private void printPerformanceInfo() {
        if (totalNodeCount > 0) {
            long duration = (System.nanoTime() - startTime) / 1000000;
            double nodesPerMs = totalNodeCount / (duration > 0 ? duration : 1);
            double avgBranches = (double) branchesExploredSum / (double) nonLeafCount;

            Log.e("NegamaxPlayer", "Time: {" + duration + "}ms");
            Log.e("NegamaxPlayer", "Nodes: {" + totalNodeCount + "}");
            Log.e("NegamaxPlayer", "Nodes/ms: {" + nodesPerMs + "}");
            Log.e("NegamaxPlayer", String.format(
                    "Branches explored (avg): %.2f ", avgBranches));
        }
    }

    /**
     * Print the result of a search. Includes the best move found, depth
     * searched, and the evaluation score.
     */
    private void printSearchInfo(Point bestMove, int score, int depth) {
        int rowAlgebraic = 15 - bestMove.x;
        char colAlgebraic = (char) ('A' + bestMove.y);
        String moveAlgebraic = new String(Character.toString(colAlgebraic) + rowAlgebraic);

        Log.e("NegamaxPlayer",
                String.format("Depth: %d, Evaluation: %d, "
                        + "Best move: %s", depth, score, moveAlgebraic));
    }

    private class ScoredMove implements Comparable<ScoredMove> {
        public Point move;
        public int score;

        public ScoredMove(Point move, int score) {
            this.move = move;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredMove move) {
            return move.score - this.score;
        }
    }

    private class MoveEntry {
        Point move;
        int depth;

        public MoveEntry(Point move, int depth) {
            this.move = move;
            this.depth = depth;
        }
    }
}