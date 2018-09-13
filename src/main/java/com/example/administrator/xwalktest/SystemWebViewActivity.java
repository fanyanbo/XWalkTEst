package com.example.administrator.xwalktest;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import org.apache.cordova.CordovaActivity;


/**
 * Created by fanyanbo on 2018/8/17.
 * Email: fanyanbo@skyworth.com
 */
public class SystemWebViewActivity extends CordovaActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getStringExtra("url");
        if(url == null) {
            url = "https://www.baidu.com";
        }
        Log.i("WebViewSDK", "onCreate getUrl = " + url);
        loadUrl(url);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i("WebViewSDK","onKeyDown keyCode = " + event.getKeyCode() + ",event = " + event.getAction());
        if(event.getKeyCode() == 8 && event.getAction() == 1)
            loadUrl("https://www.baidu.com");
        return super.dispatchKeyEvent(event);
    }

}
