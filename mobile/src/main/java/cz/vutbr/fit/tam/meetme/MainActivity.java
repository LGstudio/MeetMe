package cz.vutbr.fit.tam.meetme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import cz.vutbr.fit.tam.meetme.asynctasks.WearableSendAsyncTask;
import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.fragments.*;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.schema.AllConnectionData;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.service.GPSLocationService;
import cz.vutbr.fit.tam.meetme.service.GetGroupDataService;
import cz.vutbr.fit.tam.meetme.service.SensorService;

public class MainActivity extends AppCompatActivity implements ServiceConnection, View.OnClickListener {

    private static final String LOG_TAG = "MainActivity";
    public static final String GROUP_HASH="group_hash";
    private final int NOTIFICATION_ID = 1;

    private boolean isMapShowed = false;
    private CompassFragment fragCompass;
    private MapViewFragment fragMap;
    private CustomViewPager viewPager;

    private AllConnectionData data;

    private RelativeLayout toolbar;
    private ImageButton gpsStatus;
    private ImageButton backToCompass;
    private TextView showMap;

    private RequestCrafter resourceCrafter;
    private ServiceConnection mConnection = this;
    private boolean mIsBound;
    private GetGroupDataService.MyLocalBinder binder;
    private String newUrlGroupHash;

    private static MainActivity activity;
    private static SharedPreferences prefs;

    private GoogleApiClient googleApiClient;
    private boolean wearableConnected;

