package com.czf.nativeapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteDBHelper extends SQLiteOpenHelper {

    public MySQLiteDBHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("-----", "sql helper onCreate"); // 创建数据库的时候才会调用，不创建不调用
        createTableIfNeed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("-----", "sql helper onUpgrade oldV:" + oldVersion + ", newV:" + newVersion);
    }

    private void createTableIfNeed(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS" + " [mytable] " + "([_id] INTEGER PRIMARY KEY AUTOINCREMENT, [name] VARCHAR(20), [age] INTEGER);");
    }

}
