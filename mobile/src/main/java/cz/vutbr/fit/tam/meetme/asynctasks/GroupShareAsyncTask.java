package cz.vutbr.fit.tam.meetme.asynctasks;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import cz.vutbr.fit.tam.meetme.MainActivity;
import cz.vutbr.fit.tam.meetme.R;
import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;

/**
 * Created by Lada on 29.10.2015.
 */
public class GroupShareAsyncTask extends AsyncTask<Void,Void,Void> {
        private final String TAG="GroupShareAsyncTask";

        private RequestCrafter resourceCrafter;
        private Context context;
        private Location loc;
        private GroupInfo group;

        public GroupShareAsyncTask(Context context, RequestCrafter rc, Location loc){
            this(context, rc, loc, null);
        }

        public GroupShareAsyncTask(Context context, RequestCrafter rc, Location loc, GroupInfo group){
            this.resourceCrafter = rc;
            this.context = context.getApplicationContext();
            this.loc = loc;
            this.group = group;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if(this.group == null) {
                //group hash is null -> create new group
                try {
                    this.group = this.resourceCrafter.restGroupCreate(this.loc);
                } catch (InternalErrorException e) {
                    Log.d(TAG, "exception: " + e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String shareUrl = this.context.getString(R.string.share_link) + this.group.hash;

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(android.content.Intent.EXTRA_TEXT, shareUrl);
            MainActivity.getActivity().startActivity(Intent.createChooser(i, shareUrl));

        }

}
