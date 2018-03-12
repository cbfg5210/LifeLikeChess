package com.ue.cchess.entity;

import static com.ue.cchess.entity.ChessID.B_CANNON;
import static com.ue.cchess.entity.ChessID.B_CHARIOT;
import static com.ue.cchess.entity.ChessID.B_ELEPHANT;
import static com.ue.cchess.entity.ChessID.B_GENERAL;
import static com.ue.cchess.entity.ChessID.B_GUARD;
import static com.ue.cchess.entity.ChessID.B_HORSE;
import static com.ue.cchess.entity.ChessID.B_SOLDIER;
import static com.ue.cchess.entity.ChessID.EMPTY;
import static com.ue.cchess.entity.ChessID.R_CANNON;
import static com.ue.cchess.entity.ChessID.R_CHARIOT;
import static com.ue.cchess.entity.ChessID.R_ELEPHANT;
import static com.ue.cchess.entity.ChessID.R_GENERAL;
import static com.ue.cchess.entity.ChessID.R_GUARD;
import static com.ue.cchess.entity.ChessID.R_HORSE;
import static com.ue.cchess.entity.ChessID.R_SOLDIER;

/**
 * Created by hawk on 2017/11/6.
 */

public class MoveGenerator {
    private int weight = 0;//0:无炮台,>0有炮台
    private boolean isMyFirst;

    public void setMyFirst(boolean isMyFirst) {
        this.isMyFirst = isMyFirst;
    }

    public void showValidMoves(int[][] qzs, int fromX, int fromY) {
        switch (qzs[fromX][fromY]) {
            case B_CHARIOT:
            case R_CHARIOT:
                showChariotMoves(qzs, fromX, fromY);
                break;
            case B_CANNON:
            case R_CANNON:
                showCannonMoves(qzs, fromX, fromY);
                break;
            case B_GUARD:
            case R_GUARD:
                showGuardMoves(qzs, fromX, fromY);
                break;
            case B_HORSE:
            case R_HORSE:
                showHorseMoves(qzs, fromX, fromY);
                break;
            case B_ELEPHANT:
            case R_ELEPHANT:
                showElephantMoves(qzs, fromX, fromY);
                break;
            case B_GENERAL:
            case R_GENERAL:
                showGeneralMoves(qzs, fromX, fromY);
                break;
            case B_SOLDIER:
            case R_SOLDIER:
                showSoldierMoves(qzs, fromX, fromY);
                break;
        }
    }

    private void showCannonMoves(int[][] qzs, int fromX, int fromY) {
        //前
        weight = 0;
        for (int i = fromY - 1; i >= 0; i--) {
            if (changeStatus(qzs, fromX, fromY, fromX, i)) {
                break;
            }
        }
        //后
        weight = 0;
        for (int i = fromY + 1; i < 10; i++) {
            if (changeStatus(qzs, fromX, fromY, fromX, i)) {
                break;
            }
        }
        //左
        weight = 0;
        for (int i = fromX - 1; i >= 0; i--) {
            if (changeStatus(qzs, fromX, fromY, i, fromY)) {
                break;
            }
        }
        //右
        weight = 0;
        for (int i = fromX + 1; i < 9; i++) {
            if (changeStatus(qzs, fromX, fromY, i, fromY)) {
                break;
            }
        }
    }

    private void showChariotMoves(int[][] qzs, int fromX, int fromY) {
        //前
        for (int i = fromY - 1; i >= 0; i--) {
            if (changeStatus(qzs, fromX, fromY, fromX, i)) {
                break;
            }
        }
        //后
        for (int i = fromY + 1; i < 10; i++) {
            if (changeStatus(qzs, fromX, fromY, fromX, i)) {
                break;
            }
        }
        //左
        for (int i = fromX - 1; i >= 0; i--) {
            if (changeStatus(qzs, fromX, fromY, i, fromY)) {
                break;
            }
        }
        //右
        for (int i = fromX + 1; i < 9; i++) {
            if (changeStatus(qzs, fromX, fromY, i, fromY)) {
                break;
            }
        }
    }

