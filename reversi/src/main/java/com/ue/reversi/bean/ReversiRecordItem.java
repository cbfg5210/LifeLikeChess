package com.ue.reversi.bean;

import android.graphics.Point;

import com.ue.resource.util.GsonHolder;

import java.util.List;

/**
 * Created by hawk on 2016/11/27.
 */

public class ReversiRecordItem {
    public int x;
    public int y;
    public List<Point> changedChess;

    public ReversiRecordItem(int x, int y, List<Point> changedChess) {
        this.x = x;
        this.y = y;
        this.changedChess = changedChess;
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
