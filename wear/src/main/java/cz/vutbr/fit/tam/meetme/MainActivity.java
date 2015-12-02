package cz.vutbr.fit.tam.meetme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.vutbr.fit.tam.meetme.release.schema.ConnectionData;
import cz.vutbr.fit.tam.meetme.release.service.DataReceiverService;
import cz.vutbr.fit.tam.meetme.release.service.SensorService;


public class MainActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    ArrayList<ConnectionData> data;
    boolean handheldConnected;

    Intent sensorIntent;
    Intent dataIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);

        handheldConnected = true;

        dataIntent = new Intent(this, DataReceiverService.class);
        startService(dataIntent);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                handheldDataReceiver, new IntentFilter(this.getString(R.string.wear_data_intent_filter))
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(
                statusReceiver, new IntentFilter(this.getString(R.string.wear_status_intent_filter))
        );

        sensorIntent = new Intent(this, SensorService.class);
        startService(sensorIntent);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                positionReceiver, new IntentFilter(this.getString(R.string.wear_rotation_intent_filter))
        );
    }

    @Override
    protected void onResume() {
        startService(sensorIntent);
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopService(sensorIntent);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, SensorService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(positionReceiver);

        stopService(new Intent(this, DataReceiverService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(handheldDataReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * -------------- Local Broadcast Receivers ---------------------------------------
     * --------------------------------------------------------------------------------
     */

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float x = Float.parseFloat(intent.getStringExtra(context.getString(R.string.rotation_x)));
        }
    };

    private BroadcastReceiver handheldDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String name = intent.getStringExtra("name");
            int groupId = Integer.parseInt(intent.getStringExtra("groupId"));
            float bearing = Float.parseFloat(intent.getStringExtra("bearing"));
            float distance = Float.parseFloat(intent.getStringExtra("distance"));

            boolean updated = false;

            for (ConnectionData d : data) {

                if (d.isSame(name)) {
                    d.updateData(groupId, bearing, distance);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                data.add(new ConnectionData(name, groupId, bearing, distance));
            }

            // TODO: remove item
        }
    };

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handheldConnected = intent.getBooleanExtra(getString(R.string.wear_connection_status), true);

            if (handheldConnected) {

                // TODO: show dialog (device disconnected)
            }
        }
    };
}