    private void showGeneralMoves(int[][] qzs, int fromX, int fromY) {
        //是否将帅相对且之间没子
        int oFlag, yStartIndx;

        if (qzs[fromX][fromY] == B_GENERAL) {
            yStartIndx = isMyFirst ? 7 : 0;
            oFlag = R_GENERAL;
        } else {
            yStartIndx = isMyFirst ? 0 : 7;
            oFlag = B_GENERAL;
        }
        //找对面是否有General
        int yy = -1;
        for (int i = 0, j = yStartIndx; i < 3; i++, j++) {
            if (qzs[fromX][j] != EMPTY && qzs[fromX][j] == oFlag) {
                yy = j;
                break;
            }
        }
        if (yy != -1) {
            //将帅间是否有棋子阻隔
            boolean isBlock = false;
            int from, to;
            if (fromY < yy) {
                from = fromY + 1;
                to = yy;
            } else {
                from = yy + 1;
                to = fromY;
            }
            for (int i = from; i < to; i++) {
                if (qzs[fromX][i] != EMPTY) {
                    isBlock = true;
                    break;
                }
            }
            if (!isBlock) {
                //之间无子
                qzs[fromX][yy] = -qzs[fromX][yy];
            }
        }
        //前
        changeStatus(qzs, fromX, fromY, fromX, fromY - 1);
        //后
        changeStatus(qzs, fromX, fromY, fromX, fromY + 1);
        //左
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY);
        //右
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY);
    }

    private void showGuardMoves(int[][] qzs, int fromX, int fromY) {
        //4*4格子，士在中心时有最多走法-4种
        //左上
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY - 1);
        //右上
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY - 1);
        //左下
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY + 1);
        //右下
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY + 1);
    }

    private void showSoldierMoves(int[][] qzs, int fromX, int fromY) {
        //只能走一步:前/左/右
        //我方
        if ((isMyFirst && qzs[fromX][fromY] == R_SOLDIER) || (!isMyFirst && qzs[fromX][fromY] == B_SOLDIER)) {
            if (fromY > 4) {
                //过河前
                changeStatus(qzs, fromX, fromY, fromX, fromY - 1);//前行
            } else {
                //过河后
                changeStatus(qzs, fromX, fromY, fromX, fromY - 1);//前行
                changeStatus(qzs, fromX, fromY, fromX - 1, fromY);//左行
                changeStatus(qzs, fromX, fromY, fromX + 1, fromY);//右行
            }
            return;
        }
        //对方
        if (fromY < 5) {
            //过河前
            changeStatus(qzs, fromX, fromY, fromX, fromY + 1);//前行
        } else {
            //过河后
            changeStatus(qzs, fromX, fromY, fromX, fromY + 1);//前行
            changeStatus(qzs, fromX, fromY, fromX - 1, fromY);//左行
            changeStatus(qzs, fromX, fromY, fromX + 1, fromY);//右行
        }
    }

    private void showHorseMoves(int[][] qzs, int fromX, int fromY) {
        //8个位置
        //前
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY - 2);
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY - 2);
        //后
        changeStatus(qzs, fromX, fromY, fromX - 1, fromY + 2);
        changeStatus(qzs, fromX, fromY, fromX + 1, fromY + 2);
        //左
        changeStatus(qzs, fromX, fromY, fromX - 2, fromY + 1);
        changeStatus(qzs, fromX, fromY, fromX - 2, fromY - 1);
        //右
        changeStatus(qzs, fromX, fromY, fromX + 2, fromY - 1);
        changeStatus(qzs, fromX, fromY, fromX + 2, fromY + 1);
    }

    private void showElephantMoves(int[][] qzs, int fromX, int fromY) {
        //左上
        changeStatus(qzs, fromX, fromY, fromX - 2, fromY - 2);
        //右上
        changeStatus(qzs, fromX, fromY, fromX + 2, fromY - 2);
        //左下
        changeStatus(qzs, fromX, fromY, fromX - 2, fromY + 2);
        //右下
        changeStatus(qzs, fromX, fromY, fromX + 2, fromY + 2);
    }

    /**
     * 目标位置是否是将/士的有效走位
     *
     * @param toX
     * @param toY
     * @return
     */
    private boolean isValidPoint4GG(int toX, int toY) {
        if (toX < 3) {
            return false;
        }
        if (toX > 5) {
            return false;
        }
        if (toY < 0) {
            return false;
        }
        if (toY > 9) {
            return false;
        }
        if (toY > 2 && toY < 7) {
            return false;
        }
        return true;
    }

    /**
     * 目标位置是否是象的有效走位
     *
     * @param qzs
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @return
     */
    private boolean isValidPoint4Elephant(int[][] qzs, int fromX, int fromY, int toX, int toY) {
        if (!isValidPoint(toX, toY)) {
            return false;
        }
        //象不能过河
        if (fromY < 5 && toY > 4) {
            return false;
        }
        if (fromY > 4 && toY < 5) {
            return false;
        }
        //是否塞象眼
        if (qzs[(fromX + toX) / 2][(fromY + toY) / 2] != EMPTY) {
            return false;
        }
        return true;
    }

    /**
     * 目标位置是否是马的有效走位
     *
     * @param qzs
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @return
     */
    private boolean isValidPoint4Horse(int[][] qzs, int fromX, int fromY, int toX, int toY) {
        if (!isValidPoint(toX, toY)) {
            return false;
        }
        //是否塞马脚
        int majiaoX, majiaoY;
        if (fromY - 2 == toY) {
            //前
            majiaoX = fromX;
            majiaoY = fromY - 1;
        } else if (fromY + 2 == toY) {
            //后
            majiaoX = fromX;
            majiaoY = fromY + 1;
        } else if (fromX - 2 == toX) {
            //左
            majiaoX = fromX - 1;
            majiaoY = fromY;
        } else {
            //右
            majiaoX = fromX + 1;
            majiaoY = fromY;
        }
        if (qzs[majiaoX][majiaoY] != EMPTY) {
            return false;
        }

        return true;
    }

    /**
     * 目标位置是否超出棋盘
     *
     * @param toX
     * @param toY
     * @return
     */
    private boolean isValidPoint(int toX, int toY) {
        if (toX < 0) {
            return false;
        }
        if (toX > 8) {
            return false;
        }
        if (toY < 0) {
            return false;
        }
        if (toY > 9) {
            return false;
        }
        return true;
    }

    /**
     * @param qzs
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @return true:遇到墙了
     */
    private boolean changeStatus(int[][] qzs, int fromX, int fromY, int toX, int toY) {
        //检查目标位置是否是合理走位
        switch (qzs[fromX][fromY]) {
            case B_HORSE:
            case R_HORSE:
                if (!isValidPoint4Horse(qzs, fromX, fromY, toX, toY)) {
                    return true;
                }
                break;
            case B_ELEPHANT:
            case R_ELEPHANT:
                if (!isValidPoint4Elephant(qzs, fromX, fromY, toX, toY)) {
                    return true;
                }
                break;
            case B_GUARD:
            case R_GUARD:
            case B_GENERAL:
            case R_GENERAL:
                if (!isValidPoint4GG(toX, toY)) {
                    return true;
                }
                break;
            default:
                if (!isValidPoint(toX, toY)) {
                    return true;
                }
        }
        //标记目标位置是否可以落子或吃子
        //特殊:炮
        if (qzs[fromX][fromY] == B_CANNON || qzs[fromX][fromY] == R_CANNON) {
            if (qzs[toX][toY] == EMPTY) {
                if (weight == 0) {
                    qzs[toX][toY] = -qzs[toX][toY];
                }
                return false;
            }
            if (weight == 0) {//炮台
                weight++;
                return false;
            }
            if (!isSameSide(qzs[fromX][fromY], qzs[toX][toY]) && weight == 1) {
                //靶子
                qzs[toX][toY] = -qzs[toX][toY];
            }
            return true;
        }
        //其它
        if (qzs[toX][toY] == EMPTY) {
            qzs[toX][toY] = -qzs[toX][toY];
            return false;
        }
        if (!isSameSide(qzs[fromX][fromY], qzs[toX][toY])) {
            qzs[toX][toY] = -qzs[toX][toY];
        }
        return true;
    }

    private boolean isSameSide(int fromF, int toF) {
        if (fromF < 8) {
            return toF < 8;
        }
        if (toF == EMPTY) {
            return false;
        }
        return toF > 7;
    }
}