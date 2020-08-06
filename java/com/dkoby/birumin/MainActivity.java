/*
 * Jaro 2020.
 * Auxtoro estas Dmitrij Kobilin. 
 *
 * Nenia rajtigilo ekzistas.
 * Faru bone, ne faru malbone.
 */
package com.dkoby.birumin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;

/*
 *
 */
public class MainActivity extends Activity
{
    public static final String VERSION = "BIRUMINv1";
    public static final String TAG = "BiruminLog";
    private WebView webView;
    private MainMsgHandler msgHandler;
    public Track currentTrack;

    public Handler getMsgHandler() {
        return msgHandler;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        msgHandler = new MainMsgHandler();

//        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 1/* 1 to 255 */);

        /* Disable screen orientation change. */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        currentTrack = new Track(MainActivity.this);

        webView = new WebView(this);

        webView.setWebViewClient(new MyWebViewClient());

        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG, cm.message() + " -- From line "
                    + cm.lineNumber() + " of "
                    + cm.sourceId() );
                return true;
            }
        });

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(this), "android");

        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);
//        (new Thread((new TimeThread()))).start();
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(TAG, "onStart");
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume");
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, "onPause");
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.i(TAG, "onStop");
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    /*
     *
     */
    public void sendMessage(MainMessage mainMessage)
    {
        Message msg;

        msg = getMsgHandler().obtainMessage();
        msg.obj = mainMessage;
        getMsgHandler().sendMessage(msg);
    }
    /*
     *
     */
    private class MainMsgHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            MainMessage mainMessage;

//            Log.i(TAG, "Handle: " + msg);

            mainMessage = (MainMessage)msg.obj;
            switch (mainMessage.msgType)
            {
                case SCREEN_ON:
                    boolean value;

                    value = ((Boolean)mainMessage.obj).booleanValue();
                    getWindow().setFlags(
                            value ? WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON : 0,
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                case TRACK_START:
                    currentTrack.start();
                    break;
                case TRACK_PAUSE:
                    currentTrack.pause();
                    break;
                case TRACK_RESUME:
                    currentTrack.resume();
                    break;
                case TRACK_STOP:
                    currentTrack.stop();
                    break;
                case TRACK_UPDATE:
                    if (currentTrack.state == Track.State.CANCEL ||
                        currentTrack.state == Track.State.DONE)
                        currentTrack = new Track(MainActivity.this);
                    webView.loadUrl("javascript:app.statusUpdate()");
                    break;
            }
        }
    }
}
/*
 *
 */
class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }
}
/*
 *
 */
class WebAppInterface {
    private MainActivity mainActivity;

    /*
     *
     */
    WebAppInterface(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    @JavascriptInterface
    public String getStatus() {
        JSON json = new JSON();
        JSON jt = new JSON();

        if (mainActivity.currentTrack.locationProvider != null)
            jt.append("provider", mainActivity.currentTrack.locationProvider);
        else
            jt.append("provider", "-");

        jt.append("state", mainActivity.currentTrack.state.toString());
        jt.append("movingTime", mainActivity.currentTrack.movingTime);
        jt.append("elapsedTime", mainActivity.currentTrack.movingTime);
        jt.append("elevation", mainActivity.currentTrack.elevation);
        Track.Point point = mainActivity.currentTrack.getLastPoint();
        if (point != null)
        {
            jt.append("latitude", point.latitude);
            jt.append("longitude", point.longitude);
            jt.append("altitude", point.altitude);
            jt.append("speed", point.speed);
            jt.append("time", point.time);
        }
        jt.append("points", mainActivity.currentTrack.getPointsNum());

        json.append("track", jt);
        return json.toString();
    }
    @JavascriptInterface
    public void startTrack() {
        mainActivity.sendMessage(
            new MainMessage(
                MainMessage.MsgType.TRACK_START)
        );
    }
    @JavascriptInterface
    public void pauseTrack() {
        mainActivity.sendMessage(
            new MainMessage(
                MainMessage.MsgType.TRACK_PAUSE)
        );
    }
    @JavascriptInterface
    public void resumeTrack() {
        mainActivity.sendMessage(
            new MainMessage(
                MainMessage.MsgType.TRACK_RESUME)
        );
    }
    @JavascriptInterface
    public void stopTrack() {
        mainActivity.sendMessage(
            new MainMessage(
                MainMessage.MsgType.TRACK_STOP)
        );
    }
    @JavascriptInterface
    public void keepScreenOn(boolean value) {
        mainActivity.sendMessage(new MainMessage(
                MainMessage.MsgType.SCREEN_ON, new Boolean(value)
        ));
    }
    /*
     *
     */
    private class JSON {
        private StringBuilder sb;
        private String dot;
        public JSON() {
            sb = new StringBuilder("{");
            dot = "";
        }
        public void append(String name, JSON json) {
            sb.append(dot);
            sb.append("\"");
            sb.append(name);
            sb.append("\"");
            sb.append(": ");
            sb.append(json.toString());
            dot = ",";
        }
        public void append(String name, String value) {
            sb.append(dot);
            sb.append("\"");
            sb.append(name);
            sb.append("\"");
            sb.append(": ");
            sb.append("\"");
            sb.append(value);
            sb.append("\"");
            dot = ",";
        }
        public void append(String name, int value) {
            sb.append(dot);
            sb.append("\"");
            sb.append(name);
            sb.append("\"");
            sb.append(": ");
            sb.append(value);
            dot = ",";
        }
        public void append(String name, long value) {
            sb.append(dot);
            sb.append("\"");
            sb.append(name);
            sb.append("\"");
            sb.append(": ");
            sb.append(value);
            dot = ",";
        }
        public void append(String name, double value) {
            sb.append(dot);
            sb.append("\"");
            sb.append(name);
            sb.append("\"");
            sb.append(": ");
            sb.append(value);
            dot = ",";
        }
        public void append(String name, float value) {
            sb.append(dot);
            sb.append("\"");
            sb.append(name);
            sb.append("\"");
            sb.append(": ");
            sb.append(value);
            dot = ",";
        }
        public String toString() {
            return sb.append("}").toString();
        }
    }
    /*
     *
     */
    private String jsonString(String name, String value) {
        return name + "\"" + value + "\"";
    }
}

