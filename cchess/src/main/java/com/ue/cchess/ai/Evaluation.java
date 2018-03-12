package com.ue.cchess.ai;

import android.graphics.Point;

import com.ue.cchess.util.ChessUtils;

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

public class Evaluation {
    // 定义每种棋子的基本价值
    private final static int BASE_PAWN = 100;
    private final static int BASE_BISHOP = 250;
    private final static int BASE_ELEPHANT = 250;
    private final static int BASE_CAR = 500;
    private final static int BASE_HORSE = 350;
    private final static int BASE_CANON = 350;
    private final static int BASE_KING = 10000;

    // 定义各棋子的灵活性 ,每多一个可走位置应加上的分值
    private final static int FLEX_PAWN = 15;
    private final static int FLEX_BISHOP = 1;
    private final static int FLEX_ELEPHANT = 1;
    private final static int FLEX_CAR = 6;
    private final static int FLEX_HORSE = 12;
    private final static int FLEX_CANON = 6;
    private final static int FLEX_KING = 0;

    //红卒的附加值矩阵
    private final int BA0[][] = new int[][]{
            {0, 90, 90, 70, 70, 0, 0, 0, 0, 0},
            {0, 90, 90, 90, 70, 0, 0, 0, 0, 0},
            {0, 110, 110, 110, 70, 0, 0, 0, 0, 0},
            {0, 120, 120, 110, 70, 0, 0, 0, 0, 0},
            {0, 120, 120, 110, 70, 0, 0, 0, 0, 0},
            {0, 120, 120, 110, 70, 0, 0, 0, 0, 0},
            {0, 110, 110, 110, 70, 0, 0, 0, 0, 0},
            {0, 90, 90, 90, 70, 0, 0, 0, 0, 0},
            {0, 90, 90, 70, 70, 0, 0, 0, 0, 0},
    };
    //黑兵的附加值矩阵
    private final int[][] BA1 = new int[][]{
            {0, 0, 0, 0, 0, 70, 70, 90, 90, 0},
            {0, 0, 0, 0, 0, 70, 90, 90, 90, 0},
            {0, 0, 0, 0, 0, 70, 110, 110, 110, 0},
            {0, 0, 0, 0, 0, 70, 110, 120, 120, 0},
            {0, 0, 0, 0, 0, 70, 110, 120, 120, 0},
            {0, 0, 0, 0, 0, 70, 110, 120, 120, 0},
            {0, 0, 0, 0, 0, 70, 110, 110, 110, 0},
            {0, 0, 0, 0, 0, 70, 90, 90, 90, 0},
            {0, 0, 0, 0, 0, 70, 70, 90, 90, 0},
    };

    /**
     * 为每一个兵返回附加值
     *
     * @param x
     * @param y
     * @param qzs
     * @return
     */
    private int getBingValue(int x, int y, int qzs[][]) {
        if (qzs[x][y] == R_SOLDIER)
            return BA0[x][y];

        if (qzs[x][y] == B_SOLDIER)
            return BA1[x][y];
        return 0;
    }

    private int baseValues[] = new int[15];//存放棋子基本价值的数组
    private int flexValues[] = new int[15];//存放棋子灵活性分数的数组
    private int attackPos[][] = new int[9][10];//存放每一位置被威胁的信息
    private int guardPos[][] = new int[9][10];//存放每一位置被保护的信息
    private int flexPos[][] = new int[9][10];//存放每一位置上的棋子的灵活性分数
    private int chessValues[][] = new int[9][10];//存放每一位置上的棋子的总价值
    private int posCount = 0;//记录一棋子的相关位置个数
    private Point relatePos[] = new Point[20];//记录一棋子相关位子的数组

