package com.czf.nativeapp;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.czf.nativeapp.db.MyNameTable;
import com.czf.nativeapp.db.MySQLiteDBHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static Handler nativeHandler;
    private static long nativePtr;
    private ServiceConnection myServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("---------", "onServiceConnected" + service);
            Parcel toData = Parcel.obtain();
            toData.writeString("-----from main activity");
            Parcel replyData = Parcel.obtain(); // c style
            try {
                boolean result = service.transact(7, toData, replyData, 0);
                Log.d("----", "result:" + result + ", replayData:" + replyData.readString());
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                toData.recycle();
                replyData.recycle();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("---------", "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        findViewById(R.id.show_native_thread_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nativeHandler != null) {
                    nativeHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Thread t = Thread.currentThread();
                            Log.d("-----", "name: " + t.getName() + ", tid: " + t.getId());
                        }
                    });
                }
            }
        });
        findViewById(R.id.start_new_mq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNativeMQ();
            }
        });
        findViewById(R.id.quit_mq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nativeHandler != null) {
                    nativeHandler.getLooper().quit();
                    nativeHandler = null;
                }
            }
        });
        findViewById(R.id.send_msg_2_native).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nativeHandler == null) return;
                sendMsg2Native(nativeHandler, null);
            }
        });
        findViewById(R.id.sleep_1_s).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SystemClock.sleep(1000);
                Log.d("--==---", "start");
                for (int i = 0; i < 100; i++) {
                    Object o = MainActivity.this.getJavaObjFromNative();
                }
                Log.d("--==---", "end");
            }
        });
        findViewById(R.id.handle_data_from_native).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] iArr = {1, 2, 3};
                boolean isCopy = handleDataFromNative(iArr);
                Log.d("---------", iArr[0] + "-" + iArr[1] + "-" + iArr[2]);
                Log.d("---------", "isCopy: " + isCopy);
            }
        });
        findViewById(R.id.start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myService = new Intent();
                myService.setClassName(MainActivity.this,
                        "com.czf.nativeapp.MyService");
//                startService(myService);
                bindService(myService, myServiceConn, BIND_AUTO_CREATE);
            }
        });
        findViewById(R.id.stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent myService = new Intent();
//                myService.setClassName(MainActivity.this, "com.czf.nativeapp.MyService");
//                stopService(myService);
                unbindService(myServiceConn);
            }
        });
        findViewById(R.id.open_sqlite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySQLiteDBHelper h = new MySQLiteDBHelper(getApplicationContext());
                SQLiteDatabase db = h.getWritableDatabase();
                Log.d("-------", "db open: " + db.toString());
                //db.execSQL("CREATE ");
            }
        });
        findViewById(R.id.insert_one_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySQLiteDBHelper h = new MySQLiteDBHelper(getApplicationContext());
                SQLiteDatabase db = h.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(MyNameTable.columnName, "chenzhifei");
                cv.put(MyNameTable.columnAge, 29);
                cv.put(MyNameTable.columnAdrress, "America");
                db.insert(MyNameTable.name, null, cv);
            }
        });
        findViewById(R.id.query_one_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySQLiteDBHelper h = new MySQLiteDBHelper(getApplicationContext());
                SQLiteDatabase db = h.getWritableDatabase();
                Cursor c = db.query(MyNameTable.name, null, MyNameTable.columnName +"=?",
                        new String[]{"chenzhifei"}, null, null, null);
                Log.d("-----------", "num: " + c.getCount());
                while (c.moveToNext()) {
                    Log.d("----", c.getString(c.getColumnIndex(MyNameTable.columnName)));
                    Log.d("----", c.getInt(c.getColumnIndex(MyNameTable.columnAge)) + "");
                    Log.d("----", c.getString(c.getColumnIndex(MyNameTable.columnAdrress)));
                }
                c.close();
            }
        });
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySQLiteDBHelper h = new MySQLiteDBHelper(getApplicationContext());
                SQLiteDatabase db = h.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(MyNameTable.columnAge, 28);
                db.update(MyNameTable.name, cv, MyNameTable.columnName + "=?",
                        new String[]{"chenzhifei"});
            }
        });
        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySQLiteDBHelper h = new MySQLiteDBHelper(getApplicationContext());
                SQLiteDatabase db = h.getWritableDatabase();
                db.delete(MyNameTable.name, MyNameTable.columnName + "=?",
                        new String[]{"chenzhifei"});
            }
        });
        findViewById(R.id.fork_linux_proc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewLinuxProc();
            }
        });
    }

    /**
     * will be called from native side
     */
    public static void setNativePtr(long ptr) {
        nativePtr = ptr;
    }

    /**
     * will be called from native side
     */
    public static void startNewMessageQueue() {
        Looper.prepare();
        nativeHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.d("handle message", msg.toString());
                nativeRun(nativePtr);
                return true;
            }
        });
        Looper.loop();
    }

    /**
     * will be called from native side
     */
    public static void showJavaThreadInfo(int flag) {
        Thread t = Thread.currentThread();
        Log.d("----java---- " + flag, "name: " + t.getName() + ", tid: " + t.getId());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void startNativeMQ();

    public native void sendMsg2Native(Handler nativeHandler, String msg);

    /**
     * will be called from native side
     */
    public static void nativeLog(String tag, String log) {
        Log.d(tag, log);
    }

    public native static void nativeRun(long nativeFnPtr);

    public native Object getJavaObjFromNative();

    public native boolean handleDataFromNative(int[] intArr);

    public native void startNewLinuxProc();

}
