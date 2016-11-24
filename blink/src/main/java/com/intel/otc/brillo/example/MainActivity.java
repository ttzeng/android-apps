package com.intel.otc.brillo.example;

import android.app.Activity;
import android.hardware.pio.Gpio;
import android.hardware.pio.PeripheralManagerService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.system.ErrnoException;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SERVICE_INTERVAL_IN_MSEC = 1000;
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_JOULE = "joule";

    private Handler mHandler = new Handler();
    private Runnable mRunnable = this::myService;

    Gpio mLed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mLed = OpenLED();

        // Add myService runnable to the message queue
        mHandler.post(mRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Remove the pending of posts of runnable in the message queue
        mHandler.removeCallbacks(mRunnable);

        if (mLed != null) {
            mLed.close();
            mLed = null;
        }
    }

    private Gpio OpenLED() {
        Gpio gpio;
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            gpio = service.openGpio(getOnBoardLedName());
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (ErrnoException e) {
            Log.e(TAG, e.toString());
            gpio = null;
        }
        return gpio;
    }

    boolean isIntelEdisonBoard() {
        return (Build.DEVICE.equals(DEVICE_EDISON));
    }

    boolean isIntelJouleBoard() {
        return (Build.DEVICE.equals(DEVICE_JOULE));
    }

    String getOnBoardLedName() {
        if (isIntelEdisonBoard())
            return "IO13";
        if (isIntelJouleBoard())
            return "LED100";

        throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
    }

    private void myService() {
        try {
            if (mLed != null)
                mLed.setValue(!mLed.getValue());
        } catch (ErrnoException e) {
            Log.e(TAG, e.toString());
        }

        mHandler.postDelayed(mRunnable, SERVICE_INTERVAL_IN_MSEC);
    }
}
