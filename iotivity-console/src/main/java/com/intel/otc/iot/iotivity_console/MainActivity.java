package com.intel.otc.iot.iotivity_console;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.iotivity.base.ModeType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcResource;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        OcPlatform.OnResourceFoundListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_CONSOLE_DUMP = "consoleDump";
    private static final String SVR_DB_NAME = "oic_svr_db.dat";

    private ScrollView viewScroll;
    private TextView viewConsole;
    private LinearLayout layoutResourceDocking;
    private OcBinarySwitchView ocSW1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewScroll = (ScrollView) findViewById(R.id.scrollViewConsoleText);
        viewScroll.fullScroll(View.FOCUS_DOWN);
        viewConsole = (TextView) findViewById(R.id.consoleTextView);
        viewConsole.setMovementMethod(new ScrollingMovementMethod());
        layoutResourceDocking = (LinearLayout) findViewById(R.id.linearLayoutResourceDocking);

        if (null == savedInstanceState) {
            // Start this app 1st time
            configPlatform();
        } else {
            viewConsole.setText(savedInstanceState.getString(KEY_CONSOLE_DUMP));
        }
    }

    @Override
    protected void onResume() {
        // User returns to the activity
        Log.d(TAG, "onResume");
        super.onResume();

        // Start hosting resources
        ocSW1 = new OcBinarySwitchView(this, mHandler, "/a/led");
        layoutResourceDocking.addView(ocSW1.getView());

        try {
            OcPlatform.findResource("", OcPlatform.WELL_KNOWN_QUERY, EnumSet.of(OcConnectivityType.CT_DEFAULT), this);
            TimeUnit.SECONDS.sleep(1);
            OcPlatform.findResource("", OcPlatform.WELL_KNOWN_QUERY, EnumSet.of(OcConnectivityType.CT_DEFAULT), this);
        } catch (OcException | InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        // Another activity comes into the foreground
        Log.d(TAG, "onPause");
        layoutResourceDocking.removeAllViews();
        ocSW1.destroy();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONSOLE_DUMP, viewConsole.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Retain console output on screen orientation changed
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        viewConsole.setText(savedInstanceState.getString(KEY_CONSOLE_DUMP));
    }

    private void configPlatform() {
        String dbPath = getFilesDir().getPath() + File.separator;

        File dbFolder = new File(dbPath);
        if (!(dbFolder.isDirectory())) {
            // Create the folder if it doesn't exist
            dbFolder.mkdirs();
            display("Create DB directory at " + dbPath);
        }

        String svrDbFilename = dbPath + SVR_DB_NAME;
        File svrDbFile = new File(svrDbFilename);
        if (!svrDbFile.exists()) {
            // Copy from the built-in SVR DB file asset
            InputStream inStream = null;
            OutputStream outStream = null;
            final int BUF_LEN = 1024;
            try {
                inStream = getAssets().open(SVR_DB_NAME);
                outStream = new FileOutputStream(svrDbFilename);
                byte[] buf = new byte[BUF_LEN];
                int count;
                while ((count = inStream.read(buf)) != -1)
                    outStream.write(buf, 0, count);
                display("Copy " + SVR_DB_NAME + " from the package asset");
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
            display("Found SVR DB file: " + svrDbFilename);
        }

        display("Configuring platform...");
        PlatformConfig platformConfig = new PlatformConfig(
                this, ServiceType.IN_PROC, ModeType.CLIENT_SERVER,
                // By setting to "0.0.0.0", it binds to all available interfaces
                "0.0.0.0", 0,
                QualityOfService.HIGH, dbPath + SVR_DB_NAME);
        OcPlatform.Configure(platformConfig);
    }

    @Override
    public void onResourceFound(OcResource res) {
        Log.e(TAG, res.getServerId() + ':' + res.getUri());
        List<String> hosts = res.getAllHosts();
        for (String s : hosts)
            Log.e(TAG, "\t" + s);
    }

    @Override
    public void onFindResourceFailed(Throwable throwable, String s) {
        Log.e(TAG, "Resource discovery failure: " + throwable.toString());
    }

    private void display(final String text) {
        Log.i(TAG, text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewConsole.append("\n" + text);
                viewScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public static final int MSG_CONSOLE_LOG = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CONSOLE_LOG:
                    display(msg.obj.toString());
                    break;
                default:
                    Log.d(TAG, "Received message code " + msg.what);
            }
        }
    };
}