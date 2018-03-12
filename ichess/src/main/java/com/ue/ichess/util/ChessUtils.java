package com.ue.ichess.util;

import android.util.SparseArray;

import com.ue.ichess.R;

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
 * Reference:
 * Author:
 * Date:2016/9/11.
 */
public final class ChessUtils {
    public static final int FLAG_EXCHANGE = 111;
    private static SparseArray<Integer> chessImages;

    public static int[][] getChessBoard(boolean isMyFirst, boolean isReversed) {
        if (isMyFirst) {
            return new int[][]{
                    {B_ROOK, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, W_ROOK},
                    {B_KNIGHT, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, W_KNIGHT},
                    {B_BISHOP, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, W_BISHOP},
                    {B_QUEEN, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, W_QUEEN},
                    {B_KING, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, W_KING},
                    {B_BISHOP, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, W_BISHOP},
                    {B_KNIGHT, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, W_KNIGHT},
                    {B_ROOK, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, W_ROOK}
            };
        }
        if (isReversed) {
            return new int[][]{
                    {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
                    {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
                    {W_BISHOP, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_BISHOP},
                    {W_KING, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
                    {W_QUEEN, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_QUEEN},
                    {W_BISHOP, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_BISHOP},
                    {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
                    {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK}
            };
        }
        return new int[][]{
                {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
                {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
                {W_BISHOP, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_BISHOP},
                {W_QUEEN, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_QUEEN},
                {W_KING, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
                {W_BISHOP, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_BISHOP},
                {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
                {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK}
        };
    }

    public static int getChessImage(int chessId) {
        if (chessImages == null) {
            chessImages = new SparseArray<>();
            chessImages.put(W_PAWN, R.mipmap.ic_wpawn);
            chessImages.put(W_PAWN_N, R.mipmap.ic_wpawn);
            chessImages.put(B_PAWN, R.mipmap.ic_bpawn);
            chessImages.put(B_PAWN_N, R.mipmap.ic_bpawn);
            chessImages.put(W_BISHOP, R.mipmap.ic_wbishop);
            chessImages.put(B_BISHOP, R.mipmap.ic_bbishop);
            chessImages.put(W_KING, R.mipmap.ic_wking);
            chessImages.put(W_KING_N, R.mipmap.ic_wking);
            chessImages.put(B_KING, R.mipmap.ic_bking);
            chessImages.put(B_KING_N, R.mipmap.ic_bking);
            chessImages.put(W_KNIGHT, R.mipmap.ic_wknight);
            chessImages.put(B_KNIGHT, R.mipmap.ic_bknight);
            chessImages.put(W_QUEEN, R.mipmap.ic_wqueen);
            chessImages.put(B_QUEEN, R.mipmap.ic_bqueen);
            chessImages.put(W_ROOK, R.mipmap.ic_wrook);
            chessImages.put(W_ROOK_N, R.mipmap.ic_wrook);
            chessImages.put(B_ROOK, R.mipmap.ic_brook);
            chessImages.put(B_ROOK_N, R.mipmap.ic_brook);
        }
        return chessImages.get(chessId);
    }

    public static boolean isPawn(int chessId) {
        if (chessId == W_PAWN) {
            return true;
        }
        if (chessId == W_PAWN_N) {
            return true;
        }
        if (chessId == B_PAWN) {
            return true;
        }
        if (chessId == B_PAWN_N) {
            return true;
        }
        return false;
    }

    public static boolean isKingFirstMove(int kingId) {
        if (kingId == W_KING) {
            return true;
        }
        if (kingId == B_KING) {
            return true;
        }
        return false;
    }

    public static boolean isRookFirstMove(int rookId) {
        if (rookId == W_ROOK) {
            return true;
        }
        if (rookId == B_ROOK) {
            return true;
        }
        return false;
    }

    public static boolean isKing(int chessId) {
        if (chessId == W_KING) {
            return true;
        }
        if (chessId == W_KING_N) {
            return true;
        }
        if (chessId == B_KING) {
            return true;
        }
        if (chessId == B_KING_N) {
            return true;
        }
        return false;
    }

    public static boolean isMyChess(int chessId, boolean isMyFirst) {
        if (isMyFirst) {
            return chessId < 10;
        }
        return chessId > 9;
    }

    public static boolean isPawnFirstMove(int pawnId) {
        if (pawnId == W_PAWN) {
            return true;
        }
        if (pawnId == B_PAWN) {
            return true;
        }
        return false;
    }

    public static boolean isSameSide(int fromF, int toF) {
        if (fromF < 10) {
            return toF < 10;
        }
        if (toF == EMPTY) {
            return false;
        }
        return toF > 9;
    }
}