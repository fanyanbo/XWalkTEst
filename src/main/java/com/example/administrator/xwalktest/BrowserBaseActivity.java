package com.example.administrator.xwalktest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;

import com.coocaa.dataer.api.SkyDataer;
import com.coocaa.dataer.api.ccc.impl.DefaultCoocaaSystemConnecter;
import com.coocaa.dataer.api.event.page.lifecycle.PageProperty;
import com.tianci.media.api.Log;
import com.umeng.analytics.MobclickAgent;

import org.apache.cordova.CordovaExtActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BrowserBaseActivity extends CordovaExtActivity
        implements CordovaExtActivity.CordovaWebPageListener,
        CordovaExtActivity.CordovaWebViewListener
{

    private static Handler HANDLER = null;

    static
    {
        if (HANDLER == null)
        {
            HandlerThread ht = new HandlerThread("BrowserActivity");
            ht.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException(Thread thread, Throwable ex)
                {

                }
            });
            ht.start();
            HANDLER = new Handler(ht.getLooper());
        }
    }

    private String parentPkgName = "";
    private String parentServiceAction = "";
    private String url = "";
    private Map<String, String> extraMap;
    private boolean cmdInitDone = false;
    private Map<String, Map<String, String>> waitSubmitData;
    private long startTime = 0, endTime = 0;
    private boolean isLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        try
        {
            Log.i("Sea-browser", "this is BrowserActivity");
            super.onCreate(savedInstanceState);
            url = getIntent().getStringExtra("url");
            if (TextUtils.isEmpty(url))
            {
                url = getIntent().getDataString();
            }
            extraMap = (Map<String, String>) getIntent().getSerializableExtra("extraMap");
            parentPkgName = getIntent().getStringExtra("parentPkgName");
            parentServiceAction = getIntent().getStringExtra("parentServiceAction");

            Log.i("Sea-browser", "parentPkgName = " + parentPkgName + "  parentServiceAction = " +
                    parentServiceAction);
            if (null != extraMap && extraMap.size() > 0)
            {
                String extraString = "";
                for (Object ob : extraMap.keySet())
                {
                    extraString += (ob + "  : " + extraMap.get(ob) + "  ");
                }
                Log.i("Sea-browser", "extraMap = " + extraString);
            }

            setUserAgentMode(1);
            this.setCordovaWebPageListener(this);
            this.setCordovaWebViewListener(this);
            loadUrl(url);
        } catch (Throwable e)
        {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if (intent != null)
        {
            String u = intent.getStringExtra("url");
            if (!TextUtils.isEmpty(u) && !u.equals(url))
            {
                url = u;
                try
                {
                    loadUrl(url);
                } catch (Throwable e)
                {
                    e.printStackTrace();
                    finish();
                }
            }
        }
    }

    @Override
    public void onCmdConnectorInit()
    {
        super.onCmdConnectorInit();
        try
        {
            SkyDataer.onCore().withContext(BrowserBaseActivity.this)
                    .withDebugMode(BuildConfig.DEBUG).withProductID("App_Browser")
                    .withCoocaaSystemConnecter(
                            new DefaultCoocaaSystemConnecter(BrowserBaseActivity.this)).create();
            final Map<String, String> map = new HashMap<String, String>();
            map.put("channel", "common");
            SkyDataer.onEvent().baseEvent().withEventID("browser_connector_init").withParams(map).submit();

            HANDLER.post(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        cmdInitDone = true;
                        if (null != waitSubmitData && waitSubmitData.size() > 0)
                        {
                            Set<String> set = waitSubmitData.keySet();
                            for (String key : set)
                            {
                                if ("pageResume".equals(key))
                                {
                                    Map<String, String> logMap = waitSubmitData.get(key);
                                    final String pageName =
                                            (logMap != null && logMap.containsKey("pageName")) ?
                                                    logMap.get("pageName") : "";
                                    logMap.remove("pageName");
                                    PageProperty pageProperty =
                                            new PageProperty().withName(pageName);
                                    pageProperty.withExtras(logMap);
                                    SkyDataer.onEvent().pageEvent().pageResumeEvent()
                                            .onResume(pageProperty);
                                } else if ("pagePause".equals(key))
                                {
                                    Map<String, String> logMap = waitSubmitData.get(key);
                                    final String pageName =
                                            (logMap != null && logMap.containsKey("pageName")) ?
                                                    logMap.get("pageName") : "";
                                    PageProperty pageProperty =
                                            new PageProperty().withName(pageName);
                                    SkyDataer.onEvent().pageEvent().pagePausedEvent()
                                            .onPaused(pageProperty);
                                } else if ("browser_web_load_start".equals(key))
                                {
                                    SkyDataer.onEvent().baseEvent()
                                            .withEventID(key).withParams(waitSubmitData.get(key)).submitSync();
                                } else if ("browser_web_load_success".equals(key))
                                {
                                    SkyDataer.onEvent().baseEvent()
                                            .withEventID(key).withParams(waitSubmitData.get(key)).submitSync();
                                } else if ("browser_web_load_failed".equals(key)) {
                                    SkyDataer.onEvent().baseEvent()
                                            .withEventID(key).withParams(waitSubmitData.get(key)).submitSync();
                                } else
                                {
                                    SkyDataer.onEvent().pageEvent().pageCustomEvent()
                                            .withEventID(key).withParams(waitSubmitData.get(key))
                                            .submitSync();
                                }
                            }
                            waitSubmitData.clear();
                        }
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        try
        {
            MobclickAgent.onPause(this);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            MobclickAgent.onResume(this);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.i("WebViewSDK","onDestroy isLoading = " + isLoading);
        if (isLoading) {
            endTime = SystemClock.uptimeMillis();
            final Map<String, String> map = new HashMap<String, String>();
            map.put("channel", "common");
            map.put("url", url);
            map.put("intervalTime", (endTime-startTime) + "");
            SkyDataer.onEvent().baseEvent()
                    .withEventID("browser_web_load_return").withParams(map).submit();
        }
    }

    @Override
    public void notifyMessage(String data)
    {
    }

    @Override
    public void notifyLogInfo(final String eventId, final Map<String, String> map)
    {
        HANDLER.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (cmdInitDone)
                    {
                        SkyDataer.onEvent().pageEvent().pageCustomEvent().withEventID(eventId)
                                .withParams(map).submitSync();
                    } else
                    {
                        if (null == waitSubmitData)
                        {
                            waitSubmitData = new HashMap<>();
                        }
                        waitSubmitData.put(eventId, map);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void notifyPageResume(final String eventId, final Map<String, String> map)
    {

        HANDLER.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (cmdInitDone)
                    {
                        PageProperty pageProperty = new PageProperty().withName(eventId);
                        pageProperty.withExtras(map);
                        SkyDataer.onEvent().pageEvent().pageResumeEvent().onResume(pageProperty);
                    } else
                    {
                        if (null == waitSubmitData)
                        {
                            waitSubmitData = new HashMap<>();
                        }
                        Map<String, String> _map;
                        if (null == map)
                        {
                            _map = new HashMap<>();
                        } else
                        {
                            _map = map;
                        }
                        map.put("pageName", eventId);
                        waitSubmitData.put("pageResume", _map);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void notifyPagePause(final String eventId)
    {
        HANDLER.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (cmdInitDone)
                    {
                        PageProperty pageProperty = new PageProperty().withName(eventId);
                        SkyDataer.onEvent().pageEvent().pagePausedEvent().onPaused(pageProperty);
                    } else
                    {
                        if (null == waitSubmitData)
                        {
                            waitSubmitData = new HashMap<>();
                        }
                        Map<String, String> map = new HashMap<>();
                        map.put("pageName", eventId);
                        waitSubmitData.put("pagePause", map);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPageStarted(String url)
    {
        startTime = SystemClock.uptimeMillis();
        isLoading = true;

        final Map<String, String> map = new HashMap<String, String>();
        map.put("channel", "common");
        map.put("url", url);

        HANDLER.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (cmdInitDone) {
                        SkyDataer.onEvent().baseEvent()
                                .withEventID("browser_web_load_start").withParams(map).submitSync();
                    } else {
                        if (null == waitSubmitData) {
                            waitSubmitData = new HashMap<>();
                        }
                        waitSubmitData.put("browser_web_load_start", map);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPageFinished(String url)
    {
        isLoading = false;
        endTime = SystemClock.uptimeMillis();

        final Map<String, String> map = new HashMap<String, String>();
        map.put("channel", "common");
        map.put("url", url);
        map.put("intervalTime", (endTime-startTime) + "");

        HANDLER.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (cmdInitDone) {
                        SkyDataer.onEvent().baseEvent()
                                .withEventID("browser_web_load_success").withParams(map).submitSync();
                    } else {
                        if (null == waitSubmitData) {
                            waitSubmitData = new HashMap<>();
                        }
                        waitSubmitData.put("browser_web_load_success", map);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl)
    {
        isLoading = false;

        final Map<String, String> map = new HashMap<String, String>();
        map.put("channel", "common");
        map.put("url", url);
        map.put("errorCode", errorCode + "");
        map.put("description", description);

        HANDLER.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (cmdInitDone) {
                        SkyDataer.onEvent().baseEvent()
                                .withEventID("browser_web_load_failed").withParams(map).submitSync();
                    } else {
                        if (null == waitSubmitData) {
                            waitSubmitData = new HashMap<>();
                        }
                        waitSubmitData.put("browser_web_load_failed", map);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

}
