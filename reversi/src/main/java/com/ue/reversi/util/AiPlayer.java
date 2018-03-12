package com.ue.reversi.util;

import android.graphics.Point;

import com.ue.reversi.bean.MinimaxResult;
import com.ue.reversi.bean.Roles;

import java.util.List;

/**
 * 算法
 */
public class AiPlayer {
    //    private static final int depth[] = new int[]{0, 1, 2, 3, 7, 3, 5, 2, 4};
    private byte[][] tmp;
    private int difficulty;
    private int depth;

    public void setAiLevel(int aiLevel) {
        this.difficulty = aiLevel;
        this.depth = aiLevel;
    }

    public AiPlayer() {
        tmp = new byte[8][8];
    }

    public Point getGoodMove(byte[][] chessBoard, byte chessColor) {
        Util.copyBinaryArray(chessBoard, tmp);
        if (chessColor == Roles.BLACK)
            return max(tmp, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, chessColor, difficulty).move;
        else
            return min(tmp, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, chessColor, difficulty).move;
    }

    private MinimaxResult max(byte[][] chessBoard, int depth, int alpha, int beta, byte chessColor, int difficulty) {
        if (depth == 0) {
            return new MinimaxResult(evaluate(chessBoard, difficulty), null);
        }

        List<Point> legalMovesMe = Rule.getLegalMoves(chessBoard, chessColor);
        if (legalMovesMe.size() == 0) {
            if (Rule.getLegalMoves(chessBoard, (byte) -chessColor).size() == 0) {
                return new MinimaxResult(evaluate(chessBoard, difficulty), null);
            }
            return min(chessBoard, depth, alpha, beta, (byte) -chessColor, difficulty);
        }

        byte[][] tmp = new byte[8][8];
        Util.copyBinaryArray(chessBoard, tmp);
        int best = Integer.MIN_VALUE;
        Point move = null;

        for (int i = 0; i < legalMovesMe.size(); i++) {
            alpha = Math.max(best, alpha);
            if (alpha >= beta) {
                break;
            }
            Rule.move(chessBoard, legalMovesMe.get(i), chessColor);
            int value = min(chessBoard, depth - 1, Math.max(best, alpha), beta, (byte) -chessColor, difficulty).mark;
            if (value > best) {
                best = value;
                move = legalMovesMe.get(i);
            }
            Util.copyBinaryArray(tmp, chessBoard);
        }
        return new MinimaxResult(best, move);
    }

    private MinimaxResult min(byte[][] chessBoard, int depth, int alpha, int beta, byte chessColor, int difficulty) {
        if (depth == 0) {
            return new MinimaxResult(evaluate(chessBoard, difficulty), null);
        }

        List<Point> legalMovesMe = Rule.getLegalMoves(chessBoard, chessColor);
        if (legalMovesMe.size() == 0) {
            if (Rule.getLegalMoves(chessBoard, (byte) -chessColor).size() == 0) {
                return new MinimaxResult(evaluate(chessBoard, difficulty), null);
            }
            return max(chessBoard, depth, alpha, beta, (byte) -chessColor, difficulty);
        }

        byte[][] tmp = new byte[8][8];
        Util.copyBinaryArray(chessBoard, tmp);
        int best = Integer.MAX_VALUE;
        Point move = null;

        for (int i = 0; i < legalMovesMe.size(); i++) {
            beta = Math.min(best, beta);
            if (alpha >= beta) {
                break;
            }
            Rule.move(chessBoard, legalMovesMe.get(i), chessColor);
            int value = max(chessBoard, depth - 1, alpha, Math.min(best, beta), (byte) -chessColor, difficulty).mark;
            if (value < best) {
                best = value;
                move = legalMovesMe.get(i);
            }
            Util.copyBinaryArray(tmp, chessBoard);
        }
        return new MinimaxResult(best, move);
    }

