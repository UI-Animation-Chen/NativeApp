package com.czf.nativeapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        // build.gradle中的applicationId不会改变源文件的包名。
        Log.d("------- pacakge: ", getPackageName());
        Log.d("------- class name: ", getClass().getName());
    }

    private void initView() {
        findViewById(R.id.send_msg).setOnClickListener(new View.OnClickListener() {
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
                SystemClock.sleep(1000);
            }
        });
    }

    /**
     * will be called in a native thread.
     */
    public static void startNewMessageQueue() {
        Looper.prepare();
        nativeHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.d("handle message", msg.toString());
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
     * native will call.
     */
    public static void nativeLog(String tag, String log) {
        Log.d(tag, log);
    }

}