    public Evaluation() {
        baseValues[B_GENERAL] = BASE_KING;
        baseValues[B_CHARIOT] = BASE_CAR;
        baseValues[B_HORSE] = BASE_HORSE;
        baseValues[B_GUARD] = BASE_BISHOP;
        baseValues[B_ELEPHANT] = BASE_ELEPHANT;
        baseValues[B_CANNON] = BASE_CANON;
        baseValues[B_SOLDIER] = BASE_PAWN;

        baseValues[R_GENERAL] = BASE_KING;
        baseValues[R_CHARIOT] = BASE_CAR;
        baseValues[R_HORSE] = BASE_HORSE;
        baseValues[R_GUARD] = BASE_BISHOP;
        baseValues[R_ELEPHANT] = BASE_ELEPHANT;
        baseValues[R_CANNON] = BASE_CANON;
        baseValues[R_SOLDIER] = BASE_PAWN;

        flexValues[B_GENERAL] = FLEX_KING;
        flexValues[B_CHARIOT] = FLEX_CAR;
        flexValues[B_HORSE] = FLEX_HORSE;
        flexValues[B_GUARD] = FLEX_BISHOP;
        flexValues[B_ELEPHANT] = FLEX_ELEPHANT;
        flexValues[B_CANNON] = FLEX_CANON;
        flexValues[B_SOLDIER] = FLEX_PAWN;

        flexValues[R_GENERAL] = FLEX_KING;
        flexValues[R_CHARIOT] = FLEX_CAR;
        flexValues[R_HORSE] = FLEX_HORSE;
        flexValues[R_GUARD] = FLEX_BISHOP;
        flexValues[R_ELEPHANT] = FLEX_ELEPHANT;
        flexValues[R_CANNON] = FLEX_CANON;
        flexValues[R_SOLDIER] = FLEX_PAWN;
    }

    /**
     * @param position
     * @param isRedTurn 轮到谁的标志，!0 是红,0 是黑
     * @return
     */
    public int evaluate(int position[][], int isRedTurn) {
        int i, j, k;
        int chessType, targetType;

        for (i = 0; i < 9; i++) { // 初始化数值
            for (j = 0; j < 10; j++) {
                chessValues[i][j] = 0;
                attackPos[i][j] = 0;
                guardPos[i][j] = 0;
                flexPos[i][j] = 0;
            }
        }

        for (i = 0; i < 9; i++)
            for (j = 0; j < 10; j++) {
                if (position[i][j] != EMPTY) {
                    chessType = position[i][j];
                    getRelatePiece(position, i, j);
                    for (k = 0; k < posCount; k++) {
                        targetType = position[relatePos[k].x][relatePos[k].y];
                        if (targetType == EMPTY) {
                            flexPos[i][j]++;
                        } else {
                            if (ChessUtils.isSameSide(chessType, targetType)) {
                                guardPos[relatePos[k].x][relatePos[k].y]++;
                            } else {
                                attackPos[relatePos[k].x][relatePos[k].y]++;
                                flexPos[i][j]++;
                                switch (targetType) {
                                    case R_GENERAL:
                                        if (isRedTurn == 0)
                                            return 18888;
                                        break;
                                    case B_GENERAL:
                                        if (isRedTurn == 1)
                                            return 18888;
                                        break;
                                    default:
                                        attackPos[relatePos[k].x][relatePos[k].y] += (30 + (baseValues[targetType] - baseValues[chessType]) / 10) / 10;
                                        break;
                                }
                            }
                        }
                    }
                }
            }

        for (i = 0; i < 9; i++)
            for (j = 0; j < 10; j++) {
                if (position[i][j] != EMPTY) {
                    chessType = position[i][j];
                    chessValues[i][j]++;
                    chessValues[i][j] += flexValues[chessType] * flexPos[i][j];
                    chessValues[i][j] += getBingValue(i, j, position);
                }
            }
        int halfValue;
        for (i = 0; i < 9; i++)
            for (j = 0; j < 10; j++) {
                if (position[i][j] != EMPTY) {
                    chessType = position[i][j];
                    halfValue = baseValues[chessType] / 16;
                    chessValues[i][j] += baseValues[chessType];

                    if (ChessUtils.isRed(chessType)) {
                        if (attackPos[i][j] != 0) {
                            if (isRedTurn != 0) {
                                if (chessType == R_GENERAL) {
                                    chessValues[i][j] -= 20;
                                } else {
                                    chessValues[i][j] -= halfValue * 2;
                                    if (guardPos[i][j] != 0)
                                        chessValues[i][j] += halfValue;
                                }
                            } else {
                                if (chessType == R_GENERAL)
                                    return 18888;
                                chessValues[i][j] -= halfValue * 10;
                                if (guardPos[i][j] != 0)
                                    chessValues[i][j] += halfValue * 9;
                            }
                            chessValues[i][j] -= attackPos[i][j];
                        } else {
                            if (guardPos[i][j] != 0)
                                chessValues[i][j] += 5;
                        }
                    } else {
                        if (attackPos[i][j] != 0) {
                            if (isRedTurn == 0) {
                                if (chessType == B_GENERAL) {
                                    chessValues[i][j] -= 20;
                                } else {
                                    chessValues[i][j] -= halfValue * 2;
                                    if (guardPos[i][j] != 0)
                                        chessValues[i][j] += halfValue;
                                }
                            } else {
                                if (chessType == B_GENERAL)
                                    return 18888;
                                chessValues[i][j] -= halfValue * 10;
                                if (guardPos[i][j] != 0)
                                    chessValues[i][j] += halfValue * 9;
                            }
                            chessValues[i][j] -= attackPos[i][j];
                        } else {
                            if (guardPos[i][j] != 0)
                                chessValues[i][j] += 5;
                        }
                    }
                }
            }

        int redValue = 0;
        int blackValue = 0;

        for (i = 0; i < 9; i++)
            for (j = 0; j < 10; j++) {
                chessType = position[i][j];
                if (chessType != EMPTY) {
                    if (ChessUtils.isRed(chessType)) {
                        redValue += chessValues[i][j];
                    } else {
                        blackValue += chessValues[i][j];
                    }
                }
            }
        if (isRedTurn != 0) {
            return redValue - blackValue;
        } else {
            return blackValue - redValue;
        }
    }

