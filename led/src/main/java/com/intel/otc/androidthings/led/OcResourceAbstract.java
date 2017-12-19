package com.intel.otc.androidthings.led;

import android.util.Log;

import org.iotivity.base.EntityHandlerResult;
import org.iotivity.base.ObservationInfo;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResourceHandle;
import org.iotivity.base.OcResourceRequest;
import org.iotivity.base.OcResourceResponse;
import org.iotivity.base.RequestHandlerFlag;
import org.iotivity.base.ResourceProperty;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

abstract class OcResourceAbstract implements OcPlatform.EntityHandler {
    private static final String TAG = OcResourceAbstract.class.getSimpleName();
    protected static final long Notifier_Default_Interval_In_Msec = 1000;

    protected OcResourceHandle mHandle = null;
    protected List<Byte> mObservationIds = new LinkedList<>();
    protected Thread mObserverNotifier = null;
    protected long mNotifyInterval = Notifier_Default_Interval_In_Msec;

    protected abstract OcRepresentation getOcRepresentation();
    protected abstract void setOcRepresentation(OcRepresentation rep);

    OcResourceAbstract(String resourceUri, String resourceType, String resourceIf, EnumSet<ResourceProperty> resourcePropertySet) {
        try {
            mHandle = OcPlatform.registerResource(resourceUri, resourceType, resourceIf, this, resourcePropertySet);
            Log.d(TAG, "Resource " + resourceUri + " of type '" + resourceType + "' registered");
        } catch (OcException e) {
            error(e, "Failed to register resource " + resourceUri);
            mHandle = null;
        }
    }

    public synchronized void Delete() {
        if (mHandle != null) {
            try {
                OcPlatform.unregisterResource(mHandle);
                Log.d(TAG, "Resource unregistered");
            } catch (OcException e) {
                error(e, "Failed to unregister resource");
            }
        }
    }

    @Override
    public synchronized EntityHandlerResult handleEntity(OcResourceRequest request) {
        EntityHandlerResult result = EntityHandlerResult.ERROR;
        if (request != null && mHandle != null) {
            // Get the request flags
            EnumSet<RequestHandlerFlag> requestFlags = request.getRequestHandlerFlagSet();
            if (requestFlags.contains(RequestHandlerFlag.INIT)) {
                Log.d(TAG, "\tRequest Flag: Init");
                result = handleInit(request);
            }
            if (requestFlags.contains(RequestHandlerFlag.REQUEST)) {
                Log.d(TAG, "\tRequest Flag: Request");
                result = handleRequest(request);
            }
            if (requestFlags.contains(RequestHandlerFlag.OBSERVER)) {
                Log.d(TAG, "\tRequest Flag: Observer");
                result = handleObserver(request);
            }
        }
        return result;
    }

    protected EntityHandlerResult handleInit(OcResourceRequest request) {
        // the entity handler should go and perform initialization operations
        return EntityHandlerResult.OK;
    }

    protected EntityHandlerResult handleRequest(OcResourceRequest request) {
        // the entity handler needs to perform the requested operations (GET/PUT/POST/DELETE)
        EntityHandlerResult result = EntityHandlerResult.ERROR;
        switch (request.getRequestType()) {
            case GET:
                Log.d(TAG, "\t\t[GET]");
                result = handleGetRequest(request);
                break;
            case PUT:
                Log.d(TAG, "\t\t[PUT]");
                result = handlePutRequest(request);
                break;
            case POST:
                Log.d(TAG, "\t\t[POST]");
                result = handlePostRequest(request);
                break;
            case DELETE:
                Log.d(TAG, "\t\t[DELETE]");
                result = handleDeleteRequest(request);
                break;
        }
        return result;
    }

    protected EntityHandlerResult handleGetRequest(OcResourceRequest request) {
        OcResourceResponse response = new OcResourceResponse();
        response.setRequestHandle(request.getRequestHandle());
        response.setResourceHandle(request.getResourceHandle());
        response.setResponseResult(EntityHandlerResult.OK);
        response.setResourceRepresentation(getOcRepresentation());
        return sendResponse(response);
    }

    protected EntityHandlerResult handlePutRequest(OcResourceRequest request) {
        OcResourceResponse response = new OcResourceResponse();
        response.setRequestHandle(request.getRequestHandle());
        response.setResourceHandle(request.getResourceHandle());
        setOcRepresentation(request.getResourceRepresentation());
        response.setResourceRepresentation(getOcRepresentation());
        response.setResponseResult(EntityHandlerResult.OK);
        return sendResponse(response);
    }

    protected EntityHandlerResult handlePostRequest(OcResourceRequest request) {
        OcResourceResponse response = new OcResourceResponse();
        response.setRequestHandle(request.getRequestHandle());
        response.setResourceHandle(request.getResourceHandle());
        setOcRepresentation(request.getResourceRepresentation());
        response.setResourceRepresentation(getOcRepresentation());
        response.setResponseResult(EntityHandlerResult.OK);
        return sendResponse(response);
    }

    protected EntityHandlerResult handleDeleteRequest(OcResourceRequest request) {
        EntityHandlerResult result = EntityHandlerResult.ERROR;
        try {
            OcPlatform.unregisterResource(mHandle);
            result = EntityHandlerResult.RESOURCE_DELETED;
        } catch (OcException e) {
            error(e, "Failed to unregister resource");
            mHandle = null;
        }
        return result;
    }

    protected EntityHandlerResult handleObserver(final OcResourceRequest request) {
        ObservationInfo observationInfo = request.getObservationInfo();
        Byte observationId = observationInfo.getOcObservationId();
        switch (observationInfo.getObserveAction()) {
            case REGISTER:
                mObservationIds.add(observationId);
                break;
            case UNREGISTER:
                mObservationIds.remove(observationId);
                break;
        }
        if (null == mObserverNotifier) {
            mObserverNotifier = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                        try {
                            mObserverNotifier.sleep(mNotifyInterval);
                            if (mObservationIds.size() > 0) {
                                Log.d(TAG, "Notifying observers...");
                                OcPlatform.notifyAllObservers(mHandle);
                            }
                        } catch (OcException | InterruptedException e) {
                            Log.e(TAG, e.toString());
                            mObservationIds.clear();
                        }
                }
            });
            mObserverNotifier.start();
        }
        return EntityHandlerResult.OK;
    }

    protected EntityHandlerResult sendResponse(OcResourceResponse response) {
        EntityHandlerResult result = EntityHandlerResult.ERROR;
        try {
            OcPlatform.sendResponse(response);
            result = EntityHandlerResult.OK;
        } catch (OcException e) {
            error(e, "Failed to send response");
        }
        return result;
    }

    protected void error(OcException e, String msg) {
        Log.e(TAG, msg + "\n" + e.toString());
    }
}