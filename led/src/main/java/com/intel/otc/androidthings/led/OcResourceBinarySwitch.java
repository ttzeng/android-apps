package com.intel.otc.androidthings.led;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.ResourceProperty;

import java.util.EnumSet;

class OcResourceBinarySwitch extends OcResourceAbstract {
    private static final String TAG = OcResourceBinarySwitch.class.getSimpleName();
    public  static final String RESOURCE_TYPE = "oic.r.switch.binary";
    protected static final String KEY_VALUE = "value";

    protected boolean mState = false;

    OcResourceBinarySwitch(String resourceUri, String resourceIf, EnumSet<ResourceProperty> resourcePropertySet) {
        super(resourceUri, RESOURCE_TYPE, resourceIf, resourcePropertySet);
    }

    @Override
    protected OcRepresentation getOcRepresentation() {
        OcRepresentation rep = null;
        if (mHandle != null) {
            rep = new OcRepresentation();
            try {
                rep.setValue(KEY_VALUE, mState);
            } catch (OcException e) {
                error(e, "Failed to set '" + KEY_VALUE + "' representation");
            }
        }
        return rep;
    }

    @Override
    protected void setOcRepresentation(OcRepresentation rep) {
        if (mHandle != null) {
            try {
                mState = rep.hasAttribute(KEY_VALUE) ? (boolean) rep.getValue(KEY_VALUE) : false;
            } catch (OcException e) {
                error(e, "Failed to get '" + KEY_VALUE + "' representation");
            }
        }
    }
}