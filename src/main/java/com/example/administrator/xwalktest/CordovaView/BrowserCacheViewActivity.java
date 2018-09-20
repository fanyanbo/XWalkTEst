package com.example.administrator.xwalktest.CordovaView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.coocaa.dataer.api.SkyDataer;
import com.umeng.analytics.MobclickAgent;

import org.apache.cordova.CordovaExtWebView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianjisheng on 2018/6/25.
 */

public class BrowserCacheViewActivity extends Activity implements IBroswerCacheViewActivity
{
    private LinearLayout layout = null;
    private CordovaExtWebView view = null;
    private boolean isShown = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bindCordovaViewCacheService();
        layout = new LinearLayout(getApplicationContext());
        CordovaViewCacheHolder.getInstance().setActivity(this);
        view = CordovaViewCacheHolder.getInstance().getView();
        if (view == null)
        {
            finish();
        } else
        {
            initUI();
        }
        setContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private boolean bindCordovaViewCacheService()
    {
        try
        {
            Intent intent = new Intent();
            intent.setAction("coocaa.intent.action.browser.cache_web_service");
            intent.setPackage(this.getPackageName());//浏览器 package
            this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
            Log.e("cordova_view", "activity ,onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.e("cordova_view", "activity ,onServiceDisconnected");
        }
    };

    private void initUI()
    {
        layout.removeAllViews();

        layout.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setAlpha(0.01f);
        layout.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                layout.setAlpha(1.0f);
            }
        }, 1000L);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        view.onStart();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            MobclickAgent.onResume(this);
            final Map<String, String> map = new HashMap<String, String>();
            String url = getIntent().getStringExtra("url");
            map.put("url",url);
            MobclickAgent.onEvent(getApplicationContext(), "browser_cache_activity_success", map);
            SkyDataer.onEvent().baseEvent().withEventID("browser_cache_activity_success").withParams(map).submit();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        isShown = true;
        view.onResume();
        if (CordovaViewCacheHolder.getInstance().getService() != null)
        {
            CordovaViewCacheHolder.getInstance().getService().onActivityResume();
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
        isShown = false;
        view.onPause();
        this.finish();//页面盖住，直接退掉。防止首页盖住之后，缓存不了新的页面
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        view.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        layout.removeAllViews();
        view = CordovaViewCacheHolder.getInstance().getView();
        initUI();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            if (layout != null)
            {
                layout.removeAllViews();
            }

            if (CordovaViewCacheHolder.getInstance().getService() != null)
            {
                CordovaViewCacheHolder.getInstance().getService().onActivityDestroy();
            }
            CordovaViewCacheHolder.getInstance().removeActivity();
            this.unbindService(serviceConnection);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isActivityShown()
    {
        return isShown;
    }

    @Override
    public void exitActivity()
    {
        Log.i("cordova_view", "exitActivity()");
        this.finish();
    }
}
