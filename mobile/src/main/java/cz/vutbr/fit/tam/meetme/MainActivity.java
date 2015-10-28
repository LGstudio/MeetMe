package cz.vutbr.fit.tam.meetme;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.fragments.*;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafterInterface;
import cz.vutbr.fit.tam.meetme.schema.DeviceInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupColor;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.service.GPSLocationService;
import cz.vutbr.fit.tam.meetme.service.GetGroupDataService;
import cz.vutbr.fit.tam.meetme.service.SensorService;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final String LOG_TAG = "MainActivity";
    public static final String GROUP_HASH="group_hash";
    private ArrayList<GroupInfo> groups;

    private boolean isLoggedIn = true;

    private CompassFragment fragCompass;
    private MapViewFragment fragMap;

    private ImageButton gpsStatus;
    private ImageButton netStatus;

    private RequestCrafter resourceCrafter;
    private ServiceConnection mConnection = this;
    private GetGroupDataService.MyLocalBinder binder;
    private String groupHash;
    private static MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.activity = this;

        groups = new ArrayList<>();

        gpsStatus = (ImageButton) findViewById(R.id.toolbar_gps_stat);
        netStatus = (ImageButton) findViewById(R.id.toolbar_net_stat);

        if (!isNetworkAvailable()) {
            // TODO: connect to network
            Log.d("DEBUG", "network disabled");
        }

        if (!isLocationEnabled()) {
            // TODO: enable location
            Log.d("DEBUG", "location disabled");
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
        else {
            // TODO: error
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

    @Override
    public void onStart(){
        super.onStart();
        Log.d("GetGroupDataService", "onStart");
        appInit();
    }

    /**
     * Pokud app je zapla z App drawer -> vytvari group pro sdileni
     * Pokud byla zapla klikem na odkaz -> pripoji se do group a zapne GetGroupDataService
     * */
    private void appInit() {
        final Location loc = new Location("testLocation");

        this.groupHash = getIntentData();
        if(groupHash != null){
            Log.d(LOG_TAG, "joining group:" + groupHash);
            //join group
            new Thread(new Runnable() {
                public void run() {
                    try {
                        GroupInfo gi = resourceCrafter.restGroupAttach(groupHash, loc);
                    }
                    catch(InternalErrorException e){
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    Intent i = new Intent(MainActivity.this, GetGroupDataService.class);
                    i.putExtra(GROUP_HASH, MainActivity.this.groupHash);
                    bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                }
            }).start();
        }
        else{
            //create group
            new Thread(new Runnable() {
                public void run() {
                    try {
                        GroupInfo gi = resourceCrafter.restGroupCreate(loc);
                        MainActivity.this.groupHash = gi.getHash();
                        Log.d(LOG_TAG, "created group:" + gi.getHash());
                    }
                    catch(InternalErrorException e){
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    Intent i = new Intent(MainActivity.this, GetGroupDataService.class);
                    i.putExtra(GROUP_HASH, MainActivity.this.groupHash);
                    bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                }
            }).start();
        }


    }


    @Override
    public void onStop(){
        Log.d("GetGroupDataService", "onStop");
        super.onStop();

        if(this.binder!=null && this.binder.getService().isBinded())
            unbindService(mConnection);
    }


    /**
    * @return groupHash
    * */
    private String getIntentData() {
        if(getIntent().getData()!= null) {
            Log.d(LOG_TAG, "with URL data");

            try{
                //http://scattergoriesonline.net/meetme/groupHash
                Uri data = getIntent().getData();

                List<String> params = data.getPathSegments();
                //String first = params.get(0); // "meetme"
                return params.get(1); // "groupHash"
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
    public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "service binded!");
            MainActivity.this.binder = (GetGroupDataService.MyLocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, GPSLocationService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsReceiver);

        stopService(new Intent(this, SensorService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(positionReceiver);
    }

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String str_x = intent.getStringExtra(context.getString(R.string.rotation_x));
            String str_y = intent.getStringExtra(context.getString(R.string.rotation_y));
            String str_z = intent.getStringExtra(context.getString(R.string.rotation_z));

            float x = Float.parseFloat(str_x);
            float y = Float.parseFloat(str_y);
            float z = Float.parseFloat(str_z);

            // TODO: angle (arrow rotation) = from.bearingTo(to) - x (azimuth)
            // TODO: distanceTo
        }
    };

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String str_latitude = intent.getStringExtra(context.getString(R.string.gps_latitude));
            String str_longitude = intent.getStringExtra(context.getString(R.string.gps_longitude));

            double latitude  = Double.parseDouble(str_latitude);
            double longitude = Double.parseDouble(str_longitude);
        }
    };

    private boolean checkGooglePlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // TODO: show error dialog
            }
            else {
                // TODO: error (not supported device)
            }

            return false;
        }

        return true;
    }

    private void showLoggedInLayout(){
        fragCompass = new CompassFragment();
        fragCompass.putGroups(groups);

        fragMap = new MapViewFragment();
        fragMap.putGroups(groups);

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

        /**
         * DUMMY DATA -------------------------- !!!!!!!!!!!!
         */
        addDummyGroupData();
    }

    /**
     * JUST SOME BULLSHIT FOR DUMMY DATA --------------------------------------
     */

    static final String AB = "ABCDEF GHIJKLM NOPQRS TUVWXYZ abcdefg hijklmno pqrst uvwxyz";
    static Random rnd = new Random();

    private void addDummyGroupData(){

        for (int g = 0; g < 6; g++){
            GroupInfo group = new GroupInfo();
            group.hash = "Group_" + g;
            group.id = g + 1;

            int max = g%4 + 2;

            ArrayList<DeviceInfo> deviceInfoList = new ArrayList<>();

            for (int d = 0; d < max; d++){
                DeviceInfo device = new DeviceInfo();
                device.id = 10 * g + d;
                device.name = randomString(10+d);
                deviceInfoList.add(device);
            }

            group.setDeviceInfoList(deviceInfoList);
            groups.add(group);
        }

    }

    private String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public static MainActivity getActivity() {
        return activity;
    }

    public void showGroupData(GroupInfo gi){
        Toast.makeText(this.getApplicationContext(), gi.id+"", Toast.LENGTH_SHORT).show();
    }

    /**
     * -------------------------------------------------------------------------
     */

}
