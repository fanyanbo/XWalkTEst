package com.example.administrator.xwalktest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by fanyanbo on 2018/9/12.
 * Email: fanyanbo@skyworth.com
 */
public class MainActivity extends Activity {

    private static final String TAG = "WebViewSDK";
    private Button btn1;
    private Button btn2;
    private EditText editText;
    private CheckBox cb;
    private boolean mIsChecked = false;
    private boolean mIsHidden = false;
    private int core = 0;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.activity_main);
        setContentView(R.layout.layout);
        Log.i(TAG,"MainActivity onCreate");

        mContext = this;

        editText = (EditText)findViewById(R.id.content_url);
        cb = (CheckBox)findViewById(R.id.checkBox);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG,"onCheckedChanged isChecked = " + isChecked);
                mIsChecked = isChecked;
            }
        });

        btn1 = (Button)findViewById(R.id.button);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String urlcontent = editText.getText().toString();
                Log.i(TAG,"onClick!!! url = " + urlcontent);
                if( urlcontent != null && ( urlcontent.startsWith("http://") || urlcontent.startsWith("https://")))
                {
                    Intent mIntent = new Intent("com.qjy.action.browser2");
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.putExtra("url", urlcontent);
//                    mIntent.putExtra("mode", 0);
//                    mIntent.putExtra("core", core);
//                    mIntent.putExtra("cache", mIsChecked);
                    startActivity(mIntent);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "url is not illigel", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn2 = (Button)findViewById(R.id.button1);
        btn2.setText("启动活动页");
        if (mIsHidden) btn1.setVisibility(View.INVISIBLE);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String urlcontent = "http://beta.webapp.skysrt.com/appstore/webxtest/test7/test.html";
                Log.i(TAG,"onClick!!! url = " + urlcontent);
                if( urlcontent != null && ( urlcontent.startsWith("http://") || urlcontent.startsWith("https://")))
                {
                    Intent mIntent = new Intent("com.qjy.action.browser2");
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.putExtra("url", urlcontent);
//                    mIntent.putExtra("mode", 0);
//                    mIntent.putExtra("core", core);
//                    mIntent.putExtra("cache", mIsChecked);
                    startActivity(mIntent);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "url is not illigel", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}

