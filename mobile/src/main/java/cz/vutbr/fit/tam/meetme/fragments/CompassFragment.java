package cz.vutbr.fit.tam.meetme.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import cz.vutbr.fit.tam.meetme.MainActivity;
import cz.vutbr.fit.tam.meetme.R;
import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.gui.SquareButton;
import cz.vutbr.fit.tam.meetme.requestcrafter.RequestCrafter;
import cz.vutbr.fit.tam.meetme.schema.DeviceInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.gui.ArrowView;

/**
 * @author Gebriel Lehocky
 *         <p/>
 *         Fragment that shows the compass.
 */
public class CompassFragment extends MeetMeFragment implements View.OnClickListener {

    private View view;

    private SquareButton addButton;
    private Button leaveButton;
    private RelativeLayout arrowArea;
    private Spinner groupSpinner;
    private Spinner personSpinner;
    public int selectedGroup = 0;
    public int selectedPerson = 0;

    protected ArrayList<DeviceInfo> devices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_compass, container, false);
        this.view = v;

        groupSpinner = (Spinner) view.findViewById(R.id.list_group);
        personSpinner = (Spinner) view.findViewById(R.id.list_person);
        arrowArea = (RelativeLayout) view.findViewById(R.id.arrow_area);
        addButton = (SquareButton) view.findViewById(R.id.button_add);
        leaveButton = (Button) view.findViewById(R.id.button_exit);

        addButton.setOnClickListener(this);
        leaveButton.setOnClickListener(this);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = position;
                selectedPerson = 0;
                addDevicesToSpinner();
                redrawCompass();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        personSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPerson = position;
                redrawCompass();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        changeSpinnerData();
        createArrows();

        return view;
    }

    /**
     * Changes the data in the spinners based on the dara.grops array
     */
    public void changeSpinnerData() {
        groupSpinner.setAdapter(new GroupAdapter(getContext(), R.layout.list_group_line, R.id.list_group_item_text, data.groups));
        groupSpinner.setSelection(selectedGroup);
        addDevicesToSpinner();
    }

    /**
     * Adds the connected Devices into spinner
     * based on the ArrayList<GroupInfo> groups list.
     */
    public void addDevicesToSpinner() {

        devices = new ArrayList<>();
        DeviceInfo all = new DeviceInfo();
        all.id = 0;
        all.name = getString(R.string.dropdown_all_contact);
        devices.add(all);

        if (selectedGroup == 0 || data.groups.get(selectedGroup) == null) {
            for (GroupInfo g : data.groups) {
                for (DeviceInfo d : g.deviceInfoList) {
                    devices.add(d);
                }
            }
            leaveButton.setText(getString(R.string.button_end_all_connection));
        } else {
            for (DeviceInfo d : data.groups.get(selectedGroup).deviceInfoList) {
                devices.add(d);
            }
            leaveButton.setText(getString(R.string.button_end_connection));
        }

        personSpinner.setAdapter(new PersonAdapter(getContext(), R.layout.list_person_line, R.id.list_person_item_text, devices));
        personSpinner.setSelection(selectedPerson);
    }


    public void createArrows() {
        //-------------------

        ArrowView a = new ArrowView(getContext());
        arrowArea.addView(a);

        RotateAnimation r; // = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
        r = new RotateAnimation(0.0f, -10.0f * 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration((long) 2 * 1500);
        r.setRepeatCount(0);
        a.startAnimation(r);

        //--------------------
    }

    /**
     * Redraws the compass area based on the ArrayList<GroupInfo> groups list.
     * TODO: AsyncTask to refresh based on data
     */
    public void redrawCompass() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add:
                createNewGroup();
                break;
            case R.id.button_exit:
                leaveGroup();
                break;
        }
    }

    /**
     * Creates new group by sending request to server, then ask the user to share the link with someone
     */
    private void createNewGroup() {
        final Location loc = new Location("testLocation");

        GroupShareAsyncTask gs = new GroupShareAsyncTask(this.getContext(), MainActivity.getActivity().getResourceCrafter(), loc);
        gs.execute();
    }

    /**
     * Leaves the appropriate group based on the group spinners selection
     */
    private void leaveGroup() {
        if (selectedGroup == 0 || data.groups.get(selectedGroup) == null) {
            // TODO: Detach all groups from server

            GroupInfo base = data.groups.get(0);
            data.groups = new ArrayList<>();
            data.groups.add(base);
        } else {
            // TODO: Detach data.groups.get(selectedGroup) from server

            data.groups.remove(selectedGroup);
        }

        selectedGroup = 0;
        selectedPerson = 0;
        changeSpinnerData();
    }

    /**
     * Custom ArrayAdapter for GroupInfo in the Spinner
     */
    public class GroupAdapter extends ArrayAdapter<GroupInfo> {

        public GroupAdapter(Context ctx, int lineLayout, int txtViewResourceId, ArrayList<GroupInfo> objects) {
            super(ctx, lineLayout, txtViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View spinnerElement = inflater.inflate(R.layout.list_group_line, parent, false);


            TextView groupSizeText = (TextView) spinnerElement.findViewById(R.id.list_group_item_text);
            groupSizeText.setText(data.groups.get(position).toString());


            ImageView groupIcon = (ImageView) spinnerElement.findViewById(R.id.list_group_item_img);
            Drawable icon = getResources().getDrawable(R.drawable.list_group_none);
            icon = icon.mutate();
            icon.setColorFilter(getResources().getColor(data.groupColor.get(data.groups.get(position).id)), PorterDuff.Mode.MULTIPLY);
            groupIcon.setImageDrawable(icon);

            return spinnerElement;
        }

    }

    /**
     * Custom ArrayAdapter for DeviceInfo in the Spinner
     */
    public class PersonAdapter extends ArrayAdapter<DeviceInfo> {

        public PersonAdapter(Context ctx, int lineLayout, int txtViewResourceId, ArrayList<DeviceInfo> objects) {
            super(ctx, lineLayout, txtViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View spinnerElement = inflater.inflate(R.layout.list_person_line, parent, false);


            TextView personText = (TextView) spinnerElement.findViewById(R.id.list_person_item_text);
            personText.setText(devices.get(position).toString());

            /**
             ImageView personIcon = (ImageView) spinnerElement.findViewById(R.id.list_person_item_img);
             Drawable icon = getResources().getDrawable(R.drawable.list_single_none);
             icon = icon.mutate();
             icon.setColorFilter(getResources().getColor(deviceInfoItems[position].color), PorterDuff.Mode.MULTIPLY);
             personIcon.setImageDrawable(icon);
             */
            return spinnerElement;
        }

    }

    public class GroupShareAsyncTask extends AsyncTask<Void,Void,Void> {
        private final String TAG="GroupShareAsyncTask";

        private RequestCrafter resourceCrafter;
        private Context context;
        private Location loc;
        private GroupInfo group = null;
        private String shareMsg;

        public GroupShareAsyncTask(Context context, RequestCrafter rc, Location loc){
            this.resourceCrafter = rc;
            this.context = context.getApplicationContext();
            this.loc = loc;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (selectedGroup == 0 || data.groups.get(selectedGroup) == null) {
                try {
                    this.group = this.resourceCrafter.restGroupCreate(this.loc);
                    data.updateGroupInfo(this.group);
                    this.shareMsg = getString(R.string.share_msg_new);
                } catch (InternalErrorException e) {
                    Log.d(TAG, "exception: " + e.getMessage());
                }
            }
            else {
                this.group = data.groups.get(selectedGroup);
                this.shareMsg = getString(R.string.share_msg_existing);
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
            MainActivity.getActivity().startActivity(Intent.createChooser(i, this.shareMsg + " (" + this.group.id + ")"));

        }

    }

}
