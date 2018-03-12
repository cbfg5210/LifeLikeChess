package com.ue.ichess.entity;

/**
 * Created by hawk on 2017/11/7.
 */

/**
 * 不能轻易改变，可能造成不同版本间的不适配
 */
public interface ChessID {
    int EMPTY = 20;

    int W_PAWN = 1;
    int W_KING = 2;
    int W_ROOK = 3;
    int W_PAWN_N = 4;
    int W_KING_N = 5;
    int W_ROOK_N = 6;
    int W_BISHOP = 7;
    int W_KNIGHT = 8;
    int W_QUEEN = 9;

    int B_PAWN = 10;
    int B_KING = 11;
    int B_ROOK = 12;
    int B_PAWN_N = 13;
    int B_KING_N = 14;
    int B_ROOK_N = 15;
    int B_BISHOP = 16;
    int B_KNIGHT = 17;
    int B_QUEEN = 18;
}
