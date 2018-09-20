package com.example.administrator.xwalktest.CordovaView;
interface ICordovaViewCallback {
    /**
    *   该url是否成功加入缓存队列。
    *   isSuccess true    加入队列
    *   isSuccess false   未加入队列
    **/
    void onAddCacheWebViewPool(boolean isSuccess);
   /**
    *   该url开始缓存。
    **/
    void onLoadStart(String url);
    /**
     *   该url加载过程中出错。
     *   warn:不要在该方法里面，又调用cache url 重试，进入循环
     **/
    void onLoadError(int errorCode, String description, String failingUrl);
    /**
     *   该url加载完毕。
     **/
    void onLoadFinished(String url);
    /**
      *   该url此刻能否展示。
     **/
    boolean canShow(String url);
    /**
     *   code >0 表示成功
     *   code<=0 表示失败，具体失败原因，见文档
     **/
    void showWebView(int code);
}
