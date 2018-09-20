package com.example.administrator.xwalktest.CordovaView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tianjisheng on 2018/6/26.
 */

public class CordovaViewApi
{
    private Object lockObject = new Object();
    private final int TIME_OUT = 500;//ms
    private Context applicationContext;
    private volatile ICordovaViewControl binder = null;
    private CordovaViewCacheInterface listener = null;
    private ExecutorService workService = null;
    private String clientName = "";
    private String helpCacheUrlPackageName = "com.coocaa.app_browser";//
    private String helpCacheUrlAction = "coocaa.intent.action.browser.cache_web_service";

    public CordovaViewApi(Context context, String helpCacheUrlPackageName, String helpCacheUrlAction, CordovaViewCacheInterface listener)
    {
        clientName = context.getPackageName() + this.hashCode();
        this.applicationContext = context.getApplicationContext();
        this.helpCacheUrlPackageName = helpCacheUrlPackageName;
        this.helpCacheUrlAction = helpCacheUrlAction;
        this.listener = listener;
    }

    public CordovaViewApi(Context context, CordovaViewCacheInterface listener)
    {
        clientName = context.getPackageName() + this.hashCode();
        this.applicationContext = context.getApplicationContext();
        this.listener = listener;
    }

    public interface CordovaViewCacheInterface
    {
        void onAddCacheWebViewPool(boolean isSuccess);

        void onLoadStart(String url);

        void onLoadError(int errorCode, String description, String failingUrl);

        void onLoadFinished(String url);

        boolean canShowUrl(String url);

        void showWebView(int code);
    }

    public void cleanUp()
    {
        try
        {
            if (binder != null)
            {
                binder.cleanUp(clientName);
            }
            applicationContext.unbindService(serviceConnection);

            if (workService != null)
            {
                workService.shutdownNow();
                workService = null;
            }

            lockObject = null;
            Log.i("cordova_view","binder = " + binder + ",lockObject = " + lockObject);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void cacheWebView(String url)
    {
        _cacheWebView(url, null, null);
    }

    public void cacheWebView(String url, boolean isNeedThemeBg)
    {
        _cacheWebView(url, new Boolean(isNeedThemeBg), null);
    }

    public void cacheWebView(String url, boolean isNeedThemeBg, Map<String, String> header)
    {
        _cacheWebView(url, new Boolean(isNeedThemeBg), header);
    }

    public void cacheWebView(CordovaViewUrlData urlData)
    {
        _cacheWebView(urlData);
    }

    private void _cacheWebView(String url, Boolean isNeedThemeBg, Map<String, String> header)
    {
        CordovaViewUrlData cordovaViewUrlData = new CordovaViewUrlData();
        cordovaViewUrlData.setUrl(url);
        cordovaViewUrlData.setNeedThemeBg(isNeedThemeBg);
        cordovaViewUrlData.setHeader(header);
        _cacheWebView(cordovaViewUrlData);
    }

    private void _cacheWebView(CordovaViewUrlData urlData)
    {
        try
        {
            getWorkService().submit(new CacheRunnable(urlData));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private class CacheRunnable implements Runnable
    {
        private CordovaViewUrlData cordovaViewUrlData = null;

        public CacheRunnable(CordovaViewUrlData cordovaViewUrlData)
        {
            this.cordovaViewUrlData = cordovaViewUrlData;
        }

        @Override
        public void run()
        {
            if (binder == null)
            {
                bindCordovaViewCacheService();
            }

            if (binder == null)
            {
                Log.e("cordova_view","binder == null ");
                listener.onAddCacheWebViewPool(false);//cache 失败
            } else
            {
                try
                {
                    Log.i("cordova_view"," cache url remote  ");
                    binder.cacheWebView(clientName, new RemoteCallback(), cordovaViewUrlData);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    listener.onAddCacheWebViewPool(false);
                }
            }
        }
    }

    private ExecutorService getWorkService()
    {
        if (workService == null || workService.isShutdown())
        {
            workService = Executors.newSingleThreadExecutor();
        }
        return workService;
    }

    private boolean bindCordovaViewCacheService()
    {
        try
        {
            Intent intent = new Intent();
            intent.setAction(helpCacheUrlAction);
            intent.setPackage(helpCacheUrlPackageName);//浏览器 package
            applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            synchronized (lockObject)
            {
                lockObject.wait(TIME_OUT);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.i("cordova_view","onServiceConnected = " + CordovaViewApi.this.hashCode());
            synchronized (lockObject)
            {
                binder = ICordovaViewControl.Stub.asInterface(service);
                synchronized (lockObject)
                {
                    lockObject.notifyAll();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.i("cordova_view","onServiceDisconnected");
            synchronized (lockObject)
            {
                binder = null;
            }
        }
    };

    private class RemoteCallback extends ICordovaViewCallback.Stub
    {

        @Override
        public void onAddCacheWebViewPool(boolean isStart) throws RemoteException
        {
            listener.onAddCacheWebViewPool(isStart);
        }

        @Override
        public void onLoadStart(String url) throws RemoteException
        {
            listener.onLoadStart(url);
        }

        @Override
        public void onLoadError(int errorCode, String description, String failingUrl) throws RemoteException
        {
            listener.onLoadError(errorCode, description, failingUrl);
        }

        @Override
        public void onLoadFinished(String url) throws RemoteException
        {
            listener.onLoadFinished(url);
        }

        @Override
        public boolean canShow(String url) throws RemoteException
        {
            return listener.canShowUrl(url);
        }

        @Override
        public void showWebView(int code) throws RemoteException
        {
            listener.showWebView(code);
        }
    }
}
