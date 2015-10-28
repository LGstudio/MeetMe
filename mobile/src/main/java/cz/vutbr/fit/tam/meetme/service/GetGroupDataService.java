package cz.vutbr.fit.tam.meetme.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import cz.vutbr.fit.tam.meetme.MainActivity;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;

/**
 * Created by Lada on 26.10.2015.
 */
public class GetGroupDataService extends Service {

    public static final String LOG_TAG = "GetGroupDataService";
    public static final long PERIOD = 5000L;

    private final IBinder localBinder = new MyLocalBinder();
    private Timer mTimer = new Timer();
    private String groupHash;
    private RequestCrafter resourceCrafter;



    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "Service Started.");

        this.resourceCrafter = new RequestCrafter(System.getProperty("http.agent","NO USER AGENT"), this.getApplicationContext());

        mTimer.scheduleAtFixedRate(new GetGroupDataTask(), 0, PERIOD);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(intent != null){
            this.groupHash = intent.getStringExtra(MainActivity.GROUP_HASH);
        }

        return localBinder;
    }

    public void sendMessageActivity(final GroupInfo  gi){
        //Log.i(LOG_TAG, "group info: " + gi.toString());
        MainActivity.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                MainActivity.getActivity().showGroupData(gi);
            }
        });
    }


    public class MyLocalBinder extends Binder {
        GetGroupDataService getService() {
            return GetGroupDataService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }
    //////////////////////////////////////////
    // Nested classes
    /////////////////////////////////////////

    /**
     * The task to run...
     */
    private class GetGroupDataTask extends TimerTask {

        public static final String LOG_TAG = "GetGroupDataTask";
        final Location loc = new Location("testLocation");

        public GetGroupDataTask(){
            super();
        }

        @Override
        public void run() {
            if(GetGroupDataService.this.groupHash != null){
            try {

                GroupInfo gi = resourceCrafter.restGroupData(groupHash, loc);
                sendMessageActivity(gi);
   // Log.d(LOG_TAG, gi.getHash());

            } catch (Exception e) { //you should always ultimately catch all exceptions in timer tasks.
                Log.e("TimerTick", "Timer Tick Failed: "+ e.getMessage());
            }
            }
        }
    }
}
