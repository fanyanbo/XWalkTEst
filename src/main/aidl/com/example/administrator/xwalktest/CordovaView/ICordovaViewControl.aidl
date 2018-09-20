package com.example.administrator.xwalktest.CordovaView;
import com.example.administrator.xwalktest.CordovaView.ICordovaViewCallback;
import com.example.administrator.xwalktest.CordovaView.CordovaViewUrlData;
interface ICordovaViewControl {
     void cacheWebView(String clientName,ICordovaViewCallback callback,in CordovaViewUrlData urlData);
     boolean isCachedWebView(String clientName,String url);
     void cleanUp(String clientName);
}
