package com.intel.otc.iot.iotivity_console;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import org.iotivity.service.RcsResourceAttributes;
import org.iotivity.service.server.RcsResourceObject;

abstract class OcResourceView {
    protected Context mContext;
    protected Handler mHandler;
    protected RcsResourceObject mOcResource;

    OcResourceView(Context context, Handler handler, String uri, String resourceType) {
        mContext = context;
        mHandler = handler;
        createResource(uri, resourceType, "oic.if.baseline");
    }

    OcResourceView(Context context, Handler handler, String uri, String resourceType, String resourceInterface) {
        mContext = context;
        mHandler = handler;
        createResource(uri, resourceType, resourceInterface);
    }

    abstract View getView();

    protected void createResource(String uri, String resourceType, String resourceInterface) {
        RcsResourceObject.Builder builder = new RcsResourceObject.Builder(uri, resourceType, resourceInterface);
        mOcResource = configure(builder).build();
    }

    protected RcsResourceObject.Builder configure(RcsResourceObject.Builder builder) {
        // builder creates observable and discoverable resource by default, override this function
        // to set these properties explicitly with setDiscoverable() and setObservable()
        return builder.setAttributes(createAttributes());
    }

    protected RcsResourceAttributes createAttributes() {
        // empty resource attribute to be set in the overrided functions
        return new RcsResourceAttributes();
    }

    public void destroy() {
        mOcResource.destroy();
    }

    protected void consoleLog(String s) {
        Message msg = mHandler.obtainMessage(MainActivity.MSG_CONSOLE_LOG);
        msg.obj = s;
        mHandler.sendMessage(msg);
    }
}