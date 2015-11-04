package cz.vutbr.fit.tam.meetme.asynctasks;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import cz.vutbr.fit.tam.meetme.MainActivity;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.schema.AllConnectionData;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;

/**
 * Created by Lada on 2.11.2015.
 */
public class GroupLeaveAsyncTask  extends AsyncTask<Void,Void,Void> {
        private final String TAG = "GroupLeaveAsyncTask";
        private RequestCrafter resourceCrafter;
        private Context context;
        private Location loc;
        private GroupInfo group = null;
        private String shareMsg;
        private int selectedGroup;
        private AllConnectionData data;

        public GroupLeaveAsyncTask(Context context, int selectedGroup, AllConnectionData data, RequestCrafter rc){
            this.resourceCrafter = rc;
            this.context = context.getApplicationContext();
            this.loc = new Location("asyncTaskLoc");
            this.selectedGroup = selectedGroup;
            this.data = data;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (selectedGroup == 0) {
                for (int i = 1; i < data.groups.size(); i++) {
                    try {
                        resourceCrafter.restGroupDetach(data.groups.get(i).hash);
                    } catch (Exception e){
                        Log.d(TAG, "Exception during detaching from all groups index:"+ i + ", msg:"+ e.getMessage());
                    }
                }

            }
            else {

                try {
                    resourceCrafter.restGroupDetach(data.groups.get(selectedGroup).hash);
                } catch (Exception e){
                    Log.d(TAG, e.getMessage());
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //unbind service
            MainActivity.getActivity().doUnbindGroupDataService();

            Log.d(TAG, "GroupLeave async task successful");
        }

    }
