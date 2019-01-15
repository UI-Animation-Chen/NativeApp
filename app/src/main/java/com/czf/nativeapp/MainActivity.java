package com.czf.nativeapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static Handler nativeHandler;
    private static long nativePtr;

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
    }

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

}
