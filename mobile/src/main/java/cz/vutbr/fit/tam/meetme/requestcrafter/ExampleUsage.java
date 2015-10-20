package cz.vutbr.fit.tam.meetme.requestcrafter;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import cz.vutbr.fit.tam.meetme.MainActivity;
import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;

/**
 * Created by Lada on 20.10.2015.
 */
public class ExampleUsage {

    public static final String LOG_TAG = "test";

    public static void tryRequestCrafter(final Context context){

        new Thread(new Runnable() {
            public void run() {

                RequestCrafter rc = new RequestCrafter(System.getProperty("http.agent","NO USER AGENT"), context);
                Location loc = new Location("testLocation");

                try {
                    GroupInfo gi = rc.restGroupCreate(loc);

                    GroupInfo gi3 = rc.restGroupData(gi.getHash(), loc);

                    rc.restGroupDetach(gi.getHash());

                    GroupInfo gi4 = rc.restGroupAttach(gi.getHash(), loc);

                    GroupInfo gi5 = rc.restGroupData(gi.getHash(), loc);

                    loc.setLatitude(1.548799);
                    loc.setLongitude(0.988411);
                    GroupInfo gi6 = rc.restGroupData(gi.getHash(), loc);
                }
                catch(InternalErrorException e){
                    Log.e(LOG_TAG, e.getMessage());
                }



            }
        }).start();
    }
}
