package cz.vutbr.fit.tam.meetme.release.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import cz.vutbr.fit.tam.meetme.R;

/**
 * Created by Jakub on 25. 11. 2015.
 */
public class WearConnectionService extends WearableListenerService {

    public boolean connected;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        connected = intent.getBooleanExtra(context.getString(R.string.wear_init_status), false);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        connected = true;
        sendState();
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        connected = false;
        sendState();
    }

    public void sendState() {
        Context context = getApplicationContext();

        Intent intent = new Intent(context.getString(R.string.wear_intent_filter));
        intent.putExtra(context.getString(R.string.wear_connection_status), connected);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
