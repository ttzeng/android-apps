package com.intel.otc.iot.iotivity_console;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.iotivity.service.RcsException;
import org.iotivity.service.RcsResourceAttributes;
import org.iotivity.service.RcsValue;
import org.iotivity.service.server.RcsResourceObject;

class OcBinarySwitchView extends OcResourceView {
    private static final String TAG = OcBinarySwitchView.class.getSimpleName();
    // OICBinarySwitch - http://oneiota.org/revisions/1393
    public  static final String RESOURCE_TYPE = "oic.r.switch.binary";
    private static final String KEY_VALUE = "value";

    private ToggleButton mResourceView;
    private boolean state = false;

    OcBinarySwitchView(Context context, Handler handler, final String uri) {
        super(context, handler, uri, RESOURCE_TYPE);
        mResourceView = new ToggleButton(context);
        mResourceView.setText("");
        mResourceView.setTextOn("");
        mResourceView.setTextOff("");
        mResourceView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean newState) {
                try {
                    consoleLog("Set " + uri + " to " + ((state = newState)? "On" : "Off"));
                    mOcResource.setAttribute(KEY_VALUE, new RcsValue(state));
                } catch (RcsException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
        try {
            mOcResource.addAttributeUpdatedListener(KEY_VALUE, new RcsResourceObject.OnAttributeUpdatedListener() {
                @Override
                public void onAttributeUpdated(RcsValue oldState, RcsValue newState) {
                    consoleLog("Remote set " + uri + " to " + ((state = newState.asBoolean())? "On" : "Off"));
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResourceView.setChecked(state);
                        }
                    });
                }
            });
        } catch (RcsException e) {
            Log.e(TAG, e.toString());
        }
        consoleLog("Resource " + uri + " of type '" + RESOURCE_TYPE + "' created");
    }

    @Override
    protected RcsResourceAttributes createAttributes() {
        RcsResourceAttributes attrs = super.createAttributes();
        attrs.put(KEY_VALUE, state);
        return attrs;
    }

    @Override
    View getView() {
        return mResourceView;
    }
}