    private int evaluate(byte[][] chessBoard, int difficulty) {
        int whiteEvaluate = 0;
        int blackEvaluate = 0;
        switch (difficulty) {
            case 1:
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (chessBoard[i][j] == Roles.WHITE) {
                            whiteEvaluate += 1;
                        } else if (chessBoard[i][j] == Roles.BLACK) {
                            blackEvaluate += 1;
                        }
                    }
                }
                break;
            case 2:
            case 3:
            case 4:
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if ((i == 0 || i == 7) && (j == 0 || j == 7)) {
                            if (chessBoard[i][j] == Roles.WHITE) {
                                whiteEvaluate += 5;
                            } else if (chessBoard[i][j] == Roles.BLACK) {
                                blackEvaluate += 5;
                            }
                        } else if (i == 0 || i == 7 || j == 0 || j == 7) {
                            if (chessBoard[i][j] == Roles.WHITE) {
                                whiteEvaluate += 2;
                            } else if (chessBoard[i][j] == Roles.BLACK) {
                                blackEvaluate += 2;
                            }
                        } else {
                            if (chessBoard[i][j] == Roles.WHITE) {
                                whiteEvaluate += 1;
                            } else if (chessBoard[i][j] == Roles.BLACK) {
                                blackEvaluate += 1;
                            }
                        }
                    }
                }
                break;
            case 5:
            case 6:
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if ((i == 0 || i == 7) && (j == 0 || j == 7)) {
                            if (chessBoard[i][j] == Roles.WHITE) {
                                whiteEvaluate += 5;
                            } else if (chessBoard[i][j] == Roles.BLACK) {
                                blackEvaluate += 5;
                            }
                        } else if (i == 0 || i == 7 || j == 0 || j == 7) {
                            if (chessBoard[i][j] == Roles.WHITE) {
                                whiteEvaluate += 2;
                            } else if (chessBoard[i][j] == Roles.BLACK) {
                                blackEvaluate += 2;
                            }
                        } else {
                            if (chessBoard[i][j] == Roles.WHITE) {
                                whiteEvaluate += 1;
                            } else if (chessBoard[i][j] == Roles.BLACK) {
                                blackEvaluate += 1;
                            }
                        }
                    }
                }
                blackEvaluate = blackEvaluate * 2 + Rule.getLegalMoves(chessBoard, Roles.BLACK).size();
                whiteEvaluate = whiteEvaluate * 2 + Rule.getLegalMoves(chessBoard, Roles.WHITE).size();
                break;
            case 7:
            case 8:
                /**
                 * 稳定度
                 */
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        int weight[] = new int[]{2, 4, 6, 10, 15};
                        if (chessBoard[i][j] == Roles.WHITE) {
                            whiteEvaluate += weight[getStabilizationDegree(chessBoard, new Point(i, j))];
                        } else if (chessBoard[i][j] == Roles.BLACK) {
                            blackEvaluate += weight[getStabilizationDegree(chessBoard, new Point(i, j))];
                        }
                    }
                }
                /**
                 * 行动力
                 */
                blackEvaluate += Rule.getLegalMoves(chessBoard, Roles.BLACK).size();
                whiteEvaluate += Rule.getLegalMoves(chessBoard, Roles.WHITE).size();
                break;
        }
        return blackEvaluate - whiteEvaluate;
    }

    private int getStabilizationDegree(byte[][] chessBoard, Point move) {
        int chessColor = chessBoard[move.x][move.y];
        int drow[][], dcol[][];
        int row[] = new int[2], col[] = new int[2];
        int degree = 0;

        drow = new int[][]{{0, 0}, {-1, 1}, {-1, 1}, {1, -1}};
        dcol = new int[][]{{-1, 1}, {0, 0}, {-1, 1}, {-1, 1}};

        for (int k = 0; k < 4; k++) {
            row[0] = row[1] = move.x;
            col[0] = col[1] = move.y;
            for (int i = 0; i < 2; i++) {
                while (Rule.isLegal(row[i] + drow[k][i], col[i] + dcol[k][i])
                        && chessBoard[row[i] + drow[k][i]][col[i] + dcol[k][i]] == chessColor) {
                    row[i] += drow[k][i];
                    col[i] += dcol[k][i];
                }
            }
            if (!Rule.isLegal(row[0] + drow[k][0], col[0] + dcol[k][0])
                    || !Rule.isLegal(row[1] + drow[k][1], col[1] + dcol[k][1])) {
                degree += 1;
            } else if (chessBoard[row[0] + drow[k][0]][col[0] + dcol[k][0]] == (-chessColor)
                    && chessBoard[row[1] + drow[k][1]][col[1] + dcol[k][1]] == (-chessColor)) {
                degree += 1;
            }
        }
        return degree;
    }

}
