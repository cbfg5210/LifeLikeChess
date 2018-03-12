package com.ue.resource.db;

import android.content.Context;

import com.ue.resource.model.GameRecord;
import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2016/11/28.
 */
public class GameRecordManager {
    private GameRecordDao mGameRecordDao;
    private int gameFlag;
    private int gameMode;

    public GameRecordManager(Context context, int gameFlag, int gameMode) {
        this.gameFlag = gameFlag;
        this.gameMode = gameMode;
        this.mGameRecordDao = AppDatabase.getInstance(context).gameRecordDao();
    }

    public void saveChessRecord(IChessRecord lastMove) {
        Observable
                .create(e -> {
                    lastMove.id = mGameRecordDao.saveChessRecord(new GameRecord(lastMove, gameFlag, gameMode));
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .subscribe();
    }

    public Object delToGetLastRecord(IChessRecord lastMove) {
        mGameRecordDao.delete(lastMove.id);
        GameRecord gameRecord = mGameRecordDao.getLastRecord(gameFlag, gameMode);
        if (gameRecord == null) {
            return null;
        }
        IChessRecord chessRecord = GsonHolder.getGson().fromJson(gameRecord.data, lastMove.getClass());
        chessRecord.id = gameRecord.id;
        return chessRecord;
    }

    public Observable<List<IChessRecord>> getChessRecords(Class<? extends IChessRecord> classType) {
        return Observable
                .create((ObservableEmitter<List<IChessRecord>> e) -> {
                    List<GameRecord> gameRecords = mGameRecordDao.getGameRecords(gameFlag, gameMode);
                    if (gameRecords == null) {
                        e.onNext(new ArrayList<>());
                        e.onComplete();
                        return;
                    }
                    List<IChessRecord> chessRecords = new ArrayList<>();
                    for (GameRecord gameRecord : gameRecords) {
                        IChessRecord chessRecord = GsonHolder.getGson().fromJson(gameRecord.data, classType);
                        chessRecord.id = gameRecord.id;
                        chessRecords.add(chessRecord);
                    }
                    e.onNext(chessRecords);
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void clearGameRecords() {
        Observable
                .create(e -> {
                    mGameRecordDao.clearGameRecords(gameFlag, gameMode);
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .subscribe();
    }
}