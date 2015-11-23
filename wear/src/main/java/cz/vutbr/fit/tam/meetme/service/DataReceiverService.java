package cz.vutbr.fit.tam.meetme.service;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

import cz.vutbr.fit.tam.meetme.R;

/**
 * Created by Jakub on 21. 11. 2015.
 */
public class DataReceiverService extends WearableListenerService {

    GoogleApiClient mGoogleApiClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();
        }

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {

            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();

                if (!path.equals("/myapp/myevent")) {
                    continue;
                }

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                DataMap dataMap = dataMapItem.getDataMap();

                ArrayList<DataMap> dataMapList = dataMap.getDataMapArrayList("data");

                if (dataMapList == null) {
                    continue;
                }

                Context context = getApplicationContext();

                for (DataMap data : dataMapList) {
                    Intent intent = new Intent(context.getString(R.string.data_intent_filter));
                    intent.putExtra("name", data.getString("name"));
                    intent.putExtra("groupId", data.getInt("groupId"));
                    intent.putExtra("bearing", data.getFloat("bearing"));
                    intent.putExtra("distance", data.getFloat("distance"));
                }
            }
        }
    }
}
