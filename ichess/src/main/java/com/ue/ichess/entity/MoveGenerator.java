package com.ue.ichess.entity;

import com.ue.ichess.util.ChessUtils;

import static com.ue.ichess.entity.ChessID.B_BISHOP;
import static com.ue.ichess.entity.ChessID.B_KING;
import static com.ue.ichess.entity.ChessID.B_KING_N;
import static com.ue.ichess.entity.ChessID.B_KNIGHT;
import static com.ue.ichess.entity.ChessID.B_PAWN;
import static com.ue.ichess.entity.ChessID.B_PAWN_N;
import static com.ue.ichess.entity.ChessID.B_QUEEN;
import static com.ue.ichess.entity.ChessID.B_ROOK;
import static com.ue.ichess.entity.ChessID.B_ROOK_N;
import static com.ue.ichess.entity.ChessID.EMPTY;
import static com.ue.ichess.entity.ChessID.W_BISHOP;
import static com.ue.ichess.entity.ChessID.W_KING;
import static com.ue.ichess.entity.ChessID.W_KING_N;
import static com.ue.ichess.entity.ChessID.W_KNIGHT;
import static com.ue.ichess.entity.ChessID.W_PAWN;
import static com.ue.ichess.entity.ChessID.W_PAWN_N;
import static com.ue.ichess.entity.ChessID.W_QUEEN;
import static com.ue.ichess.entity.ChessID.W_ROOK;
import static com.ue.ichess.entity.ChessID.W_ROOK_N;

/**
 * Created by hawk on 2017/11/6.
 */

public class MoveGenerator {
    private boolean isMyFirst;

    public void setMyFirst(boolean myFirst) {
        isMyFirst = myFirst;
    }

    public void showValidMoves(int[][] qzs, int fromX, int fromY) {
        switch (qzs[fromX][fromY]) {
            case W_PAWN:
            case W_PAWN_N:
            case B_PAWN:
            case B_PAWN_N:
                showPawnMoves(qzs, fromX, fromY);
                break;
            case W_BISHOP:
            case B_BISHOP:
                showBishopMoves(qzs, fromX, fromY);
                break;
            case W_KING:
            case W_KING_N:
            case B_KING:
            case B_KING_N:
                showKingMoves(qzs, fromX, fromY);
                break;
            case W_KNIGHT:
            case B_KNIGHT:
                showKnightMoves(qzs, fromX, fromY);
                break;
            case W_QUEEN:
            case B_QUEEN:
                showQueenMoves(qzs, fromX, fromY);
                break;
            case W_ROOK:
            case W_ROOK_N:
            case B_ROOK:
            case B_ROOK_N:
                showRookMoves(qzs, fromX, fromY);
                break;
        }
    }

    /**
     * 王（K）：横、直、斜都可以走，但每次限走一步。
     * 和中国象棋相比，王是不可以送吃的，即任何被敌方控制的格子,己方王都不能走进去。否则，算“送王”犯规。累计三次犯规就要判负。(人家要送，为什么拦着人家，忽略这条规则)
     *
     * @param qzs
     * @param fromX
     * @param fromY
     */
    private void showKingMoves(int[][] qzs, int fromX, int fromY) {
        /*
        以下情况，王车不能易位：
        王和车已经移动过。
        王和车之间有棋子阻隔；
        王正在被将军；
        王经过或到达的位置受其他棋子攻击；
        王不可穿越被敌方攻击的格；
        王和车不在同一横行。（此规则为国际棋联在1972年所添加，是为了制止之前有棋手所采用过的将与王在同一直行的兵行至对方底线后升变为车再使用王车易位的战术。）
        */
        //王车易位
        if (ChessUtils.isKingFirstMove(qzs[fromX][fromY])) {//王没有移动过
            if (ChessUtils.isRookFirstMove(qzs[0][fromY]) || ChessUtils.isRookFirstMove(qzs[7][fromY])) {
                //车没有移动过
                boolean isLeftBlocked = qzs[1][fromY] != EMPTY || qzs[2][fromY] != EMPTY || qzs[3][fromY] != EMPTY;
                boolean isRightBlocked = qzs[5][fromY] != EMPTY || qzs[6][fromY] != EMPTY;
                if (!isLeftBlocked || !isRightBlocked) {
                    //王和车之间没有棋子阻隔
                    boolean[] isJiangedOrBlocked = isJiangedOrBlocked(qzs, fromX, fromY);
                    if (!isJiangedOrBlocked[0]) {
                        //王没有被将;王经过或到达的位置没有受其他棋子攻击
                        qzs[0][fromY] += EMPTY;
                    }
                    if (!isJiangedOrBlocked[1]) {
                        //王没有被将;王经过或到达的位置没有受其他棋子攻击
                        qzs[7][fromY] += EMPTY;
                    }
                }
            }
        }
        changeAroundStatus(qzs, fromX, fromY);
    }

