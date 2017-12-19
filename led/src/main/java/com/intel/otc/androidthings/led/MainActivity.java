package com.intel.otc.androidthings.led;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.iotivity.base.ModeType;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ResourceProperty;
import org.iotivity.base.ServiceType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SVR_DB_NAME = "oic_svr_db.dat";
    private static final int BUF_SIZE = 1024;

    OcResourceLed mLed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        initOcfPlatform();
        mLed = new OcResourceLed(
                "/a/led",
                OcPlatform.DEFAULT_INTERFACE,
                EnumSet.of(ResourceProperty.DISCOVERABLE));
    }

    @Override
    protected void onResume() {
        // User returns to the activity
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Another activity comes into the foreground
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    void initOcfPlatform() {
        String dbPath = getFilesDir().getPath() + File.separator;
        File dbDir = new File(dbPath);
        if (!(dbDir.isDirectory())) {
            // Create the folder if not exist
            dbDir.mkdirs();
            Log.d(TAG, "Create DB directory at " + dbPath);
        }
        copySvrDbFromAssetsIfNotExist(dbPath);

        Log.d(TAG, "Configuring platform...");
        PlatformConfig platformConfig = new PlatformConfig(
                this,
                ServiceType.IN_PROC,
                ModeType.SERVER,
                "0.0.0.0", // By setting to "0.0.0.0", it binds to all available interfaces
                0, // Uses randomly available port
                QualityOfService.LOW,
                dbPath + SVR_DB_NAME
        );
        OcPlatform.Configure(platformConfig);
    }

    private void copySvrDbFromAssetsIfNotExist(String dbPath) {
        String svrDbFilename = dbPath + SVR_DB_NAME;
        File svrDbFile = new File(svrDbFilename);
        if (!svrDbFile.exists()) {
            // Copy from the built-in SVR DB file asset
            InputStream inStream = null;
            OutputStream outStream = null;
            try {
                inStream = getAssets().open(SVR_DB_NAME);
                outStream = new FileOutputStream(svrDbFilename);
                byte[] buf = new byte[BUF_SIZE];
                int count;
                while ((count = inStream.read(buf)) != -1)
                    outStream.write(buf, 0, count);
                Log.d(TAG, "Copy " + SVR_DB_NAME + " from the assets");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    if (inStream != null)  inStream.close();
                    if (outStream != null) outStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        } else {
            Log.d(TAG, "Found SVR DB file: " + svrDbFilename);
        }
    }
}