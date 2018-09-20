package com.example.administrator.xwalktest.CordovaView;

/**
 * Created by tianjisheng on 2018/6/27.
 */

public class CordovaViewCaller
{
    private CordovaViewCacheData cacheData = null;
    private ICordovaViewCallback callback = null;
    private String clientName = "";
    public CordovaViewCaller(String clientName,CordovaViewCacheData data, ICordovaViewCallback callback)
    {
        this.clientName = clientName;
        this.cacheData = data;
        this.callback = callback;
    }

    public void setCacheData(CordovaViewCacheData urlData)
    {
        this.cacheData = urlData;
    }

    public void setCallback(ICordovaViewCallback callback)
    {
        this.callback = callback;
    }

    public CordovaViewCacheData getCacheData()
    {
        return cacheData;
    }

    public ICordovaViewCallback getCallback()
    {
        return callback;
    }

    public String getClientName()
    {
        return clientName;
    }

    public void setClientName(String clientName)
    {
        this.clientName = clientName;
    }
}