    /**
     * 枚举给定位置上的所有相关位置
     *
     * @param position
     * @param j
     * @param i
     * @return
     */
    private int getRelatePiece(int position[][], int i, int j) {
        posCount = 0;
        int chessId;
        boolean flag;
        int x, y;

        chessId = position[i][j];
        switch (chessId) {
            case R_GENERAL:
            case B_GENERAL:
                for (y = 0; y < 3; y++)
                    for (x = 3; x < 6; x++)
                        if (canTouch(position, i, j, x, y))
                            addPoint(x, y);
                for (y = 7; y < 10; y++)
                    for (x = 3; x < 6; x++)
                        if (canTouch(position, i, j, x, y))
                            addPoint(x, y);
                break;

            case R_GUARD:
                for (y = 7; y < 10; y++)
                    for (x = 3; x < 6; x++)
                        if (canTouch(position, i, j, x, y))
                            addPoint(x, y);
                break;

            case B_GUARD:
                for (y = 0; y < 3; y++)
                    for (x = 3; x < 6; x++)
                        if (canTouch(position, i, j, x, y))
                            addPoint(x, y);
                break;

            case R_ELEPHANT:
            case B_ELEPHANT:
                x = i + 2;
                y = j + 2;
                if (x < 9 && y < 10 && canTouch(position, i, j, x, y))
                    addPoint(x, y);

                x = i + 2;
                y = j - 2;
                if (x < 9 && y >= 0 && canTouch(position, i, j, x, y))
                    addPoint(x, y);

                x = i - 2;
                y = j + 2;
                if (x >= 0 && y < 10 && canTouch(position, i, j, x, y))
                    addPoint(x, y);

                x = i - 2;
                y = j - 2;
                if (x >= 0 && y >= 0 && canTouch(position, i, j, x, y))
                    addPoint(x, y);
                break;

            case R_HORSE:
            case B_HORSE:
                x = i + 2;
                y = j + 1;
                if ((x < 9 && y < 10) && canTouch(position, i, j, x, y))
                    addPoint(x, y);

                x = i + 2;
                y = j - 1;
                if ((x < 9 && y >= 0) && canTouch(position, i, j, x, y))
                    addPoint(x, y);

                x = i - 2;
                y = j + 1;
                if ((x >= 0 && y < 10) && canTouch(position, i, j, x, y))
                    addPoint(x, y);

                x = i - 2;
                y = j - 1;
                if ((x >= 0 && y >= 0) && canTouch(position, i, j, x, y))
                    addPoint(x, y);

                x = i + 1;
                y = j + 2;
                if ((x < 9 && y < 10) && canTouch(position, i, j, x, y))
                    addPoint(x, y);
                x = i - 1;
                y = j + 2;
                if ((x >= 0 && y < 10) && canTouch(position, i, j, x, y))
                    addPoint(x, y);
                x = i + 1;
                y = j - 2;
                if ((x < 9 && y >= 0) && canTouch(position, i, j, x, y))
                    addPoint(x, y);
                x = i - 1;
                y = j - 2;
                if ((x >= 0 && y >= 0) && canTouch(position, i, j, x, y))
                    addPoint(x, y);
                break;

            case R_CHARIOT:
            case B_CHARIOT:
                x = i + 1;
                y = j;
                while (x < 9) {
                    if (EMPTY == position[x][y])
                        addPoint(x, y);
                    else {
                        addPoint(x, y);
                        break;
                    }
                    x++;
                }

                x = i - 1;
                y = j;
                while (x >= 0) {
                    if (EMPTY == position[x][y])
                        addPoint(x, y);
                    else {
                        addPoint(x, y);
                        break;
                    }
                    x--;
                }

                x = i;
                y = j + 1;//
                while (y < 10) {
                    if (EMPTY == position[x][y])
                        addPoint(x, y);
                    else {
                        addPoint(x, y);
                        break;
                    }
                    y++;
                }

                x = i;
                y = j - 1;
                while (y >= 0) {
                    if (EMPTY == position[x][y])
                        addPoint(x, y);
                    else {
                        addPoint(x, y);
                        break;
                    }
                    y--;
                }
                break;

            case R_SOLDIER:
                y = j - 1;
                x = i;

                if (y >= 0)
                    addPoint(x, y);

                if (i < 5) {
                    y = j;
                    x = i + 1;
                    if (x < 9)
                        addPoint(x, y);
                    x = i - 1;
                    if (x >= 0)
                        addPoint(x, y);
                }
                break;

            case B_SOLDIER:
                y = j + 1;
                x = i;

                if (y < 10)
                    addPoint(x, y);

                if (i > 4) {
                    y = j;
                    x = i + 1;
                    if (x < 9)
                        addPoint(x, y);
                    x = i - 1;
                    if (x >= 0)
                        addPoint(x, y);
                }
                break;

            case B_CANNON:
            case R_CANNON:

                x = i + 1;        //
                y = j;
                flag = false;
                while (x < 9) {
                    if (EMPTY == position[x][y]) {
                        if (!flag)
                            addPoint(x, y);
                    } else {
                        if (!flag)
                            flag = true;
                        else {
                            addPoint(x, y);
                            break;
                        }
                    }
                    x++;
                }

                x = i - 1;
                flag = false;
                while (x >= 0) {
                    if (EMPTY == position[x][y]) {
                        if (!flag)
                            addPoint(x, y);
                    } else {
                        if (!flag)
                            flag = true;
                        else {
                            addPoint(x, y);
                            break;
                        }
                    }
                    x--;
                }
                x = i;
                y = j + 1;
                flag = false;
                while (y < 10) {
                    if (EMPTY == position[x][y]) {
                        if (!flag)
                            addPoint(x, y);
                    } else {
                        if (!flag)
                            flag = true;
                        else {
                            addPoint(x, y);
                            break;
                        }
                    }
                    y++;
                }

                y = j - 1;    //
                flag = false;
                while (y >= 0) {
                    if (EMPTY == position[x][y]) {
                        if (!flag)
                            addPoint(x, y);
                    } else {
                        if (!flag)
                            flag = true;
                        else {
                            addPoint(x, y);
                            break;
                        }
                    }
                    y--;
                }
                break;

            default:
                break;

        }
        return posCount;
    }

