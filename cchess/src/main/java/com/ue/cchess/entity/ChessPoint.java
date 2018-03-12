package com.ue.cchess.entity;

import com.ue.resource.util.GsonHolder;

/*
 * 这是一个棋子类
 * */
public class ChessPoint {
    public int f;//棋子标识
    public int x;
    public int y;

    public ChessPoint(int f, int x, int y) {
        this.f = f;
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        return f;
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}