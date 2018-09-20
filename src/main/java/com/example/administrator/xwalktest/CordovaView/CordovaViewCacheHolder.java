package com.example.administrator.xwalktest.CordovaView;

import org.apache.cordova.CordovaExtWebView;

import java.lang.ref.WeakReference;

/**
 * Created by tianjisheng on 2018/6/25.
 */

public class CordovaViewCacheHolder
{
    private static CordovaViewCacheHolder instance = null;
    private WeakReference<CordovaExtWebView> _view = null;
    private WeakReference<IBroswerCacheViewActivity> _activity = null;
    private WeakReference<ICordovaViewCacheService> _service = null;

    public static CordovaViewCacheHolder getInstance()
    {
        if (instance == null)
        {
            instance = new CordovaViewCacheHolder();
        }
        return instance;
    }

    public CordovaExtWebView getView()
    {
        return _view == null ? null : _view.get();
    }

    public void setView(CordovaExtWebView view)
    {
        _view = new WeakReference<CordovaExtWebView>(view);
    }

    public void removeView()
    {
        _view = null;
    }

    public IBroswerCacheViewActivity getActivity()
    {
        return _activity == null ? null : _activity.get();
    }

    public void setActivity(IBroswerCacheViewActivity activity)
    {
        _activity = new WeakReference<IBroswerCacheViewActivity>(activity);
    }

    public void removeActivity()
    {
        _activity = null;
    }

    public ICordovaViewCacheService getService()
    {
        return _service == null ? null : _service.get();
    }

    public void setService(ICordovaViewCacheService service)
    {
        this._service = new WeakReference<ICordovaViewCacheService>(service);
    }

    public void removieService()
    {
        _service = null;
    }
}
