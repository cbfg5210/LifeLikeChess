package com.ue.ichess.entity;


import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

import static com.ue.ichess.entity.ChessID.EMPTY;

/**
 * Reference:
 * Author:
 * Date:2016/9/11.
 */
public class ChessRecord extends IChessRecord {
    public int fromF;
    public int fromX;
    public int fromY;
    public int toF;
    public int toX;
    public int toY;
    public boolean isExchange;
    public boolean isPawnPromoted;

    public void setFXY(boolean isFrom, int f, int x, int y) {
        if (isFrom) {
            this.fromF = f;
            this.fromX = x;
            this.fromY = y;
            return;
        }
        if (f < 0) {
            this.toF = -f;
        } else if (f > EMPTY) {
            this.toF = f - EMPTY;
        } else {
            this.toF = f;
        }
        this.toX = x;
        this.toY = y;
    }

    public void setExchange(boolean isExchange) {
        this.isExchange = isExchange;
    }

    public void setPawnPromoted(boolean pawnPromoted) {
        isPawnPromoted = pawnPromoted;
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}