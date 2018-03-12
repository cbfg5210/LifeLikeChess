package com.ue.moon.entity;

import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

import java.util.List;

/**
 * Created by hawk on 2016/12/14.
 */

public class ChessRecord extends IChessRecord {
    public int fromF;
    public int fromC;
    public int toF;
    public List<Integer> killedFs;

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
