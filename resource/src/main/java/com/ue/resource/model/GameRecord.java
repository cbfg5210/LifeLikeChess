package com.ue.resource.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by hawk on 2017/11/2.
 */
@Entity
public class GameRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public int gameFlag;
    public int gameMode;
    public String data;

    public GameRecord() {
    }

    public GameRecord(IChessRecord lastMove, int gameFlag, int gameMode) {
        this.id = lastMove.id;
        this.gameFlag = gameFlag;
        this.gameMode = gameMode;
        this.data = lastMove.toString();
    }
}
