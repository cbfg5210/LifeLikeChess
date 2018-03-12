package com.ue.gobang.util;

import android.graphics.Point;

import java.util.List;

/**
 * Reference:
 * Author:
 * Date:2016/8/9.
 */
public class GobangUtils {
    /**
     * @param points
     * @param moves
     * @return Roles.ERROR:board is full   Roles.ING:not terminal  index:win
     */
    public static int terminal(int[][] points, List<Point> moves) {
        int moveSize = moves.size();
        Point lastMove = moves.get(moveSize - 1);

        // Check around the last move placed to see if it formed a five
        if (checkHorizontal(lastMove.x, lastMove.y, points)) {
            return points[lastMove.x][lastMove.y];
        }
        if (checkVertical(lastMove.x, lastMove.y, points)) {
            return points[lastMove.x][lastMove.y];
        }
        if (checkLeftUp(lastMove.x, lastMove.y, points)) {
            return points[lastMove.x][lastMove.y];
        }
        if (checkLeftDown(lastMove.x, lastMove.y, points)) {
            return points[lastMove.x][lastMove.y];
        }
        if (moveSize == Roles.LEN * Roles.LEN) {
            return Roles.ERROR;
        }
        return Roles.ING;
    }

    private static boolean isSameSide(int[][] qzs, int color, int x, int y) {
        if (x < 0 || x > 14 || y < 0 || y > 14) {
            return false;
        }
        if (qzs[x][y] == Roles.EMPTY) {
            return false;
        }
        return qzs[x][y] == color;
    }

    /**
     * 横向检查
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private static boolean checkHorizontal(int x, int y, int[][] points) {
        int color = points[x][y];
        int count = 1;
        //左
        for (int i = 1; i < 5; i++) {//记住i要从1开始才行
            if (isSameSide(points, color, x - i, y)) {
                count++;
                continue;
            }
            break;
        }
        if (count >= 5) {
            return true;
        }
        //右
        for (int i = 1; i < 5; i++) {//记住i要从1开始才行
            if (isSameSide(points, color, x + i, y)) {
                count++;
                continue;
            }
            break;
        }
        if (count >= 5) {
            return true;
        }
        return false;
    }

    private static boolean checkVertical(int x, int y, int[][] points) {
        int color = points[x][y];
        int count = 1;
        //上
        for (int i = 1; i < 5; i++) {
            if (isSameSide(points, color, x, y - i)) {
                count++;
                continue;
            }
            break;
        }
        if (count >= 5) {
            return true;
        }
        //下
        for (int i = 1; i < 5; i++) {
            if (isSameSide(points, color, x, y + i)) {
                count++;
                continue;
            }
            break;
        }
        if (count >= 5) {
            return true;
        }
        return false;
    }

    private static boolean checkLeftUp(int x, int y, int[][] points) {
        int color = points[x][y];
        int count = 1;
        //左
        for (int i = 1; i < 5; i++) {
            if (isSameSide(points, color, x - i, y + i)) {
                count++;
                continue;
            }
            break;
        }
        if (count >= 5) {
            return true;
        }
        //右
        for (int i = 1; i < 5; i++) {
            if (isSameSide(points, color, x + i, y - i)) {
                count++;
                continue;
            }
            break;
        }
        if (count >= 5) {
            return true;
        }
        return false;
    }

    private static boolean checkLeftDown(int x, int y, int[][] points) {
        int color = points[x][y];
        int count = 1;
        //左
        for (int i = 1; i < 5; i++) {
            if (isSameSide(points, color, x - i, y - i)) {
                count++;
                continue;
            }
            break;
        }
        if (count >= 5) {
            return true;
        }
        //右
        for (int i = 1; i < 5; i++) {
            if (isSameSide(points, color, x + i, y + i)) {
                count++;
                continue;
            }
            break;
        }
        if (count >= 5) {
            return true;
        }
        return false;
    }
}
