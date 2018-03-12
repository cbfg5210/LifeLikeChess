package com.ue.resource.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.ue.resource.model.GameRecord;

/**
 * Created by hawk on 2017/9/28.
 */
@Database(entities = {GameRecord.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract GameRecordDao gameRecordDao();

    /**
     * 版本迁移：http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0728/8278.html
     * <p>
     * 简单迁移、复杂迁移、多版本迁移
     */

    /*
    //版本2到3的迁移
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users "
                    + " ADD COLUMN last_update INTEGER");
        }
    };*/
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "blueLightFilter.db")
                            .fallbackToDestructiveMigration()//更新版本之后清空数据库
                            //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)//版本迁移
                            .build();
                }
            }
        }
        return instance;
    }
}