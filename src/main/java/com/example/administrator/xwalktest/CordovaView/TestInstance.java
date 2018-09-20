package com.example.administrator.xwalktest.CordovaView;

import android.content.Context;
import android.util.Log;

/**
 * Created by tianjisheng on 2018/6/26.
 */

public class TestInstance
{
    private static TestInstance instance = null;

    public static TestInstance getInstance()
    {
        if (instance == null)
        {
            instance = new TestInstance();
        }
        return instance;
    }

    private CordovaViewApi api = null;

    public void cacheUrl(Context context, String url)
    {
        if (api != null) {
            api.cleanUp();
        }
        api = new CordovaViewApi(context, context.getPackageName(), "coocaa.intent.action.browser.cache_web_service", new CordovaViewApi.CordovaViewCacheInterface()
            {
                @Override
                public void onAddCacheWebViewPool(boolean isStart)
                {
                    Log.i("cordova_view", "onAddCacheWebViewPool:" + isStart);
                }

                @Override
                public void onLoadStart(String url)
                {
                    Log.i("cordova_view", "onLoadStart:" + url);
                }

                @Override
                public void onLoadError(int errorCode, String description, String failingUrl)
                {
                    Log.i("cordova_view", "onLoadError:errorCode=" + errorCode + ",description=" + description + ",url=" + failingUrl);
                }

                @Override
                public void onLoadFinished(String url)
                {
                    Log.i("cordova_view", "onLoadFinished:" + url);
                }

                @Override
                public boolean canShowUrl(String url)
                {
                    return true;
                }

                @Override
                public void showWebView(int code)
                {
                    Log.i("cordova_view", "showWebView:" + code);

                }
            });

        api.cacheWebView(url);

//        if (api == null)
//        {
//            api = new CordovaViewApi(context, context.getPackageName(), "coocaa.intent.action.browser.cache_web_service", new CordovaViewApi.CordovaViewCacheInterface()
//            {
//                @Override
//                public void onAddCacheWebViewPool(boolean isStart)
//                {
//                    Log.i("cordova_view", "onAddCacheWebViewPool:" + isStart);
//                }
//
//                @Override
//                public void onLoadStart(String url)
//                {
//                    Log.i("cordova_view", "onLoadStart:" + url);
//                }
//
//                @Override
//                public void onLoadError(int errorCode, String description, String failingUrl)
//                {
//                    Log.i("cordova_view", "onLoadError:errorCode=" + errorCode + ",description=" + description + ",url=" + failingUrl);
//                }
//
//                @Override
//                public void onLoadFinished(String url)
//                {
//                    Log.i("cordova_view", "onLoadFinished:" + url);
//                }
//
//                @Override
//                public boolean canShowUrl(String url)
//                {
//                    return true;
//                }
//
//                @Override
//                public void showWebView(int code)
//                {
//                    Log.i("cordova_view", "showWebView:" + code);
//
//                }
//            });
//        }
//        if (url.equals("clear"))
//        {
//            api.cleanUp();
//        }else
//        {
//            api.cacheWebView(url);
//        }
    }

}
