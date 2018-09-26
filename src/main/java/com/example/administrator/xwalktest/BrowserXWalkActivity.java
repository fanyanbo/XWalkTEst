package com.example.administrator.xwalktest;

import android.content.Intent;
import android.os.Bundle;

import com.tianci.media.api.Log;

import org.apache.cordova.CordovaExtActivity;

import java.util.Map;

/**
 * Created by fanyanbo on 2018/9/26.
 * Email: fanyanbo@skyworth.com
 */
public class BrowserXWalkActivity extends CordovaExtActivity
        implements CordovaExtActivity.CordovaWebPageListener,
        CordovaExtActivity.CordovaWebViewListener
{

    private static final String TAG = "WebViewSDK";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "BrowserXWalkActivity onCreate");
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra("url");

//        setUserAgentMode(1);
        setCore(1);
        this.setCordovaWebPageListener(this);
        this.setCordovaWebViewListener(this);
        loadUrl(url);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

    @Override
    public void onCmdConnectorInit()
    {
        super.onCmdConnectorInit();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onPageStarted(String url) {

    }

    @Override
    public void onPageFinished(String url) {

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void notifyMessage(String data) {

    }

    @Override
    public void notifyLogInfo(String eventId, Map<String, String> map) {

    }

    @Override
    public void notifyPageResume(String eventId, Map<String, String> map) {

    }

    @Override
    public void notifyPagePause(String eventId) {

    }
}
