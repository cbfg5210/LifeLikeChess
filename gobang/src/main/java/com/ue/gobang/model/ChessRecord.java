package com.ue.gobang.model;

import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

/**
 * Created by hawk on 2017/12/5.
 */

public class ChessRecord extends IChessRecord {
    public int x;
    public int y;
    public boolean isMyTurn;

    public ChessRecord(int x, int y, boolean isMyTurn) {
        this.x = x;
        this.y = y;
        this.isMyTurn = isMyTurn;
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
