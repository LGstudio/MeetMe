package cz.vutbr.fit.tam.meetme;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Random;

import cz.vutbr.fit.tam.meetme.data.*;
import cz.vutbr.fit.tam.meetme.fragments.*;

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

        /**
         * Start Services here
         */

        gpsStatus = (ImageButton) findViewById(R.id.toolbar_gps_stat);
        netStatus = (ImageButton) findViewById(R.id.toolbar_net_stat);

        if (isLoggedIn){
            showLoggedInLayout();
        }
        else {
            //TODO: LOGIN SCREEN
        }
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
            group.groupHash = "Group_" + g;
            group.groupColor = groupColors.getNextColor();
            group.groupId = g + 1;

            int max = g%4 + 2;

            for (int d = 0; d < max; d++){
                DeviceInfo device = new DeviceInfo();
                device.deviceId = 10*g + d;
                device.name = randomString(10+d);
                group.addDevice(device);
            }

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
