package com.example.administrator.xwalktest;

import android.util.Log;
import android.view.KeyEvent;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class BrowserActivity extends BrowserBaseActivity
{
    private boolean testMode = false;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (testMode && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            Log.i("BrowserActivity", "event = " + event);
            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_0:
                    testSubmit();
                    break;
                case KeyEvent.KEYCODE_9:
                    testSetAnimSwitch();
                    break;
                case KeyEvent.KEYCODE_8:
                    testGetAnimSwitch();
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void testGetAnimSwitch()
    {
        Log.i("BrowserActivity", "testGetAnimSwitch start");

    }

    private void testSetAnimSwitch()
    {
        Log.i("BrowserActivity", "testSetAnimSwitch start");
    }

    private void testSubmit()
    {
        Log.i("BrowserActivity", "testSubmit start");
    }
}
