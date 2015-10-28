package cz.vutbr.fit.tam.meetme.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import cz.vutbr.fit.tam.meetme.R;

/**
 * Created by Jakub on 21. 10. 2015.
 */
public class GPSLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private boolean mRequestingLocationUpdates;

    private static long UPDATE_INTERVAL  = 1000;
    private static long FASTEST_INTERVAL =  500;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public boolean isPeriodicallyUpdated() {

        return mRequestingLocationUpdates;
    }

    public void setPeriodicLocationUpdates(boolean set) {
        mRequestingLocationUpdates = set;

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        else {
            stopLocationUpdates();
        }
    }

    public int onStartCommand(Intent intent, int flags, int id) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        sendLocation();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestingLocationUpdates = true;
        createLocationRequest();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onConnected(Bundle status) {
        sendLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        sendLocation();
    }

    public void onConnectionFailed(ConnectionResult result) {
        if (mRequestingLocationUpdates)
            stopLocationUpdates();
    }

    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    public void sendLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            // TODO ???
            // rounding ( last - new < threshold ) { do not update }
            // double delta_latitude  = Math.abs(last_latitude  - latitude);
            // double delta_longitude = Math.abs(last_longitude - longitude);
            // double delta_distance = Math.pow(delta_latitude, 2) + Math.pow(delta_longitude, 2);
            // if (delta_distance < PRECISION_THRESHOLD) { return; }
            //last_latitude  = latitude;
            //last_longitude = longitude;

            Context context = getApplicationContext();

            Intent intent = new Intent(context.getString(R.string.gps_intent_filter));
            intent.putExtra(context.getString(R.string.gps_latitude), Double.toString(latitude));
            intent.putExtra(context.getString(R.string.gps_longitude), Double.toString(longitude));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        }
        else {
            // TODO: send error code > 0
        }
    }
}
