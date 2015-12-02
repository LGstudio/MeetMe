package cz.vutbr.fit.tam.meetme.release.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

import cz.vutbr.fit.tam.meetme.R;

/**
 * Created by Jakub on 21. 11. 2015.
 */
public class DataReceiverService extends WearableListenerService {

    GoogleApiClient mGoogleApiClient;
    boolean connected;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();
        }

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        new ConnectionAsyncTask(mGoogleApiClient).execute();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();

        stopSelf();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        if (!connected) {
            return;
        }

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
                    Intent intent = new Intent(context.getString(R.string.wear_data_intent_filter));
                    intent.putExtra("name", data.getString("name"));
                    intent.putExtra("groupId", data.getInt("groupId"));
                    intent.putExtra("bearing", data.getFloat("bearing"));
                    intent.putExtra("distance", data.getFloat("distance"));

                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            }
        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        connected = true;
        sendConnectionStatus();
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        connected = false;
        sendConnectionStatus();
    }

    public void sendConnectionStatus() {
        Context context = getApplicationContext();

        Intent intent = new Intent(context.getString(R.string.wear_status_intent_filter));
        intent.putExtra(context.getString(R.string.wear_connection_status), connected);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public class ConnectionAsyncTask extends AsyncTask<Void,Void,Void> {

        GoogleApiClient googleApiClient;

        public ConnectionAsyncTask(GoogleApiClient googleApiClient) {
            this.googleApiClient = googleApiClient;
        }

        @Override
        protected Void doInBackground(Void... params) {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            connected = (nodes != null && nodes.getNodes().size() > 0);
            sendConnectionStatus();
            return null;
        }
    }
}
