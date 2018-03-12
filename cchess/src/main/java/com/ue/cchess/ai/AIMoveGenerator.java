package com.ue.cchess.ai;

import com.ue.cchess.entity.ChessRecord;
import com.ue.cchess.entity.MoveGenerator;
import com.ue.cchess.util.ChessUtils;

import static com.ue.cchess.entity.ChessID.EMPTY;

// 走发产生器
public class AIMoveGenerator {
    private static int SIDE_BLACK = 0;
    //存放每一层的所有走法
    public ChessRecord[][] moveList;
    // 记录m_MoveList中走法的数量
    public int moveCount;

    private MoveGenerator mMoveGenerator;

    public AIMoveGenerator() {
        moveList = new ChessRecord[8][80];
        mMoveGenerator = new MoveGenerator();
        mMoveGenerator.setMyFirst(true);
    }

    public void reset() {
        moveCount = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 80; j++) {
                moveList[i][j] = null;
            }
        }
    }

    public int generateAllMoves(int[][] qzs, int level, int nSide) {
        moveCount = 0;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (qzs[i][j] == EMPTY) {
                    continue;
                }
                if (nSide == SIDE_BLACK) {
                    //如果产生黑棋走法，跳过红棋
                    if (ChessUtils.isRed(qzs[i][j])) {
                        continue;
                    }
                } else {
                    //如果产生红棋走法，跳过黑棋
                    if (ChessUtils.isBlack(qzs[i][j])) {
                        continue;
                    }
                }
                generateSingleMoves(qzs, i, j, level);
            }
        }
        return moveCount;
    }

    private void generateSingleMoves(int[][] qzs, int fromX, int fromY, int level) {
        mMoveGenerator.showValidMoves(qzs, fromX, fromY);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (qzs[i][j] < 0) {
                    qzs[i][j] = -qzs[i][j];
                    addMove(qzs[fromX][fromY], qzs[i][j], fromX, fromY, i, j, level);
                }
            }
        }
    }

    public int addMove(int chessId, int chessId2, int nFromX, int nFromY, int nToX, int nToY, int nPly) {
        moveList[nPly][moveCount] = new ChessRecord();
        moveList[nPly][moveCount].setFXY(true, chessId, nFromX, nFromY);
        moveList[nPly][moveCount].setFXY(false, chessId2, nToX, nToY);
        moveCount++;
        return moveCount;
    }
}