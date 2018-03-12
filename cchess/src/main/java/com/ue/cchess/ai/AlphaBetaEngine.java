package com.ue.cchess.ai;

import com.ue.cchess.entity.ChessRecord;

import static com.ue.cchess.entity.ChessID.B_GENERAL;
import static com.ue.cchess.entity.ChessID.EMPTY;
import static com.ue.cchess.entity.ChessID.R_GENERAL;

/**
 * Alpha-Beta 搜索算法
 */
public class AlphaBetaEngine {
    // 搜索时用于当前节点棋盘状态的数组
    private int qzs[][] = new int[9][10];
    //记录最佳走法的变量
    private ChessRecord bestMove;
    //走法产生器
    private AIMoveGenerator mAIMoveGenerator;
    //估值函数
    private Evaluation mEvaluation;
    //最大搜索深度
    private int mSearchDepth;
    // 当前搜索的最大搜索深度
    private int mMaxDepth;

    public AlphaBetaEngine(int searchDepth) {
        mAIMoveGenerator = new AIMoveGenerator();
        mEvaluation = new Evaluation();
        mSearchDepth = searchDepth;
    }

    /**
     * 根据传入的走法,改变棋盘
     *
     * @param mov
     * @return
     */
    int makeMove(ChessRecord mov) {
        int chessId = qzs[mov.toX][mov.toY];
        qzs[mov.toX][mov.toY] = qzs[mov.fromX][mov.fromY];
        qzs[mov.fromX][mov.fromY] = EMPTY;
        return chessId;
    }

    /**
     * 根据传入的走法,恢复到上一个棋盘
     *
     * @param mov
     * @param nChessID
     */
    void unmakeMove(ChessRecord mov, int nChessID) {
        qzs[mov.fromX][mov.fromY] = qzs[mov.toX][mov.toY];
        qzs[mov.toX][mov.toY] = nChessID;
    }

    /**
     * 判断游戏是否结束
     * 如未结束，返回0，否则返回极大/极小值
     *
     * @param position
     * @param depth
     * @return
     */
    public int isGameOver(int position[][], int depth) {
        int i, j;
        boolean redLive = false, blackLive = false;

        for (i = 3; i < 6; i++) {
            for (j = 7; j < 10; j++) {
                if (position[i][j] == B_GENERAL)
                    blackLive = true;
                if (position[i][j] == R_GENERAL)
                    redLive = true;
            }
        }

        for (i = 3; i < 6; i++) {
            for (j = 0; j < 3; j++) {
                if (position[i][j] == B_GENERAL)
                    blackLive = true;
                if (position[i][j] == R_GENERAL)
                    redLive = true;
            }
        }

        i = (mMaxDepth - depth + 1) % 2;

        if (!redLive)
            if (i != 0)
                return 19990 + depth;
            else
                return -19990 - depth;
        if (!blackLive)
            if (i != 0)
                return -19990 - depth;
            else
                return 19990 + depth;
        return 0;
    }

    public ChessRecord getBestMove() {
        return bestMove;
    }

    public void searchBestMove(int position[][]) {
        for (int i = 0; i < 9; i++) {  // 保存当前棋盘信息
            for (int j = 0; j < 10; j++) {
                qzs[i][j] = position[i][j];
            }
        }
        mMaxDepth = mSearchDepth; //当前最大深度
        
		/*
         *  调用算法，得到 BestMove
		 *  一个负 很大， 一个 正 很大
		 */
        alphaBeta(mMaxDepth, -20000, 20000);
    }

    /**
     * 黑方负最大 ， 红方正最大
     */
    public int alphaBeta(int depth, int alpha, int beta) {

        int i = isGameOver(qzs, depth);
        if (i != 0)
            return i;

        if (depth <= 0)    //叶子节点取估值
            return mEvaluation.evaluate(qzs, (mMaxDepth - depth) % 2);

        int count = mAIMoveGenerator.generateAllMoves(qzs, depth, (mMaxDepth - depth) % 2);

        for (i = 0; i < count; i++) {

            int type = makeMove(mAIMoveGenerator.moveList[depth][i]);
            int score = -alphaBeta(depth - 1, -beta, -alpha); // 递归
            unmakeMove(mAIMoveGenerator.moveList[depth][i], type);

            if (score > alpha) {
                alpha = score;
                if (depth == mMaxDepth)
                    bestMove = mAIMoveGenerator.moveList[depth][i];
            }
            if (alpha >= beta)
                break;
        }
        return alpha;
    }
}