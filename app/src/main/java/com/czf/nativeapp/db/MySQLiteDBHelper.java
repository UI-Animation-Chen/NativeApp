package com.czf.nativeapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteDBHelper extends SQLiteOpenHelper {

    public MySQLiteDBHelper(Context context) {
        super(context, DBConfig.dbName, null, DBConfig.dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("-----", "sql helper onCreate"); // 创建数据库的时候才会调用，不创建不调用
        createTableIfNeed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("-----", "sql helper onUpgrade oldV:" + oldVersion + ", newV:" + newVersion);
        if (oldVersion < 2) { // 需要升级到2
            db.execSQL("ALTER TABLE " + MyNameTable.name + " ADD COLUMN " + MyNameTable.columnAdrress
                    + " VARCHAR(100) " + "DEFAULT 'China';");
        }
        if (oldVersion < 3) { // 需要升级到3

        }
    }

    private void createTableIfNeed(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS [" + MyNameTable.name +
                "] ([_id] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MyNameTable.columnName + " VARCHAR(20), " +
                MyNameTable.columnAge + " INTEGER, " +
                MyNameTable.columnAdrress + " VARCHAR(50));"
        );
    }

    static class DBConfig {
        static String dbName = "myDbName";
        static int dbVersion = 2;
    }

}
