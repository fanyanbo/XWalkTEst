package com.example.administrator.xwalktest.CordovaView;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianjisheng on 2018/6/27.
 */

public class CordovaViewUrlData implements Parcelable
{
    private String url = "";
    private Boolean isNeedThemeBg = null;
    private Map<String, String> header = new HashMap<String, String>();
    //为了兼容activity
    private String parentPkgName = "";
    private String parentServiceAction = "";
    private Map<String, String> extraMap = new HashMap<String, String>();

    CordovaViewUrlData(Parcel in)
    {
        url = in.readString();
        byte tmpIsNeedThemeBg = in.readByte();
        isNeedThemeBg = tmpIsNeedThemeBg == 0 ? null : tmpIsNeedThemeBg == 1;
        header = in.readHashMap(ClassLoader.getSystemClassLoader());
        parentPkgName = in.readString();
        parentServiceAction = in.readString();
        extraMap = in.readHashMap(ClassLoader.getSystemClassLoader());
    }

    public CordovaViewUrlData()
    {

    }

    public static final Creator<CordovaViewUrlData> CREATOR = new Creator<CordovaViewUrlData>()
    {
        @Override
        public CordovaViewUrlData createFromParcel(Parcel in)
        {
            return new CordovaViewUrlData(in);
        }

        @Override
        public CordovaViewUrlData[] newArray(int size)
        {
            return new CordovaViewUrlData[size];
        }
    };

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Boolean getNeedThemeBg()
    {
        return isNeedThemeBg;
    }

    public void setNeedThemeBg(Boolean needThemeBg)
    {
        isNeedThemeBg = needThemeBg;
    }

    public Map<String, String> getHeader()
    {
        return header;
    }

    public void setHeader(Map<String, String> header)
    {
        this.header = header;
    }

    public String getParentPkgName()
    {
        return parentPkgName;
    }

    public void setParentPkgName(String parentPkgName)
    {
        this.parentPkgName = parentPkgName;
    }

    public String getParentServiceAction()
    {
        return parentServiceAction;
    }

    public void setParentServiceAction(String parentServiceAction)
    {
        this.parentServiceAction = parentServiceAction;
    }

    public Map<String, String> getExtraMap()
    {
        return extraMap;
    }

    public void setExtraMap(Map<String, String> extraMap)
    {
        this.extraMap = extraMap;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(url);
        dest.writeByte((byte) (isNeedThemeBg == null ? 0 : isNeedThemeBg ? 1 : 2));
        dest.writeMap(header);
        dest.writeString(parentPkgName);
        dest.writeString(parentServiceAction);
        dest.writeMap(extraMap);
    }
}
