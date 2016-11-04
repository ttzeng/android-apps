package com.intel.otc.iot.smarthome;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.iotivity.base.OcException;
import org.iotivity.base.OcResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResourceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ResourceAdapter.class.getSimpleName();
    private enum CardTypes {
        SwitchBinary,
        ColorRGB,
        Motion,
        Gas,
    }

    private Context mContext;
    private Map<String, CardTypes> mSupportedResourceType = new HashMap<>();
    private ArrayList<Pair<CardTypes, OcResource>> mCardList = new ArrayList<>();

    public ResourceAdapter(Context context) {
        mContext = context;
        mSupportedResourceType.put(CardSwitchBinary.RESOURCE_TYPE, CardTypes.SwitchBinary);
        mSupportedResourceType.put(CardColorRGB.RESOURCE_TYPE, CardTypes.ColorRGB);
        mSupportedResourceType.put(CardSensedMotion.RESOURCE_TYPE, CardTypes.Motion);
        mSupportedResourceType.put(CardSensedCarbonDioxide.RESOURCE_TYPE, CardTypes.Gas);
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType(" + position + ")");
        return mCardList.get(position).first.ordinal();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder(" + viewType + ")");
        RecyclerView.ViewHolder card = null;
        if (viewType == CardTypes.SwitchBinary.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_onoff, parent, false);
            card = new CardSwitchBinary(v, mContext);
        } else if (viewType == CardTypes.ColorRGB.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_colorpicker, parent, false);
            card = new CardColorRGB(v, mContext);
        } else if (viewType == CardTypes.Motion.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_icon, parent, false);
            card = new CardSensedMotion(v, mContext);
        } else if (viewType == CardTypes.Gas.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_icon, parent, false);
            card = new CardSensedCarbonDioxide(v, mContext);
        }
        return card;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder(" + position + ")");
        ((CardOcResource) holder).bindResource(mCardList.get(position).second);
    }

    @Override
    public int getItemCount() {
        return mCardList.size();
    }

    public void add(final OcResource resource) {
        for (String rt : resource.getResourceTypes())
            if (mSupportedResourceType.containsKey(rt)) {
                mCardList.add(new Pair<CardTypes, OcResource>(mSupportedResourceType.get(rt), resource));
            }
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void clear() {
        try {
            for (Pair<CardTypes, OcResource> card : mCardList)
                card.second.cancelObserve();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
        }
        mCardList.clear();
        notifyDataSetChanged();
    }
}
