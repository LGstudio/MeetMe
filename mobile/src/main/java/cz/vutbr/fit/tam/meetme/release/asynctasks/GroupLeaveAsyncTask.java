package cz.vutbr.fit.tam.meetme.release.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import cz.vutbr.fit.tam.meetme.release.MainActivity;
import cz.vutbr.fit.tam.meetme.release.requestcrafter.RequestCrafter;

/**
 * Created by Lada on 2.11.2015.
 */
public class GroupLeaveAsyncTask  extends AsyncTask<Void,Void,Void> {
        private final String TAG = "GroupLeaveAsyncTask";
        private RequestCrafter resourceCrafter;
        private String hashToDetach;

        public GroupLeaveAsyncTask(String hash, RequestCrafter rc){
            this.resourceCrafter = rc;
            this.hashToDetach = hash;
        }

        @Override
        protected Void doInBackground(Void... params) {
                try {
                    resourceCrafter.restGroupDetach(this.hashToDetach);
                } catch (Exception e){
                    Log.d(TAG, e.getMessage());
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //unbind service
            MainActivity.getActivity().doUnbindService();

            Log.d(TAG, "GroupLeave async task successful");
        }

    }
