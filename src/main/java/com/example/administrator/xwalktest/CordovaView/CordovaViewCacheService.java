package com.example.administrator.xwalktest.CordovaView;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.dataer.api.SkyDataer;
import com.coocaa.dataer.api.ccc.impl.DefaultCoocaaSystemConnecter;
import com.coocaa.dataer.api.event.page.lifecycle.PageProperty;
import com.example.administrator.xwalktest.BuildConfig;
import com.skyworth.framework.skysdk.ipc.SkyContext;
import com.skyworth.framework.skysdk.ipc.SkyService;
import com.umeng.analytics.MobclickAgent;

import org.apache.cordova.CordovaExtWebView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import coocaa.plugin.api.BusinessDataListener;
import coocaa.plugin.api.CoocaaOSConnecter;
import coocaa.plugin.api.CoocaaOSConnecterDefaultImpl;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


/**
 * Created by tianjisheng on 2018/6/25.
 */

public class CordovaViewCacheService extends SkyService implements ICordovaViewCacheService
{
    private static final String TAG = "cordova_view";
    private AtomicBoolean isIpcInitOk = new AtomicBoolean(false);
    private CordovaExtWebView cordovaExtWebView = null;
    private CoocaaOSConnecter coocaaOSConnecter = null;
    private Object lockObject = new Object();
    private final int TIME_OUT = 5 * 1000;//ms
    private Handler mainHandler;
    private ExecutorService workService = null;
    private List<CordovaViewCaller> callbackMap = Collections.synchronizedList(new LinkedList<CordovaViewCaller>());
    private CordovaViewCaller currentCaller = null;
    private long startTime = 0, endTime = 0;

    @Override
    public void onCreate()
    {
        super.onCreate();
        CordovaViewCacheHolder.getInstance().setService(this);
    }

