package com.example.administrator.xwalktest.CordovaView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by tianjisheng on 2018/6/26.
 */

public class CordovaViewTestBroadcast extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i("WebViewSDK","CordovaViewTestBroadcast onReceive");
        TestInstance.getInstance().cacheUrl(context, intent.getStringExtra("url"));
    }
}
