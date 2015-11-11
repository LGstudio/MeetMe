package cz.vutbr.fit.tam.meetme.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import cz.vutbr.fit.tam.meetme.asynctasks.GroupLeaveAsyncTask;
import cz.vutbr.fit.tam.meetme.asynctasks.GroupShareAsyncTask;
import cz.vutbr.fit.tam.meetme.gui.SquareButton;
import cz.vutbr.fit.tam.meetme.schema.AllConnectionData;
import cz.vutbr.fit.tam.meetme.schema.DeviceInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.gui.ArrowView;

/**
 * @author Gebriel Lehocky
 *         <p/>
 *         Fragment that shows the compass.
 */
public class CompassFragment extends Fragment implements View.OnClickListener {
    public static final String LOG_TAG = "CompassFragment";
    private View view;

    private SquareButton addButton;
    private Button leaveButton;
    private RelativeLayout arrowArea;
    private Spinner groupSpinner;
    private Spinner personSpinner;
    public int selectedGroup = 0;
    public int selectedPerson = 0;

    protected ArrayList<GroupInfo> groups;
    protected ArrayList<DeviceInfo> devices;

    protected AllConnectionData data;

    public void addData(AllConnectionData d){
        data = d;
    }

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
        groups = (ArrayList<GroupInfo>) data.groups.clone();
        groupSpinner.setAdapter(new GroupAdapter(getContext(), R.layout.list_group_line, R.id.list_group_item_text, groups));
        int tmp = selectedPerson;
        groupSpinner.setSelection(selectedGroup);
        selectedPerson = tmp;
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

        if (selectedGroup == 0) {
            for (GroupInfo g : groups) {
                for (DeviceInfo d : g.getDeviceInfoList()) {
                    devices.add(d);
                }
            }
            leaveButton.setText(getString(R.string.button_end_all_connection));
        } else {
            for (DeviceInfo d : groups.get(selectedGroup).getDeviceInfoList()) {
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
        GroupShareAsyncTask gs = new GroupShareAsyncTask(this.getContext(), this.selectedGroup, this.data, MainActivity.getActivity().getResourceCrafter());
        gs.execute();
    }

    /**
     * Leaves the appropriate group based on the group spinners selection
     */
    private void leaveGroup() {
        GroupLeaveAsyncTask gl = new GroupLeaveAsyncTask(this.getContext(), this.selectedGroup, this.data, MainActivity.getActivity().getResourceCrafter());
        gl.execute();

        if (selectedGroup == 0) {
            data.initNew();
        }
        else {
            data.disconnectFromGroup(selectedGroup);
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
            groupSizeText.setText(groups.get(position).toString());

            ImageView groupIcon = (ImageView) spinnerElement.findViewById(R.id.list_group_item_img);
            Drawable icon = getResources().getDrawable(R.drawable.list_group_none);
            icon = icon.mutate();
            icon.setColorFilter(getResources().getColor(data.groupColor.get(groups.get(position).id)), PorterDuff.Mode.MULTIPLY);
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



}
