package cz.vutbr.fit.tam.meetme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;

import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.fragments.*;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.schema.AllConnectionData;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.service.GPSLocationService;
import cz.vutbr.fit.tam.meetme.service.GetGroupDataService;
import cz.vutbr.fit.tam.meetme.service.SensorService;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final String LOG_TAG = "MainActivity";
    public static final String GROUP_HASH="group_hash";

    private boolean isLoggedIn = true;

    private CompassFragment fragCompass;
    private MapViewFragment fragMap;

    private AllConnectionData data;

    private ImageButton gpsStatus;
    private ImageButton netStatus;

    private RequestCrafter resourceCrafter;
    private ServiceConnection mConnection = this;
    private GetGroupDataService.MyLocalBinder binder;
    private String newUrlGroupHash;

    private static MainActivity activity;
    private static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.activity = this;

        prefs = this.getSharedPreferences("cz.vutbr.fit.tam.meetme", Context.MODE_PRIVATE);

        gpsStatus = (ImageButton) findViewById(R.id.toolbar_gps_stat);
        netStatus = (ImageButton) findViewById(R.id.toolbar_net_stat);

        netStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.openNetworkSettings();
            }
        });

        gpsStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.openLocationSettings();
            }
        });

        if (!isNetworkAvailable()) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle(R.string.network_dialog_title);

            alertDialogBuilder.setMessage(R.string.network_dialog_text)
                    .setCancelable(false)
                    .setPositiveButton(R.string.network_dialog_positive,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            MainActivity.this.openNetworkSettings();
                        }
                    })
                    .setNegativeButton(R.string.network_dialog_negative,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                            //MainActivity.this.finish();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        if (!isLocationEnabled()) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle(R.string.location_dialog_title);

            alertDialogBuilder.setMessage(R.string.location_dialog_text)
                    .setPositiveButton(R.string.location_dialog_positive, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.openLocationSettings();
                        }
                    })
                    .setNegativeButton(R.string.location_dialog_negative, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        resourceCrafter = new RequestCrafter(System.getProperty("http.agent","NO USER AGENT"), this.getApplicationContext());

        if (isLoggedIn){
            showLoggedInLayout();
        }
        else {
            //TODO: LOGIN SCREEN
        }

        startService(new Intent(this, SensorService.class));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                positionReceiver, new IntentFilter(this.getString(R.string.rotation_intent_filter))
        );

        if (checkGooglePlayServices()) {

            startService(new Intent(this, GPSLocationService.class));
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    gpsReceiver, new IntentFilter(this.getString(R.string.gps_intent_filter))
            );
        }
    }

    protected boolean isNetworkAvailable() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected boolean isLocationEnabled() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean gps;
        boolean net;

        try {
            gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            net = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            return false;
        }

        return (gps && net);
    }

    private void openNetworkSettings() {
        startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
    }

    private void openLocationSettings() {
        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("GetGroupDataService", "onResume");
        handleOpenViaUrl();
    }

    /**
     * Handles tha case when the app was opened by an url
     * Starts GetGroupDataService and connects into a group
     */
    private void handleOpenViaUrl() {
        final Location loc = new Location("testLocation");

        this.newUrlGroupHash = getIntentData();
        if(newUrlGroupHash != null){
            Log.d(LOG_TAG, "joining group:" + newUrlGroupHash);
            //join group
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loc.setLatitude(MainActivity.this.data.myLatitude);
                        loc.setLongitude(MainActivity.this.data.myLongitude);

                        GroupInfo gi = resourceCrafter.restGroupAttach(newUrlGroupHash, loc);
                    }
                    catch(InternalErrorException e){
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    Intent i = new Intent(MainActivity.this, GetGroupDataService.class);
                    i.putExtra(GROUP_HASH, MainActivity.this.newUrlGroupHash);
                    bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                }
            }).start();
        }
    }

    /**
    * @return newUrlGroupHash
    * */
    private String getIntentData() {
        if(getIntent().getData()!= null) {
            Log.d(LOG_TAG, "with URL data");

            try{
                //http://scattergoriesonline.net/meetme/groupHash
                Uri data = getIntent().getData();

                List<String> params = data.getPathSegments();
                //String first = params.get(0); // "meetme"
                return params.get(1); // "newUrlGroupHash"
            }catch (Exception e){
                Log.d(LOG_TAG, "Exception durring intent.gedData(): " + e.getMessage());
            }
        }
        else{
            Log.d(LOG_TAG, "without URL data");
        }

        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "service binded!");
            MainActivity.this.binder = (GetGroupDataService.MyLocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public void unbindService(){
        if(this.binder!=null && this.binder.getService().isBinded()) {
            Log.d(LOG_TAG, "service UNbinded!");
            unbindService(mConnection);
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        unbindService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, GPSLocationService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsReceiver);

        stopService(new Intent(this, SensorService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(positionReceiver);

        prefs.edit().putString(getString(R.string.pref_last_lat), String.valueOf(data.myLatitude)).apply();
        prefs.edit().putString(getString(R.string.pref_last_lon), String.valueOf(data.myLongitude)).apply();
    }

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

          /*  String str_x = intent.getStringExtra(context.getString(R.string.rotation_x));
            String str_y = intent.getStringExtra(context.getString(R.string.rotation_y));
            String str_z = intent.getStringExtra(context.getString(R.string.rotation_z));

            float x = Float.parseFloat(str_x);
            float y = Float.parseFloat(str_y);
            float z = Float.parseFloat(str_z);
*/
            // TODO: angle (arrow rotation) = from.bearingTo(to) - x (azimuth)
            // TODO: distanceTo
        }
    };

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            data.myLatitude = Double.parseDouble(intent.getStringExtra(context.getString(R.string.gps_latitude)));
            data.myLongitude = Double.parseDouble(intent.getStringExtra(context.getString(R.string.gps_longitude)));

        }
    };

    private boolean checkGooglePlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Dialog err = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1000);

                if (err != null)
                    err.show();
            }
            else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle(R.string.googleplay_dialog_title);

                alertDialogBuilder.setMessage(R.string.googleplay_dialog_text)
                        .setCancelable(false)
                        .setPositiveButton(R.string.googleplay_dialog_positive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

            return false;
        }

        return true;
    }

    private void showLoggedInLayout(){
        data = new AllConnectionData(this);

        data.myLatitude = Double.parseDouble(prefs.getString(getString(R.string.pref_last_lat), "0.0"));
        data.myLongitude = Double.parseDouble(prefs.getString(getString(R.string.pref_last_lon), "0.0"));

        fragCompass = new CompassFragment();
        fragCompass.addData(data);
        fragMap = new MapViewFragment();
        fragMap.addData(data);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Compass"));
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final TabAdapter adapter = new TabAdapter(getSupportFragmentManager());

        adapter.addFragment(fragCompass);
        adapter.addFragment(fragMap);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    public AllConnectionData getData() {
        return data;
    }

    public static MainActivity getActivity() {
        return activity;
    }

    public ServiceConnection getmConnection() {
        return mConnection;
    }

    public RequestCrafter getResourceCrafter() {
        return resourceCrafter;
    }

    public void showGroupData(GroupInfo g){
        GroupUpdaterTask groupUpdaterTask = new GroupUpdaterTask(g);
        groupUpdaterTask.execute((Void) null);
    }

    private class GroupUpdaterTask extends AsyncTask<Void,Void,Void>{

        private GroupInfo group;

        public GroupUpdaterTask(GroupInfo g){
            group = g;
        }

        @Override
        protected Void doInBackground(Void... params) {
            data.updateGroupInfo(group);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fragCompass.changeSpinnerData();
            fragMap.updateLocations();
        }
    }
}
