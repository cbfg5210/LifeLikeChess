package com.ue.cchess.entity;

import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

/**
 * 移动棋子的一步走法类
 */
public class ChessRecord extends IChessRecord {
    public int fromX;
    public int fromY;
    public int fromF;
    public int toX;
    public int toY;
    public int toF;

    public void setFXY(boolean isFrom, int f, int x, int y) {
        if (isFrom) {
            this.fromX = x;
            this.fromY = y;
            this.fromF = f;
        } else {
            this.toX = x;
            this.toY = y;
            this.toF = f;
        }
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}