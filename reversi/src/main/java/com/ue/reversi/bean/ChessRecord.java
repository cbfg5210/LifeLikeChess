package com.ue.reversi.bean;

import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hawk on 2017/5/13.
 */

public class ChessRecord extends IChessRecord {
    public byte c;
    public boolean isMyTurn;
    public List<ReversiRecordItem> mRecordItems;

    public ChessRecord(byte color, boolean isMyTurn) {
        this.c = color;
        this.isMyTurn = isMyTurn;
        this.mRecordItems = new ArrayList<>();
    }

    public void addRecordItem(ReversiRecordItem recordItem) {
        mRecordItems.add(recordItem);
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
