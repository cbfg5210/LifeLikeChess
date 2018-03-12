package com.ue.cchess.util;

import android.util.SparseArray;

import com.ue.cchess.entity.ChessID;

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
 * Reference:
 * Author:
 * Date:2016/9/11.
 */
public class ChessUtils {
    private static SparseArray<String> chessNames;

    public static int[][] getChessBoard(boolean isMyFirst) {
        if (isMyFirst) {
            return new int[][]{
                    {B_CHARIOT, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, R_CHARIOT},
                    {B_HORSE, EMPTY, B_CANNON, EMPTY, EMPTY, EMPTY, EMPTY, R_CANNON, EMPTY, R_HORSE},
                    {B_ELEPHANT, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, R_ELEPHANT},
                    {B_GUARD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, R_GUARD},
                    {B_GENERAL, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, R_GENERAL},
                    {B_GUARD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, R_GUARD},
                    {B_ELEPHANT, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, R_ELEPHANT},
                    {B_HORSE, EMPTY, B_CANNON, EMPTY, EMPTY, EMPTY, EMPTY, R_CANNON, EMPTY, R_HORSE},
                    {B_CHARIOT, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, R_CHARIOT}
            };
        }
        return new int[][]{
                {R_CHARIOT, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, B_CHARIOT},
                {R_HORSE, EMPTY, R_CANNON, EMPTY, EMPTY, EMPTY, EMPTY, B_CANNON, EMPTY, B_HORSE},
                {R_ELEPHANT, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, B_ELEPHANT},
                {R_GUARD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, B_GUARD},
                {R_GENERAL, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, B_GENERAL},
                {R_GUARD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, B_GUARD},
                {R_ELEPHANT, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, B_ELEPHANT},
                {R_HORSE, EMPTY, R_CANNON, EMPTY, EMPTY, EMPTY, EMPTY, B_CANNON, EMPTY, B_HORSE},
                {R_CHARIOT, EMPTY, EMPTY, R_SOLDIER, EMPTY, EMPTY, B_SOLDIER, EMPTY, EMPTY, B_CHARIOT},
        };
    }

    /**
     * @param chessId
     * @return
     */
    public static String getChessName(int chessId) {
        if (chessNames == null) {
            chessNames = new SparseArray<>();
            chessNames.put(R_CHARIOT, "車");
            chessNames.put(B_CHARIOT, "車");
            chessNames.put(B_HORSE, "馬");
            chessNames.put(R_HORSE, "馬");
            chessNames.put(B_ELEPHANT, "象");
            chessNames.put(R_ELEPHANT, "相");
            chessNames.put(B_GUARD, "士");
            chessNames.put(R_GUARD, "仕");
            chessNames.put(B_GENERAL, "将");
            chessNames.put(R_GENERAL, "帥");
            chessNames.put(B_SOLDIER, "卒");
            chessNames.put(R_SOLDIER, "兵");
            chessNames.put(B_CANNON, "炮");
            chessNames.put(R_CANNON, "炮");
        }
        return chessNames.get(chessId);
    }

    public static boolean isMyChess(int chessId, boolean isMyFirst) {
        if (isMyFirst) {
            return chessId < 8;
        }
        if (chessId == ChessID.EMPTY) {
            return false;
        }
        return chessId > 7;
    }

    public static boolean isSameSide(int fromF, int toF) {
        if (fromF < 8) {
            return toF < 8;
        }
        if (toF == ChessID.EMPTY) {
            return false;
        }
        return toF > 7;
    }

    public static boolean isBlack(int chessId) {
        return (chessId >= B_GENERAL && chessId <= B_SOLDIER);
    }

    public static boolean isRed(int chessId) {
        return (chessId >= R_GENERAL && chessId <= R_SOLDIER);
    }
}