package com.example.administrator.xwalktest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.xwalk.core.*;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class XWalkWebViewActivity extends AppCompatActivity
        implements XWalkInitializer.XWalkInitListener,
        XWalkUpdater.XWalkBackgroundUpdateListener{

    XWalkInitializer mXWalkInitializer;
    XWalkUpdater mXWalkUpdater = null;
    XWalkView mXWalkView;
    FrameLayout mMainLayout = null;
    private static final String TAG = "QJY";
    private String url = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        mXWalkInitializer = new XWalkInitializer(this, this);
        mXWalkInitializer.initAsync();

//        setContentView(R.layout.activity_main);
//        mXWalkView = (XWalkView) findViewById(R.id.xwalk);

        mMainLayout = new FrameLayout(this);
        mMainLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        mXWalkView = new XWalkView(this);
        mXWalkView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        mMainLayout.addView(mXWalkView);
        setContentView(mMainLayout);

        url = getIntent().getStringExtra("url");
        if(url == null) {
            url = "https://www.baidu.com";
        }
        Log.i(TAG, "onCreate getUrl = " + url);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        mXWalkInitializer.initAsync();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onXWalkInitStarted() {
        Log.i(TAG, "onXWalkInitStarted");
    }

    @Override
    public void onXWalkInitCancelled() {

        Log.i(TAG, "onXWalkInitCancelled");
        finish();
    }

    @Override
    public void onXWalkInitFailed() {
        // Initialization failed. Trigger the Crosswalk runtime download
        if (mXWalkUpdater == null) {
            mXWalkUpdater = new XWalkUpdater(this, this);
        }
//        mXWalkUpdater.updateXWalkRuntime();
        Log.i(TAG, "onXWalkInitFailed");
    }

    @Override
    public void onXWalkInitCompleted() {

        Log.i(TAG, "onXWalkInitCompleted");
        // Initialization successfully, ready to invoke any XWalk embedded API
        mXWalkView.load(url, null);
        mXWalkView.clearCache(true);
        mXWalkView.setDrawingCacheEnabled(false);

    }

    @Override
    public void onXWalkUpdateStarted() {
        Log.i(TAG, "onXWalkUpdateStarted");
    }

    @Override
    public void onXWalkUpdateProgress(int percentage) {
        //Log.d(TAG, "XWalkUpdate progress: " + percentage);
        Log.i(TAG, "onXWalkUpdateProgress progress = " + percentage);
    }

    @Override
    public void onXWalkUpdateCancelled() {
        Log.i(TAG, "onXWalkUpdateCancelled");
        finish();
    }

    @Override
    public void onXWalkUpdateFailed() {
        finish();
    }

    @Override
    public void onXWalkUpdateCompleted() {
        // Crosswalk Runtime update finished, re-init again.
        Log.i(TAG, "onXWalkUpdateCompleted");
//        mXWalkInitializer.initAsync();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
}
