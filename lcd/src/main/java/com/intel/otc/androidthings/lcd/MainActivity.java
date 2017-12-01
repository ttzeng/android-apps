package com.intel.otc.androidthings.lcd;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    LcdRgbBacklight lcd = new LcdRgbBacklight();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        new Runnable() {
            @Override
            public void run() {
                lcd.begin(16, 2, LcdRgbBacklight.LCD_5x10DOTS);
                lcd.home();
                lcd.write("Android Things");
            }
        }.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}