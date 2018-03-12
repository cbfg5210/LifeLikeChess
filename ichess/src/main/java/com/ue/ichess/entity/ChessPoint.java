package com.ue.ichess.entity;

/*
 * 这是一个棋子类
 * */
public class ChessPoint {
    public int f;//棋子标识
    public int x;
    public int y;

    public ChessPoint(int f, int x, int y) {
        this.x = x;
        this.y = y;
        this.f = f;
    }

    @Override
    public String toString() {
        return "ChessPoint{" +
                "f=" + f +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}