    /**
     * --------------------------------------------------------------------------------
     * ------------- Activity Lifecycle -----------------------------------------------
     * --------------------------------------------------------------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.activity = this;

        if (checkGooglePlayServices()) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();

            if (!googleApiClient.isConnected())
                googleApiClient.connect();
        }

        wearableConnected = false;
        new WearConnectionAsyncTask(googleApiClient).execute();

        prefs = this.getSharedPreferences("cz.vutbr.fit.tam.meetme", Context.MODE_PRIVATE);


        initLayout();

        if (!isNetworkAvailable()) {
            MainActivity.this.showNetworkDialog();
        }

        if (isNetworkAvailable() && !isLocationEnabled()) {
            MainActivity.this.showLocationDialog();
        }

        if (checkGooglePlayServices()) {

            startService(new Intent(this, GPSLocationService.class));
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    gpsReceiver, new IntentFilter(this.getString(R.string.gps_intent_filter))
            );
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("GetGroupDataService", "onResume");
        handleOpenViaUrl();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        stopService(new Intent(this, GPSLocationService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsReceiver);

        stopService(new Intent(this, SensorService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(positionReceiver);

        dismissNotification();
        doUnbindService();

        prefs.edit().putString(getString(R.string.pref_last_lat), String.valueOf(data.myLatitude)).apply();
        prefs.edit().putString(getString(R.string.pref_last_lon), String.valueOf(data.myLongitude)).apply();
    }

    /**
     * --------------------------------------------------------------------------------
     * ------------- Init activity ----------------------------------------------------
     * --------------------------------------------------------------------------------
     */

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
                        MainActivity.this.showNotification();
                        doBindService(MainActivity.this.newUrlGroupHash);
                    }
                    catch(InternalErrorException e){
                        Log.e(LOG_TAG, e.getMessage());
                    }
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

    /**
     * Initializes the layout with default data
     */
    private void initLayout(){

        toolbar = (RelativeLayout) findViewById(R.id.toolbar);

        gpsStatus = (ImageButton) findViewById(R.id.toolbar_gps_stat);
        showMap = (TextView) findViewById(R.id.toolbar_map);
        backToCompass = (ImageButton) findViewById(R.id.toolbar_back);

        gpsStatus.setOnClickListener(this);
        showMap.setOnClickListener(this);
        backToCompass.setOnClickListener(this);

        data = new AllConnectionData(this);

        data.myLatitude = Double.parseDouble(prefs.getString(getString(R.string.pref_last_lat), "0.0"));
        data.myLongitude = Double.parseDouble(prefs.getString(getString(R.string.pref_last_lon), "0.0"));

        data.myName = prefs.getString(getString(R.string.pref_name), null);

        //TODO : REMOVE TEST DATA
        data.addShit();
        //TODO : -----------------

        viewPager = (CustomViewPager) findViewById(R.id.pager);
        viewPager.setPagingEnabled(false);

        if (data.myName == null){
            showWelcome();
        }
        else {
            resourceCrafter = new RequestCrafter(System.getProperty("http.agent","NO USER AGENT"), data.myName, this.getApplicationContext());
            showLoggedIn();
        }

    }

    public void showLoggedIn(){
        toolbar.setVisibility(View.VISIBLE);

        fragCompass = new CompassFragment();
        fragCompass.addData(data);
        MapsInitializer.initialize(getApplicationContext());
        fragMap = new MapViewFragment();
        fragMap.addData(data);

        final TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(fragCompass);
        adapter.addFragment(fragMap);
        viewPager.setAdapter(adapter);

        startPositionReceiver();

        if (wearableConnected) {
            new WearableSendAsyncTask(getApplicationContext(), googleApiClient, data.getDataMap()).execute();
        }
    }

    public void showWelcome(){
        toolbar.setVisibility(View.GONE);

        WelcomeScreen welcome = new WelcomeScreen();

        final TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(welcome);
        viewPager.setAdapter(adapter);
    }

    /**
     * Checks if network connection is available
     * @return
     */
    protected boolean isNetworkAvailable() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Checks if location service is enabled
     * @return
     */
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


    /**
     * --------------------------------------------------------------------------------
     *  ------------- Notifications ---------------------------------------------------
     *  --------------------------------------------------------------------------------
     */

    public void showNotification() {
        Intent resultIntent = new Intent(this, MainActivity.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent takeToMeetMeIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_gps)
                        .setContentTitle("MeetMe")
                        .setContentText("Meeting is running...")
                        .setContentIntent(takeToMeetMeIntent);


        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void dismissNotification(){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Sets an ID for the notification, so it can be updated
        int notifyID = 1;

        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void showNetworkDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.network_dialog_title);

        alertDialogBuilder.setMessage(R.string.network_dialog_text)
                .setPositiveButton(R.string.network_dialog_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();

                        if (!isNetworkAvailable()) {
                            showNetworkDialog();
                        }
                        else {
                            showLocationDialog();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MainActivity.this.finish();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showLocationDialog() {

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
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Opens location settings window
     */
    private void openLocationSettings() {
        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
    }

    /**
     * --------------------------------------------------------------------------------
     * ------------ Services ----------------------------------------------------------
     * --------------------------------------------------------------------------------
     */

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "service binded!");
            MainActivity.this.binder = (GetGroupDataService.MyLocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public void doBindService(String groupHash) {
        Intent i = new Intent(MainActivity.this, GetGroupDataService.class);
        i.putExtra(GROUP_HASH, groupHash);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void doUnbindService() {
        if (mIsBound) {
            Log.d(LOG_TAG, "service UNbinded!");
            unbindService(mConnection);
            mIsBound = false;
        }
    }

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

    public GetGroupDataService.MyLocalBinder getBinder() {
        return binder;
    }

    /**
     * --------------------------------------------------------------------------------
     * -------------- Broadcast Receivers ---------------------------------------------
     * --------------------------------------------------------------------------------
     */

    private void startPositionReceiver(){
        startService(new Intent(this, SensorService.class));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                positionReceiver, new IntentFilter(this.getString(R.string.rotation_intent_filter))
        );
    }

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            float x = Float.parseFloat(intent.getStringExtra(context.getString(R.string.rotation_x)));
            fragCompass.setDeviceRotation(x);
        }
    };

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            data.myLatitude = Double.parseDouble(intent.getStringExtra(context.getString(R.string.gps_latitude)));
            data.myLongitude = Double.parseDouble(intent.getStringExtra(context.getString(R.string.gps_longitude)));

            if (wearableConnected) {
                new WearableSendAsyncTask(getApplicationContext(), googleApiClient, data.getDataMap()).execute();
            }
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * -------------- Click Listener --------------------------------------------------
     * --------------------------------------------------------------------------------
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolbar_gps_stat:
                MainActivity.this.openLocationSettings();
                break;
            case R.id.toolbar_map:
                showMap.setVisibility(View.GONE);
                backToCompass.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(1);
                isMapShowed = true;
                fragMap.updateLocations();
                break;
            case R.id.toolbar_back:
                showMap.setVisibility(View.VISIBLE);
                backToCompass.setVisibility(View.GONE);
                isMapShowed = false;
                viewPager.setCurrentItem(0);
                break;
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * ----------- Share and Receive async task stuff ---------------------------------
     * --------------------------------------------------------------------------------
     */

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

        if (wearableConnected) {
            new WearableSendAsyncTask(getApplicationContext(), googleApiClient, data.getDataMap()).execute();
        }
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
            fragCompass.updateView();
            if (isMapShowed) fragMap.updateLocations();
        }


    }

    /**
     * --------------------------------------------------------------------------------
     * ----------- Welcome screen to enter username -----------------------------------
     * --------------------------------------------------------------------------------
     */

    public class WelcomeScreen extends Fragment implements View.OnClickListener{

        private EditText nameField;
        private Button button;
        private TextView information;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_welcome, container, false);

            button = (Button) v.findViewById(R.id.welcome_ok_button);
            button.setOnClickListener(this);
            nameField = (EditText) v.findViewById(R.id.welcome_name_field);
            information = (TextView) v.findViewById(R.id.welcome_information);

            return v;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.welcome_ok_button){
                String name = nameField.getText().toString();

                if (name.length() < 1 ){
                    information.setText(getString(R.string.intro_info_warning));
                    information.setTextColor(getResources().getColor(R.color.indication_bad));
                }
                else {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    data.myName = name;
                    prefs.edit().putString(getString(R.string.pref_name), name).apply();
                    showLoggedIn();
                }
            }
        }


    }

    /**
     * --------------------------------------------------------------------------------
     * ----------- Wearable connection check and listeners ----------------------------
     * --------------------------------------------------------------------------------
     */
    public class WearConnectionAsyncTask extends AsyncTask<Void,Void,Void> {

        GoogleApiClient googleApiClient;

        public WearConnectionAsyncTask(GoogleApiClient googleApiClient) {
            this.googleApiClient = googleApiClient;
        }

        @Override
        protected Void doInBackground(Void... params) {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            wearableConnected = (nodes != null && nodes.getNodes().size() > 0);
            return null;
        }
    }
}
