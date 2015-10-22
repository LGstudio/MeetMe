package cz.vutbr.fit.tam.meetme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Random;

import cz.vutbr.fit.tam.meetme.fragments.*;
import cz.vutbr.fit.tam.meetme.schema.DeviceInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupColor;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.service.GPSLocationService;
import cz.vutbr.fit.tam.meetme.service.SensorService;

public class MainActivity extends AppCompatActivity {

    private GroupColor groupColors;
    private ArrayList<GroupInfo> groups;

    private boolean isLoggedIn = true;

    private CompassFragment fragCompass;
    private MapViewFragment fragMap;

    private ImageButton gpsStatus;
    private ImageButton netStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        groupColors = new GroupColor();
        groups = new ArrayList<>();

        gpsStatus = (ImageButton) findViewById(R.id.toolbar_gps_stat);
        netStatus = (ImageButton) findViewById(R.id.toolbar_net_stat);

        if (isLoggedIn){
            showLoggedInLayout();
        }
        else {
            //TODO: LOGIN SCREEN
        }


        if (!checkGooglePlayServices()) {
            // TODO: error
        }

        startService(new Intent(this, SensorService.class));
        startService(new Intent(this, GPSLocationService.class));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                gpsReceiver, new IntentFilter(this.getString(R.string.gps_intent_filter))
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                positionReceiver, new IntentFilter(this.getString(R.string.rotation_intent_filter))
        );

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

            Context app_context = getApplicationContext();

            float x = Float.parseFloat(app_context.getString(R.string.rotation_x));
            float y = Float.parseFloat(app_context.getString(R.string.rotation_y));
            float z = Float.parseFloat(app_context.getString(R.string.rotation_z));

            // TODO: angle (arrow rotation) = from.bearingTo(to) - x (azimuth)
            // TODO: distanceTo
        }
    };

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Context app_context = getApplicationContext();

            double latitude  = Double.parseDouble(app_context.getString(R.string.gps_latitude));
            double longitude = Double.parseDouble(app_context.getString(R.string.gps_longitude));
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

    /**
     * -------------------------------------------------------------------------
     */

}
