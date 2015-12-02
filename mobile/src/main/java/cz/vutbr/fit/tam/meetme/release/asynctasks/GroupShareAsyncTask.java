package cz.vutbr.fit.tam.meetme.release.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import cz.vutbr.fit.tam.meetme.release.MainActivity;
import cz.vutbr.fit.tam.meetme.R;
import cz.vutbr.fit.tam.meetme.release.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.release.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.release.schema.AllConnectionData;
import cz.vutbr.fit.tam.meetme.release.schema.GroupInfo;

public class GroupShareAsyncTask extends AsyncTask<Void,Void,Void> {
    private final String TAG="GroupShareAsyncTask";

    private RequestCrafter resourceCrafter;
    private Context context;
    private Location loc;
    private GroupInfo group = null;
    private int selectedGroup;
    private AllConnectionData data;
    private ProgressDialog dialog;

    public GroupShareAsyncTask(Context context, int selectedGroup, AllConnectionData data, RequestCrafter rc){
        this.resourceCrafter = rc;
        this.context = context.getApplicationContext();
        this.loc = new Location("asyncTaskLoc");
        this.selectedGroup = selectedGroup;
        this.data = data;
        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Creating group...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.d(TAG, "id = " + this.selectedGroup);

        if (selectedGroup == -1 || data.groups.size() == 0) {
            try {
                this.loc.setLatitude(MainActivity.getActivity().getData().myLatitude);
                this.loc.setLongitude(MainActivity.getActivity().getData().myLongitude);

                this.group = this.resourceCrafter.restGroupCreate(this.loc);
                data.updateGroupInfo(this.group);
            } catch (InternalErrorException e) {
                Log.d(TAG, "exception: " + e.getMessage());
            }
        }
        else {
            this.group = data.groups.get(selectedGroup);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        //bind to new service
        MainActivity.getActivity().doBindService(this.group.hash);

        String shareUrl = this.context.getString(R.string.share_link) + this.group.hash;

        Intent intent2 = new Intent(Intent.ACTION_SEND);
        intent2.setType("text/plain");
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent2.putExtra(android.content.Intent.EXTRA_TEXT, shareUrl);
        MainActivity.getActivity().startActivity(Intent.createChooser(intent2, this.context.getString(R.string.share_msg_new)));

    }

    public void groupShareThread(){
        new Thread(new Runnable() {
            public void run() {
                if (selectedGroup == -1 || data.groups.get(selectedGroup) == null) {
                    try {
                        GroupShareAsyncTask.this.loc.setLatitude(MainActivity.getActivity().getData().myLatitude);
                        GroupShareAsyncTask.this.loc.setLongitude(MainActivity.getActivity().getData().myLongitude);

                        GroupShareAsyncTask.this.group = GroupShareAsyncTask.this.resourceCrafter.restGroupCreate(GroupShareAsyncTask.this.loc);
                        data.updateGroupInfo(GroupShareAsyncTask.this.group);
                    } catch(Exception e){
                        Log.e(TAG, e.getMessage());
                        MainActivity.getActivity().showToastMsg("excetion: " + e.getMessage());
                    }
                }
                else {
                    GroupShareAsyncTask.this.group = data.groups.get(selectedGroup);
                }

                //bind to new service
                MainActivity.getActivity().doBindService(GroupShareAsyncTask.this.group.hash);

                String shareUrl = GroupShareAsyncTask.this.context.getString(R.string.share_link) + GroupShareAsyncTask.this.group.hash;

                Intent intent2 = new Intent(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra(android.content.Intent.EXTRA_TEXT, shareUrl);
                MainActivity.getActivity().startActivity(Intent.createChooser(intent2, GroupShareAsyncTask.this.context.getString(R.string.share_msg_new)));
            }
        }).start();
    }


}