package cz.vutbr.fit.tam.meetme.asynctasks;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;

/**
 * Created by Lada on 29.10.2015.
 */
public class GroupShareAsyncTask extends AsyncTask<Void,Void,Void> {
        private final String TAG="GroupShareAsyncTask";

        private RequestCrafter resourCrafter;
        private Location loc;
        private GroupInfo group;

        public GroupShareAsyncTask(RequestCrafter rc, Location loc){
            this.resourCrafter = rc;
            this.loc = loc;
            this.group = null;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                this.group = this.resourCrafter.restGroupCreate(this.loc);
            } catch (InternalErrorException e) {
                Log.d(TAG, "exception: "+e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }

}
