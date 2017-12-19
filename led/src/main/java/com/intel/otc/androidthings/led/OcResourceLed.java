package com.intel.otc.androidthings.led;

import android.os.Build;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import org.iotivity.base.OcRepresentation;
import org.iotivity.base.ResourceProperty;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

class OcResourceLed extends OcResourceBinarySwitch {
    private static final String TAG = OcResourceLed.class.getSimpleName();
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_JOULE = "joule";

    Gpio mLed;

    OcResourceLed(String resourceUri, String resourceIf, EnumSet<ResourceProperty> resourcePropertySet) {
        super(resourceUri, resourceIf, resourcePropertySet);
        mLed = OpenLED();
    }

    @Override
    public synchronized void Delete() {
        super.Delete();
        if (mLed != null) {
            try {
                mLed.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
            mLed = null;
        }
    }

    @Override
    protected OcRepresentation getOcRepresentation() {
        try {
            if (mLed != null)
                mState = mLed.getValue();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return super.getOcRepresentation();
    }

    @Override
    protected void setOcRepresentation(OcRepresentation rep) {
        try {
            if (mLed != null) {
                super.setOcRepresentation(rep);
                mLed.setValue(mState);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private Gpio OpenLED() {
        Gpio gpio;
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            gpio = service.openGpio(getOnBoardLedName());
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            gpio = null;
        }
        return gpio;
    }

    private String getOnBoardLedName() {
        if (Build.DEVICE.equals(DEVICE_EDISON)) {
            PeripheralManagerService service = new PeripheralManagerService();
            List<String> gpioNames = service.getGpioList();
            return (gpioNames.size() > 0 && gpioNames.get(0).startsWith("IO"))?
                    "IO13" : "GP45";
        }
        if (Build.DEVICE.equals(DEVICE_JOULE))
            return "J6_25";

        throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
    }
}