package cz.vutbr.fit.tam.meetme.release.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
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
    private LocationAvailability mLocationAvailability;
    private Location mLastLocation;
    private boolean mRequestingLocationUpdates;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(GetGroupDataService.PERIOD)
                .setFastestInterval(GetGroupDataService.PERIOD);
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

        mLocationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);

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
            
            Context context = getApplicationContext();

            Intent intent = new Intent(context.getString(R.string.gps_intent_filter));
            intent.putExtra(context.getString(R.string.gps_latitude), Double.toString(latitude));
            intent.putExtra(context.getString(R.string.gps_longitude), Double.toString(longitude));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}