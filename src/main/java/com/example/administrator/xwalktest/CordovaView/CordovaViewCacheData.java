package com.example.administrator.xwalktest.CordovaView;

/**
 * Created by tianjisheng on 2018/6/28.
 */

public class CordovaViewCacheData
{
    private CordovaViewUrlData cordovaViewUrlData = null;
    private int state = CordovaViewConst.URL_WAIT_CACHE;
    public CordovaViewCacheData(CordovaViewUrlData data)
    {
        this.cordovaViewUrlData = data;
    }

    public CordovaViewUrlData getCordovaViewUrlData()
    {
        return cordovaViewUrlData;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }
}
