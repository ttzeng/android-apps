package com.intel.otc.iot.smarthome;

import android.content.Context;
import android.util.Log;

import org.iotivity.base.ModeType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcResource;
import org.iotivity.base.OcResourceIdentifier;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OcClient implements OcPlatform.OnResourceFoundListener {
    private static final String TAG = OcClient.class.getSimpleName();

    public interface OnResourceFound {
        void onResourceFound(OcResource resource);
    }
    private OnResourceFound onResourceFoundListener;
    private Map<OcResourceIdentifier, OcResource> mResourceFound = new HashMap<>();

    public OcClient(Context context, OnResourceFound listener) {
        onResourceFoundListener = listener;
        PlatformConfig platformConfig = new PlatformConfig(
                context,
                ServiceType.IN_PROC,
                ModeType.CLIENT,
                "0.0.0.0", // By setting to "0.0.0.0", it binds to all available interfaces
                0,         // Uses randomly available port
                QualityOfService.LOW
        );
        OcPlatform.Configure(platformConfig);
    }

    public synchronized void findResources(final String ocResourceType) {
        mResourceFound.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OcPlatform.OnResourceFoundListener listener = OcClient.this;
                    String requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=" + ocResourceType;
                    OcPlatform.findResource("", requestUri, EnumSet.of(OcConnectivityType.CT_DEFAULT), listener);
                    TimeUnit.SECONDS.sleep(1);
                    /* Find resource is done twice so that we discover the original resources a second time.
                     * These resources will have the same uniqueidentifier (yet be different objects),
                     * so that we can verify/show the duplicate-checking code in foundResource();
                     */
                    OcPlatform.findResource("", requestUri, EnumSet.of(OcConnectivityType.CT_DEFAULT), listener);
                } catch (OcException | InterruptedException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }).start();
    }

    @Override
    public void onResourceFound(OcResource ocResource) {
        if (null == ocResource) {
            Log.e(TAG, "Invalid resource found");
            return;
        }
        if (!mResourceFound.containsKey(ocResource.getUniqueIdentifier())) {
            Log.d(TAG, "Found resource " + ocResource.getHost() + ocResource.getUri() +
                        " for the first time on server with ID " + ocResource.getServerId());
            mResourceFound.put(ocResource.getUniqueIdentifier(), ocResource);
        } else {
            Log.d(TAG, "Found resource " + ocResource.getHost() + ocResource.getUri());
            if (null != onResourceFoundListener)
                onResourceFoundListener.onResourceFound(ocResource);
        }
    }

    @Override
    public void onFindResourceFailed(Throwable throwable, String s) {
        Log.e(TAG, "Resource discovery failure: " + throwable.toString());
    }
}
