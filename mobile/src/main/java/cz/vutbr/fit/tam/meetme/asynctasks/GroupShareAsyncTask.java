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
import cz.vutbr.fit.tam.meetme.schema.AllConnectionData;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.service.GetGroupDataService;

public class GroupShareAsyncTask extends AsyncTask<Void,Void,Void> {
    private final String TAG="GroupShareAsyncTask";

    private RequestCrafter resourceCrafter;
    private Context context;
    private Location loc;
    private GroupInfo group = null;
    private String shareMsg;
    private int selectedGroup;
    private AllConnectionData data;

    public GroupShareAsyncTask(Context context, int selectedGroup, AllConnectionData data, RequestCrafter rc){
        this.resourceCrafter = rc;
        this.context = context.getApplicationContext();
        this.loc = new Location("asyncTaskLoc");
        this.selectedGroup = selectedGroup;
        this.data = data;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (selectedGroup == 0 || data.groups.get(selectedGroup) == null) {
            try {
                this.loc.setLatitude(MainActivity.getActivity().getData().myLatitude);
                this.loc.setLongitude(MainActivity.getActivity().getData().myLongitude);

                this.group = this.resourceCrafter.restGroupCreate(this.loc);
                data.updateGroupInfo(this.group);
                this.shareMsg = this.context.getString(R.string.share_msg_new);
            } catch (InternalErrorException e) {
                Log.d(TAG, "exception: " + e.getMessage());
            }
        }
        else {
            this.group = data.groups.get(selectedGroup);
            this.shareMsg = this.context.getString(R.string.share_msg_existing);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // bind to service
        Intent intent1 = new Intent(MainActivity.getActivity(), GetGroupDataService.class);
        intent1.putExtra(MainActivity.GROUP_HASH, this.group.hash);
        MainActivity.getActivity().bindService(intent1, MainActivity.getActivity().getmConnection(), Context.BIND_AUTO_CREATE);


        String shareUrl = this.context.getString(R.string.share_link) + this.group.hash;

        Intent intent2 = new Intent(Intent.ACTION_SEND);
        intent2.setType("text/plain");
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent2.putExtra(android.content.Intent.EXTRA_TEXT, shareUrl);
        MainActivity.getActivity().startActivity(Intent.createChooser(intent2, this.shareMsg + " (" + this.group.id + ")"));

    }

}