    /**
     * 判断棋盘position上位置From的棋子能否走到位置To
     *
     * @param position
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @return
     */
    private boolean canTouch(int position[][], int fromX, int fromY, int toX, int toY) {
        int i = 0, j = 0;
        int moveChessID, targetID;
        if (fromY == toY && fromX == toX)
            return false;//目的与源相同

        moveChessID = position[fromX][fromY];
        targetID = position[toX][toY];

        switch (moveChessID) {
            case B_GENERAL:   // 黑帅
                if (targetID == R_GENERAL) // 老将见面?
                {
                    if (fromX != toX)// 横坐标是否相等
                        return false;
                    for (i = fromY + 1; i < toY; i++)
                        if (position[fromX][i] != EMPTY)
                            return false;// 中间隔有棋子，返回false
                } else {
                    if (toY > 2 || toX > 5 || toX < 3)
                        return false;//目标点在九宫之外
                    if (Math.abs(fromY - toY) + Math.abs(toX - fromX) > 1)
                        return false;//将帅只走一步直线:
                }
                break;

            case R_GENERAL: // 红将
                if (targetID == B_GENERAL)//老将见面?
                {
                    if (fromX != toX)
                        return false;//两个将不在同一列
                    for (i = fromY - 1; i > toY; i--)
                        if (position[fromX][i] != EMPTY)
                            return false;//中间有别的子
                } else {
                    if (toY < 7 || toX > 5 || toX < 3)
                        return false;//目标点在九宫之外
                    if (Math.abs(fromY - toY) + Math.abs(toX - fromX) > 1)
                        return false;//将帅只走一步直线:
                }
                break;

            case R_GUARD:  // 红士
                if (toY < 7 || toX > 5 || toX < 3)
                    return false;//士出九宫
                if (Math.abs(fromY - toY) != 1 || Math.abs(toX - fromX) != 1)
                    return false;    //士走必须走斜线
                break;

            case B_GUARD:   //黑士
                if (toY > 2 || toX > 5 || toX < 3)
                    return false;//士出九宫
                if (Math.abs(fromY - toY) != 1 || Math.abs(toX - fromX) != 1)
                    return false;    //士走斜线
                break;

            case R_ELEPHANT: //红象
                if (toY < 5)
                    return false;//相不能过河
                if (Math.abs(fromX - toX) != 2 || Math.abs(fromY - toY) != 2)
                    return false;//相走田字
                if (position[(fromX + toX) / 2][(fromY + toY) / 2] != EMPTY)
                    return false;//相眼被塞住了
                break;

            case B_ELEPHANT://黑象
                if (toY > 4)
                    return false;//相不能过河
                if (Math.abs(fromX - toX) != 2 || Math.abs(fromY - toY) != 2)
                    return false;//相走田字
                if (position[(fromX + toX) / 2][(fromY + toY) / 2] != EMPTY)
                    return false;//相眼被塞住了
                break;

            case B_SOLDIER:     //黑兵
                if (toY < fromY)
                    return false;//兵不回头
                if (fromY < 5 && fromY == toY)
                    return false;//兵过河前只能直走
                if (toY - fromY + Math.abs(toX - fromX) > 1)
                    return false;//兵只走一步直线:
                break;

            case R_SOLDIER:    //红兵
                if (toY > fromY)
                    return false;//兵不回头
                if (fromY > 4 && fromY == toY)
                    return false;//兵过河前只能直走
                if (fromY - toY + Math.abs(toX - fromX) > 1)
                    return false;//兵只走一步直线:
                break;


            case B_CHARIOT:  // 黑车
            case R_CHARIOT:  // 红车

                if (fromY != toY && fromX != toX)
                    return false;    //车走直线:
                if (fromY == toY) {
                    if (fromX < toX) {
                        for (i = fromX + 1; i < toX; i++)
                            if (position[i][fromY] != EMPTY)
                                return false;// 中间不能有棋子
                    } else {
                        for (i = toX + 1; i < fromX; i++)
                            if (position[i][fromY] != EMPTY)
                                return false;
                    }
                } else {
                    if (fromY < toY) {
                        for (j = fromY + 1; j < toY; j++)
                            if (position[fromX][j] != EMPTY)
                                return false;
                    } else {
                        for (j = toY + 1; j < fromY; j++)
                            if (position[fromX][j] != EMPTY)
                                return false;
                    }
                }

                break;

            case B_HORSE:   // 黑马
            case R_HORSE:   // 红马

                if (!((Math.abs(toX - fromX) == 1 && Math.abs(toY - fromY) == 2)
                        || (Math.abs(toX - fromX) == 2 && Math.abs(toY - fromY) == 1)))
                    return false;//马走日字
                if (toX - fromX == 2) {
                    i = fromX + 1;
                    j = fromY;
                } else if (fromX - toX == 2) {
                    i = fromX - 1;
                    j = fromY;
                } else if (toY - fromY == 2) {
                    i = fromX;
                    j = fromY + 1;
                } else if (fromY - toY == 2) {
                    i = fromX;
                    j = fromY - 1;
                }
                if (position[i][j] != EMPTY)
                    return false; //绊马腿
                break;

            case B_CANNON: // 黑炮
            case R_CANNON: // 红炮

                if (fromY != toY && fromX != toX)
                    return false;    //炮走直线:

                //炮不吃子时，经过的路线中不能有棋子
                if (position[toX][toY] == EMPTY) {
                    if (fromY == toY) {
                        if (fromX < toX) {
                            for (i = fromX + 1; i < toX; i++)
                                if (position[i][fromY] != EMPTY)
                                    return false;
                        } else {
                            for (i = toX + 1; i < fromX; i++)
                                if (position[i][fromY] != EMPTY)
                                    return false;
                        }
                    } else {
                        if (fromY < toY) {
                            for (j = fromY + 1; j < toY; j++)
                                if (position[fromX][j] != EMPTY)
                                    return false;
                        } else {
                            for (j = toY + 1; j < fromY; j++)
                                if (position[fromX][j] != EMPTY)
                                    return false;
                        }
                    }
                } else    //炮吃子时，中间必须只隔一个棋子
                {
                    int count = 0;
                    if (fromY == toY) {
                        if (fromX < toX) {
                            for (i = fromX + 1; i < toX; i++)
                                if (position[i][fromY] != EMPTY)
                                    count++;
                            if (count != 1)
                                return false;
                        } else {
                            for (i = toX + 1; i < fromX; i++)
                                if (position[i][fromY] != EMPTY)
                                    count++;
                            if (count != 1)
                                return false;
                        }
                    } else {
                        if (fromY < toY) {
                            for (j = fromY + 1; j < toY; j++)
                                if (position[fromX][j] != EMPTY)
                                    count++;
                            if (count != 1)
                                return false;
                        } else {
                            for (j = toY + 1; j < fromY; j++)
                                if (position[fromX][j] != EMPTY)
                                    count++;
                            if (count != 1)
                                return false;
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * 将一个位置加入到数组RelatePos中
     *
     * @param x
     * @param y
     */
    private void addPoint(int x, int y) {
        if (relatePos[posCount] == null) {
            relatePos[posCount] = new Point();
        }
        relatePos[posCount].x = x;
        relatePos[posCount].y = y;
        posCount++;
    }
}