    private Handler getWebService()
    {
        if (mainHandler == null)
        {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    private ExecutorService getWorkService()
    {
        if (workService == null || workService.isShutdown())
        {
            workService = Executors.newSingleThreadExecutor();
        }
        return workService;
    }

    @Override
    public void onCmdConnectorInit()
    {
        isIpcInitOk.set(true);
        SkyDataer.onCore().withContext(getApplicationContext())
                .withDebugMode(BuildConfig.DEBUG).withProductID("App_Browser")
                .withCoocaaSystemConnecter(
                        new DefaultCoocaaSystemConnecter(this)).create();

        final Map<String, String> map = new HashMap<String, String>();
        map.put("channel", "cache");
        submitToUmeng("browser_connector_init", map);
        SkyDataer.onEvent().baseEvent().withEventID("browser_connector_init").withParams(map).submit();

        synchronized (lockObject)
        {
            lockObject.notifyAll();
        }

    }

    private void _addCordovaView(String clientName, ICordovaViewCallback callback, CordovaViewUrlData urlData)
    {
        Log.i(TAG, "_addCordovaView");
        if (callback == null)
        {
            Log.i(TAG, "_addCordovaView error callback == null ");
            final Map<String, String> map = new HashMap<String, String>();
            map.put("desc", "callback");
            submitToUmeng("browser_cache_error", map);
            SkyDataer.onEvent().baseEvent().withEventID("browser_cache_error").withParams(map).submit();
            return;
        }
        if (clientName == null || urlData == null)
        {
            Log.i(TAG, "_addCordovaView error client name == null|| urldata == null ");
            try {
                callback.onAddCacheWebViewPool(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Map<String, String> map = new HashMap<String, String>();
            map.put("desc", "clientNameOrUrlData");
            submitToUmeng("browser_cache_error", map);
            SkyDataer.onEvent().baseEvent().withEventID("browser_cache_error").withParams(map).submit();
            return;
        }
        if (callbackMap.size() >= CordovaViewConst.URL_CACHE_MAX)
        {
            Log.i(TAG, "_addCordovaView error client  size>= " + CordovaViewConst.URL_CACHE_MAX);
            try {
                callback.onAddCacheWebViewPool(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Map<String, String> map = new HashMap<String, String>();
            map.put("desc", "cacheMaxCount");
            submitToUmeng("browser_cache_error", map);
            SkyDataer.onEvent().baseEvent().withEventID("browser_cache_error").withParams(map).submit();
            return;
        }
        IBroswerCacheViewActivity activity = CordovaViewCacheHolder.getInstance().getActivity();
        if (activity != null)
        {//表示当前activity 还没有销毁，不能cache 新的url,一点机会也没有，直接不能cache
            Log.i(TAG, "_addCordovaView activity is Shown ");
            try {
                callback.onAddCacheWebViewPool(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Map<String, String> map = new HashMap<String, String>();
            map.put("desc", "isShown");
            submitToUmeng("browser_cache_error", map);
            SkyDataer.onEvent().baseEvent().withEventID("browser_cache_error").withParams(map).submit();
            return;
        }

        //到队列里面待着吧，如果上一个不能显示，再轮到你
        CordovaViewCacheData cacheData = new CordovaViewCacheData(urlData);
        cacheData.setState(CordovaViewConst.URL_IN_CACHE_LIST);
        CordovaViewCaller caller = new CordovaViewCaller(clientName, cacheData, callback);
        callbackMap.add(caller);
        try {
            callback.onAddCacheWebViewPool(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        _cacheNextUrl();
    }

    private void _cacheWebView(ICordovaViewCallback callback, CordovaViewCaller caller)
    {
        if (!isIpcInitOk.get())
        {
            synchronized (lockObject)
            {
                try
                {
                    lockObject.wait(TIME_OUT);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        if (!isIpcInitOk.get())
        {
            //ipc 异常了
            try {
                callback.onAddCacheWebViewPool(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Map<String, String> map = new HashMap<String, String>();
            map.put("desc", "ipcError");
            submitToUmeng("browser_cache_error", map);
            SkyDataer.onEvent().baseEvent().withEventID("browser_cache_error").withParams(map).submit();
            return;
        }
        try {
            getWebService().post(new CacheRunnable(caller));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CacheRunnable implements Runnable
    {
        private CordovaViewCaller caller = null;

        public CacheRunnable(CordovaViewCaller cordovaViewCaller)
        {
            this.caller = cordovaViewCaller;
        }

        @Override
        public void run()
        {
            if (cordovaExtWebView == null)
            {
                _initCordovaExtWebView();
            }
            cordovaExtWebView.stopLoading();
            CordovaViewCacheData cacheData = caller.getCacheData();
            CordovaViewUrlData data = cacheData.getCordovaViewUrlData();
            if (data == null || data.getUrl() == null)
            {
                Log.i(TAG, "caller == null");
                _cacheNextUrl();
                return;
            }

            cacheData.setState(CordovaViewConst.URL_READY_CACHE);
            int index = callbackMap.indexOf(caller);
            Log.i(TAG, "index == " + index);

            if (data.getNeedThemeBg() != null)
            {
                cordovaExtWebView.setNeedThemeBg(data.getNeedThemeBg().booleanValue());
            }
            try
            {
                if (data.getHeader() != null && data.getHeader().isEmpty())
                {
                    cordovaExtWebView.loadUrl(data.getUrl(), data.getHeader());
                } else
                {
                    Log.i(TAG,"============1 data.getUrl() = " + data.getUrl());
                    cordovaExtWebView.loadUrl(data.getUrl());
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                Map<String, String> map = new HashMap<String, String>();
                map.put("channel", "cache");
                map.put("url", data.getUrl());
                submitToUmeng("browser_create_error", map);
            }

        }
    }

    //该方法注意调用时机，必须保证ipc connected
    private void _initCordovaExtWebView()
    {
        CoocaaOSConnecter coocaaOSConnecter = new CoocaaOSConnecterDefaultImpl(getApplicationContext(), SkyContext.getListener());
        cordovaExtWebView = new CordovaExtWebView(getApplicationContext());
        cordovaExtWebView.setCoocaaOSConnecter(coocaaOSConnecter);
        cordovaExtWebView.setCordovaBusinessDataListener(new BusinessDataListener.CordovaBusinessDataListener()
        {
            @Override
            public String getBusinessData(String data, BusinessDataListener.BussinessCallback cb)
            {
                Log.i(TAG, "getBusinessData");
                return null;
            }

            @Override
            public boolean setBusinessData(String data, BusinessDataListener.BussinessCallback cb)
            {
                Log.i(TAG, "setBusinessData");
                return false;
            }
        });
        cordovaExtWebView.setCordovaExtWebViewDataListener(new CordovaExtWebView.CordovaExtWebViewDataListener()
        {
            @Override
            public void notifyMessage(String data)
            {
                Log.i(TAG, "notifyMessage");
            }

            @Override
            public void notifyLogInfo(final String eventId, final Map<String, String> map)
            {
                Log.i(TAG, "notifyLogInfo");
                getWorkService().submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            SkyDataer.onEvent().pageEvent().pageCustomEvent().withEventID(eventId)
                                    .withParams(map).submitSync();
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                //copy from activity，以上提交日志
            }

            @Override
            public void notifyPageResume(final String pageName, final Map<String, String> map)
            {
                Log.i(TAG, "notifyPageResume");
                Intent intent = new Intent();
                intent.putExtra("action", "notifyLog");
                intent.putExtra("logType", "pageResume");
                intent.putExtra("url", getCurrentCacheData().getCordovaViewUrlData().getUrl());
                intent.putExtra("pageName", pageName);
                intent.putExtra("datalist", (Serializable) map);
                returnMsgToParent(intent);

                getWorkService().submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            PageProperty pageProperty = new PageProperty().withName(pageName);
                            pageProperty.withExtras(map);
                            SkyDataer.onEvent().pageEvent().pageResumeEvent().onResume(pageProperty);
                        } catch (Exception e)

                        {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void notifyPagePause(final String pageName)
            {
                Log.i(TAG, "notifyPagePause");
                Intent intent = new Intent();
                intent.putExtra("action", "notifyLog");
                intent.putExtra("logType", "pagePause");
                intent.putExtra("url", getCurrentCacheData().getCordovaViewUrlData().getUrl());
                intent.putExtra("pageName", pageName);
                returnMsgToParent(intent);
                getWorkService().submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            PageProperty pageProperty = new PageProperty().withName(pageName);
                            SkyDataer.onEvent().pageEvent().pagePausedEvent().onPaused(pageProperty);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        cordovaExtWebView.setCordovaExtWebViewListener(new CordovaExtWebView.CordovaExtWebViewListener()
        {
            @Override
            public void onPageStarted(String url)
            {
                Log.i(TAG, "onPageStarted");
                startTime = SystemClock.uptimeMillis();

                final Map<String, String> map = new HashMap<String, String>();
                map.put("channel", "cache");
                map.put("url", url);
                submitToUmeng("browser_web_load_start", map);

                getWorkService().submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            SkyDataer.onEvent().baseEvent()
                                    .withEventID("browser_web_load_start").withParams(map).submit();
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                try
                {
                    getCurrentCacheData().setState(CordovaViewConst.URL_CACHE_START);
                    getCurrentCallback().onLoadStart(url);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageExit()
            {
                Log.i(TAG, "onPageExit()");
                IBroswerCacheViewActivity activity = CordovaViewCacheHolder.getInstance().getActivity();
                if (activity != null && activity.isActivityShown())
                {
                    activity.exitActivity();
                }
            }

            @Override
            public void onPageFinished(String url)
            {
                Log.i(TAG, "onPageFinished()");
                endTime = SystemClock.uptimeMillis();
                final Map<String, String> map = new HashMap<String, String>();
                map.put("channel", "cache");
                map.put("url", url);
                map.put("intervalTime", (endTime-startTime) + "");
                submitToUmeng("browser_web_load_success", map);
                getWorkService().submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            SkyDataer.onEvent().baseEvent()
                                    .withEventID("browser_web_load_success").withParams(map).submit();
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                //copy from activity，以上提交日志
                try
                {
                    getCurrentCacheData().setState(CordovaViewConst.URL_CACHE_FINISH);
                    getCurrentCallback().onLoadFinished(url);
                    _handleCacheFinish();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl)
            {
                Log.i(TAG, "onPageError()");

                final Map<String, String> map = new HashMap<String, String>();
                map.put("channel", "cache");
                map.put("url", failingUrl);
                map.put("errorCode", errorCode + "");
                map.put("description", description);
                submitToUmeng("browser_web_load_failed", map);

                getWorkService().submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            SkyDataer.onEvent().baseEvent()
                                    .withEventID("browser_web_load_failed").withParams(map).submit();
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                //copy from activity，以上提交日志
                try
                {
                    getCurrentCacheData().setState(CordovaViewConst.URL_CACHE_ERROR);
                    getCurrentCallback().onLoadError(errorCode, description, failingUrl);
                    _cacheNextUrl();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageSslError(int errorCode, String failingUrl)
            {
                Log.i(TAG, "onPageSslError()");
            }

            @Override
            public void onProgressChanged(int process)
            {
                Log.i(TAG, "onProgressChanged()");
            }
        });
    }

    private ICordovaViewCallback getCurrentCallback()
    {
        if (currentCaller != null)
        {
            return currentCaller.getCallback();
        }
        return defaultCallback;
    }

    private CordovaViewCacheData getCurrentCacheData()
    {
        if (currentCaller == null)
        {
            return new CordovaViewCacheData(new CordovaViewUrlData());
        }

        return currentCaller.getCacheData();
    }

    private ICordovaViewCallback.Stub defaultCallback = new ICordovaViewCallback.Stub()
    {
        @Override
        public void onAddCacheWebViewPool(boolean isSuccess) throws RemoteException
        {

        }

        @Override
        public void onLoadStart(String url) throws RemoteException
        {

        }

        @Override
        public void onLoadError(int errorCode, String description, String failingUrl) throws RemoteException
        {

        }

        @Override
        public void onLoadFinished(String url) throws RemoteException
        {

        }

        @Override
        public boolean canShow(String url) throws RemoteException
        {
            return false;
        }

        @Override
        public void showWebView(int code) throws RemoteException
        {

        }
    };


    private synchronized void _cacheNextUrl()
    {
        if (currentCaller != null)
        {
            CordovaViewCacheData cacheData = currentCaller.getCacheData();
            if ((cacheData != null && cacheData.getCordovaViewUrlData() != null)
                    && cacheData.getState() != CordovaViewConst.URL_CACHE_ERROR
                    && cacheData.getState() != CordovaViewConst.URL_SHOWN_FAILED
                    && cacheData.getState() != CordovaViewConst.URL_SHOWN_FINISH)
            {
                return;
            }

            //当前一个失败之后，展示下一个
            callbackMap.remove(currentCaller);
            currentCaller = null;
        }

        if (callbackMap.isEmpty())
        {
            stopServiceIfCanStop();//当前暂无需要缓存，可以空滤退出service了
            return;
        }
        currentCaller = callbackMap.get(0);
        _cacheWebView(currentCaller.getCallback(), currentCaller);
    }

    private void _handleCacheFinish()
    {
        boolean canShown = false;
        try
        {
            canShown = getCurrentCallback().canShow(getCurrentCacheData().getCordovaViewUrlData().getUrl());
            if (!canShown)
            {
                Log.w(TAG, "web view cache finish,but can not show by caller");
                currentCaller.getCacheData().setState(CordovaViewConst.URL_SHOWN_FAILED);
                getCurrentCallback().showWebView(CordovaViewConst.SHOW_RESULT_FAILED_NOT_SHOW);
                final Map<String, String> map = new HashMap<String, String>();
                map.put("desc", "cannotShow");
                submitToUmeng("browser_cache_error", map);
                SkyDataer.onEvent().baseEvent().withEventID("browser_cache_error").withParams(map).submit();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (!canShown)
        {
            _cacheNextUrl();
        } else
        {
            try
            {

                if (cordovaExtWebView != null)
                {
                    CordovaViewCacheHolder.getInstance().setView(cordovaExtWebView);
                }
                Intent intent = new Intent(getApplicationContext(), BrowserCacheViewActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("url",getCurrentCacheData().getCordovaViewUrlData().getUrl());
                getApplicationContext().startActivity(intent);
                getCurrentCallback().showWebView(CordovaViewConst.SHOW_RESULT_SUCCESS);
                final Map<String, String> map = new HashMap<String, String>();
                map.put("url", getCurrentCacheData().getCordovaViewUrlData().getUrl());
                submitToUmeng("browser_cache_final_success", map);
                SkyDataer.onEvent().baseEvent().withEventID("browser_cache_final_success").withParams(map).submit();
            } catch (Exception e)
            {
                e.printStackTrace();
                try
                {
                    getCurrentCallback().showWebView(CordovaViewConst.SHOW_RESULT_FAILED_UNKNOWN);
                } catch (RemoteException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (cordovaExtWebView!=null)
        {
            cordovaExtWebView.onDestroy();
            cordovaExtWebView = null;
        }
        callbackMap.clear();//需要告诉之前需要缓存的调用者吗？
        currentCaller = null;
        IBroswerCacheViewActivity activity = CordovaViewCacheHolder.getInstance().getActivity();
        if (activity != null)
        {
            activity.exitActivity();
        }
        CordovaViewCacheHolder.getInstance().removieService();
        CordovaViewCacheHolder.getInstance().removeActivity();
        CordovaViewCacheHolder.getInstance().removeView();
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        if (intent == null || intent.getAction() == null)
        {
            return null;
        }
        if ("coocaa.intent.action.browser.cache_web_service".equals(intent.getAction()))
        {
            return getApiBinder();
        }
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.w(TAG, "---------onUnbind------------");
        return super.onUnbind(intent);
    }

    private ApiBinder apiBinder = null;

    private ApiBinder getApiBinder()
    {
        if (apiBinder == null)
        {
            apiBinder = new ApiBinder();
        }
        return apiBinder;
    }

    @Override
    public void onActivityResume()
    {
        //activity 展示了
    }

    @Override
    public void onActivityDestroy()
    {
        //activity destroy了
        getWorkService().submit(new Runnable()
        {
            @Override
            public void run()
            {
                callbackMap.remove(currentCaller);
                currentCaller = null;

                if (!callbackMap.isEmpty())
                {
                    for (CordovaViewCaller caller : callbackMap)
                    {
                        try
                        {
                            caller.getCallback().showWebView(CordovaViewConst.SHOW_RESULT_FAILED_CONFLICT);
                        } catch (RemoteException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                callbackMap.clear();
                stopServiceIfCanStop();
            }
        });
    }

    private class ApiBinder extends ICordovaViewControl.Stub
    {
        @Override
        public void cacheWebView(String clientName, ICordovaViewCallback callback, CordovaViewUrlData url) throws RemoteException
        {
            _addCordovaView(clientName, callback, url);
        }

        @Override
        public boolean isCachedWebView(String clientName, String url) throws RemoteException
        {
            return false;
        }

        @Override
        public void cleanUp(String clientName) throws RemoteException
        {
            Log.e(TAG,"-----cleanUp-----,name=="+clientName);
            _cleanUpOneClient(clientName);
        }
    }

    private void _cleanUpOneClient(String clientName)
    {
        //清除所有，如果有当前在展示的，咋办？如果有正在缓存咋办？
        if (callbackMap.isEmpty())
        {
            return;
        }

        if (currentCaller != null && clientName.equals(currentCaller.getClientName()))
        {
            //已经开始缓存了，clean不了，等着自动结束吧
            return;
        }
        List<CordovaViewCaller> deleteCaller = new ArrayList<CordovaViewCaller>();
        for (CordovaViewCaller caller : callbackMap)
        {
            if (clientName.equals(caller.getClientName()))
            {
                deleteCaller.add(caller);
            }
        }

        callbackMap.removeAll(deleteCaller);
        deleteCaller.clear();

    }

    private void stopServiceIfCanStop()
    {
        Log.i(TAG, "stopServiceIfCanStop()");
        if (callbackMap.isEmpty())
        {
            Log.w(TAG, "stopSelf()");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private void submitToUmeng(String eventId, Map<String, String> map)
    {
        try
        {
            MobclickAgent.onEvent(getApplicationContext(), eventId, map);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void returnMsgToParent(Intent in)
    {
        try
        {
            CordovaViewUrlData data = getCurrentCacheData().getCordovaViewUrlData();
            String parentPkgName = data.getParentPkgName();
            String parentServiceAction = data.getParentServiceAction();
            Map<String, String> extraMap = data.getExtraMap();
            Log.i(TAG,
                    "BrowserActivity returnMsgToParent action = " + in.getStringExtra("action"));
            if (!TextUtils.isEmpty(parentPkgName) && !TextUtils.isEmpty(parentServiceAction))
            {
                in.setPackage(parentPkgName);
                in.setAction(parentServiceAction);
                in.putExtra("extraMap", (Serializable) extraMap);
                startService(in);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_NOT_STICKY;
    }

    @Override
    public byte[] onHandler(String fromtarget, String cmd, byte[] body)
    {
        if (coocaaOSConnecter != null)
        {
            coocaaOSConnecter.onHandler(getApplicationContext(), fromtarget, cmd, body);
        }
        return new byte[0];
    }

    @Override
    public void onResult(String fromtarget, String cmd, byte[] body)
    {

    }

    @Override
    public byte[] requestPause(String fromtarget, String cmd, byte[] body)
    {
        return new byte[0];
    }

    @Override
    public byte[] requestResume(String fromtarget, String cmd, byte[] body)
    {
        return new byte[0];
    }

    @Override
    public byte[] requestRelease(String fromtarget, String cmd, byte[] body)
    {
        return new byte[0];
    }

    @Override
    public byte[] requestStartToVisible(String fromtarget, String cmd, byte[] body)
    {
        return new byte[0];
    }

    @Override
    public byte[] requestStartToForground(String fromtarget, String cmd, byte[] body)
    {
        return new byte[0];
    }
}