    private void changeAroundStatus(int[][] qzs, int fromX, int fromY) {
        changeStatus(qzs, fromX, fromY, fromX, fromY - 1);//上
        changeStatus(qzs, fromX, fromY, fromX, fromY + 1);//下
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY);//左
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY);//右
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY - 1);//左上
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY + 1);//左下
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY - 1);//右上
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY + 1);//右下
    }

    //判断王是否正被将;王经过或到达的位置是否受其他棋子攻击
    private boolean[] isJiangedOrBlocked(int[][] qzs, int fromX, int fromY) {
        boolean isLeftBlocked = false;
        boolean isRightBlocked = false;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (qzs[i][j] == EMPTY) {
                    continue;
                }
                if (ChessUtils.isSameSide(qzs[fromX][fromY], qzs[i][j])) {
                    continue;
                }
                if (ChessUtils.isKing(qzs[i][j])) {
                    changeAroundStatus(qzs, i, j);
                } else {
                    showValidMoves(qzs, i, j);
                }
                boolean isKingJianged = qzs[fromX][fromY] < 0;
                if (isKingJianged) {
                    resetStatus(qzs);
                    return new boolean[]{true, true};
                }
                if (!isLeftBlocked) {
                    isLeftBlocked = qzs[1][fromY] != EMPTY || qzs[2][fromY] != EMPTY || qzs[3][fromY] != EMPTY;
                }
                if (!isRightBlocked) {
                    isRightBlocked = qzs[5][fromY] != EMPTY || qzs[6][fromY] != EMPTY;
                }
                if (isLeftBlocked && isRightBlocked) {
                    resetStatus(qzs);
                    return new boolean[]{true, true};
                }
                resetStatus(qzs);
            }
        }
        return new boolean[]{isLeftBlocked, isRightBlocked};
    }

    private void resetStatus(int[][] qzs) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (qzs[i][j] == EMPTY) {
                    continue;
                }
                if (qzs[i][j] < 0) {
                    qzs[i][j] = -qzs[i][j];
                }
                //可能易位
                if (qzs[i][j] > EMPTY) {
                    qzs[i][j] -= EMPTY;
                }
            }
        }
    }

    /**
     * 车（R）：
     * 横、竖均可以走，步数不受限制，不能斜走（和中国象棋类似）。
     * 除王车易位外不能越子。
     *
     * @param qzs
     * @param fromX
     * @param fromY
     */
    private void showRookMoves(int[][] qzs, int fromX, int fromY) {
        boolean isFEnabled = true, isBEnabled = true, isLEnabled = true, isREnabled = true;
        for (int i = 1; i < 8; i++) {
            if (isFEnabled) {//前
                isFEnabled = changeStatus(qzs, fromX, fromY, fromX, fromY - i);
            }
            if (isBEnabled) {//后
                isBEnabled = changeStatus(qzs, fromX, fromY, fromX, fromY + i);
            }
            if (isLEnabled) {//左
                isLEnabled = changeStatus(qzs, fromX, fromY, fromX - i, fromY);
            }
            if (isREnabled) {//右
                isREnabled = changeStatus(qzs, fromX, fromY, fromX + i, fromY);
            }
        }
    }

    /**
     * 象（B）：
     * 只能斜走，格数不限，但是，不能越子行棋。开局时每方各有两个象，
     * 一个占白格，一个占黑格。和中国象棋相比，走法类似，只是没有不
     * 能过河的概念，全盘皆能走。
     *
     * @param qzs
     * @param fromX
     * @param fromY
     */
    private void showBishopMoves(int[][] qzs, int fromX, int fromY) {
        //左上
        for (int i = 1; i < 8; i++) {
            if (!changeStatus(qzs, fromX, fromY, fromX - i, fromY - i)) {
                break;
            }
        }
        //左下
        for (int i = 1; i < 8; i++) {
            if (!changeStatus(qzs, fromX, fromY, fromX - i, fromY + i)) {
                break;
            }
        }
        //右上
        for (int i = 1; i < 8; i++) {
            if (!changeStatus(qzs, fromX, fromY, fromX + i, fromY - i)) {
                break;
            }
        }
        //右下
        for (int i = 1; i < 8; i++) {
            if (!changeStatus(qzs, fromX, fromY, fromX + i, fromY + i)) {
                break;
            }
        }
    }

    /**
     * 后（Q）：
     * 横、直、斜都可以走，步数不受限制，不能越子行棋。
     * 该棋也是棋力最强的棋子。
     *
     * @param qzs
     * @param fromX
     * @param fromY
     */
    private void showQueenMoves(int[][] qzs, int fromX, int fromY) {
        //上
        for (int i = fromY - 1; i >= 0; i--) {
            if (!changeStatus(qzs, fromX, fromY, fromX, i)) {//changeStatus=false表示碰到墙了
                break;
            }
        }
        //下
        for (int i = fromY + 1; i < 8; i++) {
            if (!changeStatus(qzs, fromX, fromY, fromX, i)) {
                break;
            }
        }
        //左
        for (int i = fromX - 1; i >= 0; i--) {
            if (!changeStatus(qzs, fromX, fromY, i, fromY)) {
                break;
            }
        }
        //右
        for (int i = fromX + 1; i < 8; i++) {
            if (!changeStatus(qzs, fromX, fromY, i, fromY)) {
                break;
            }
        }
        //左上
        for (int i = fromX - 1, j = fromY - 1; i >= 0 && j >= 0; i--, j--) {
            if (!changeStatus(qzs, fromX, fromY, i, j)) {
                break;
            }
        }
        //左下
        for (int i = fromX - 1, j = fromY + 1; i >= 0 && j < 8; i--, j++) {
            if (!changeStatus(qzs, fromX, fromY, i, j)) {
                break;
            }
        }
        //右上
        for (int i = fromX + 1, j = fromY - 1; i < 8 && j >= 0; i++, j--) {
            if (!changeStatus(qzs, fromX, fromY, i, j)) {
                break;
            }
        }
        //右下
        for (int i = fromX + 1, j = fromY + 1; i < 8 && j < 8; i++, j++) {
            if (!changeStatus(qzs, fromX, fromY, i, j)) {
                break;
            }
        }
    }

    /**
     * 马（N）：
     * 每行一步棋，先横走或直走一格，然后再往外斜走一格；或者先斜走一格，
     * 最后再往外横走或竖走一格（即走 日 字，这点也和中国象棋类似）。
     * 可以越子行走，而没有 中国象棋 的 蹩马腿 的限制。
     *
     * @param qzs
     * @param fromX
     * @param fromY
     */
    private void showKnightMoves(int[][] qzs, int fromX, int fromY) {
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY - 2);
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY + 2);
        changeStatus(qzs, fromX, fromY, fromX - 2, fromY + 1);
        changeStatus(qzs, fromX, fromY, fromX - 2, fromY - 1);
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY - 2);
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY + 2);
        changeStatus(qzs, fromX, fromY, fromX + 2, fromY - 1);
        changeStatus(qzs, fromX, fromY, fromX + 2, fromY + 1);
    }

    /**
     * 只能向前直走（不能后退，这点和中国象棋类似），
     * 每次行棋只能走一格。但是，走第一步时，可以走一格或两格。
     * 兵的吃子方法与行棋方向不一样，它是直走斜吃，即如果兵的
     * 前斜进一格内有对方棋子，就可以吃掉它，从而占据该格位置。
     *
     * @param qzs
     * @param fromX
     * @param fromY
     */
    private void showPawnMoves(int[][] qzs, int fromX, int fromY) {
        boolean isMyMove = ChessUtils.isMyChess(qzs[fromX][fromY], isMyFirst);
        if (isMyMove) {
            changeStatus(qzs, fromX, fromY, fromX, fromY - 1);//前进一步
            changeStatus(qzs, fromX, fromY, fromX - 1, fromY - 1);//吃子
            changeStatus(qzs, fromX, fromY, fromX + 1, fromY - 1);//吃子
            if (ChessUtils.isPawnFirstMove(qzs[fromX][fromY])) {
                changeStatus(qzs, fromX, fromY, fromX, fromY - 2);//前进2步
            }
            return;
        }
        changeStatus(qzs, fromX, fromY, fromX, fromY + 1);//前进一步
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY + 1);//吃子
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY + 1);//吃子
        if (ChessUtils.isPawnFirstMove(qzs[fromX][fromY])) {
            changeStatus(qzs, fromX, fromY, fromX, fromY + 2);//前进2步
        }
    }

    private boolean isOutOfRange(int xx, int yy) {
        if (xx < 0) {
            return true;
        }
        if (xx > 7) {
            return true;
        }
        if (yy < 0) {
            return true;
        }
        if (yy > 7) {
            return true;
        }
        return false;
    }

    /**
     * @param qzs
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @return false:碰到墙了
     */
    private boolean changeStatus(int[][] qzs, int fromX, int fromY, int toX, int toY) {
        if (isOutOfRange(toX, toY)) {
            return false;
        }
        //特殊
        if (ChessUtils.isPawn(qzs[fromX][fromY])) {
            //前进
            if (fromX == toX) {
                if (qzs[toX][toY] == EMPTY) {
                    qzs[toX][toY] = -qzs[toX][toY];
                }
                return false;
            }
            //吃子
            if (qzs[toX][toY] != EMPTY && !ChessUtils.isSameSide(qzs[fromX][fromY], qzs[toX][toY])) {
                qzs[toX][toY] = -qzs[toX][toY];
            }
            return false;
        }
        //一般
        if (qzs[toX][toY] == EMPTY) {
            qzs[toX][toY] = -qzs[toX][toY];
            return true;
        }
        if (!ChessUtils.isSameSide(qzs[fromX][fromY], qzs[toX][toY])) {
            qzs[toX][toY] = -qzs[toX][toY];
        }
        return false;
    }
}
