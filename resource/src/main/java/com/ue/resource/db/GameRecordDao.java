package com.ue.resource.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ue.resource.model.GameRecord;

import java.util.List;

/**
 * Created by hawk on 2017/11/2.
 */
@Dao
public interface GameRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveChessRecord(GameRecord gameRecord);

    @Query("delete from GameRecord where id=:id")
    int delete(long id);

    @Query("delete from GameRecord where gameFlag=:gameFlag and gameMode=:gameMode")
    int clearGameRecords(int gameFlag, int gameMode);

    @Query("select * from GameRecord where gameFlag=:gameFlag and gameMode=:gameMode order by id desc limit 1;")
    GameRecord getLastRecord(int gameFlag, int gameMode);

    @Query("select * from GameRecord where gameFlag=:gameFlag and gameMode=:gameMode order by id asc")
    List<GameRecord> getGameRecords(int gameFlag, int gameMode);
}
