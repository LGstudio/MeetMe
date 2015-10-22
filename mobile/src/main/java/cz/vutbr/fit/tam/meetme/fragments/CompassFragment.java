package cz.vutbr.fit.tam.meetme.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import cz.vutbr.fit.tam.meetme.R;
import cz.vutbr.fit.tam.meetme.schema.DeviceInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.gui.ArrowView;

/**
 * @author Gebriel Lehocky
 *
 * Fragment that shows the compass.
 */
public class CompassFragment extends MeetMeFragment{

    private View view;

    private RelativeLayout arrowArea;
    private Spinner groupSpinner;
    private Spinner personSpinner;
    protected int selectedGroup = 0;
    protected int selectedPerson = 0;

    private GroupInfo[] groupInfoItems;
    private DeviceInfo[] deviceInfoItems;
    private ArrowView[] arrows;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_compass, container, false);
        this.view = v;

        groupSpinner = (Spinner) view.findViewById(R.id.list_group);
        personSpinner = (Spinner) view.findViewById(R.id.list_person);
        arrowArea = (RelativeLayout) view.findViewById(R.id.arrow_area);

        //-------------------

        ArrowView a = new ArrowView(getContext());
        arrowArea.addView(a);

        RotateAnimation r; // = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
        r = new RotateAnimation(0.0f, -10.0f * 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration((long) 2*1500);
        r.setRepeatCount(0);
        a.startAnimation(r);

        //--------------------
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = position;
                selectedPerson = 0;
                addPersonsToSpinner();
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

        addGroupsToSpinner();

        return view;
    }

    /**
     * Adds Groups into spinner based on the ArrayList<GroupInfo> groups list
     */
    public void addGroupsToSpinner(){

        GroupInfo allGroup = new GroupInfo();
        allGroup.hash = getString(R.string.dropdown_all_group);
        //allGroup.groupColor = R.color.flat_brightness_difference;
        allGroup.id = 0;

        groupInfoItems = new GroupInfo[groups.size()+1];
        groupInfoItems[0] = allGroup;
        for (int i = 0; i < groups.size(); i++){
            groupInfoItems[i+1] = groups.get(i);
        }

        groupSpinner.setAdapter(new GroupAdapter(getContext(), R.layout.list_group_line, R.id.list_group_item_text, groupInfoItems));

        groupSpinner.setSelection(selectedGroup);
        if(selectedGroup == 0) addPersonsToSpinner();
    }

    /**
     * Adds the connected Devices into spinner
     * based on the ArrayList<GroupInfo> groups list.
     */
    public void addPersonsToSpinner(){

        DeviceInfo allDevices = new DeviceInfo();
        allDevices.name = getString(R.string.dropdown_all_contact);


        int deviceCount = 1;
        int i = 1;

        if (selectedGroup == 0){
            //allDevices.color = R.color.flat_brightness_difference;

            for (GroupInfo g: groups) deviceCount += g.deviceInfoList.size();

            deviceInfoItems = new DeviceInfo[deviceCount];
            deviceInfoItems[0] = allDevices;

            for (GroupInfo g: groups){
                for (DeviceInfo d: g.deviceInfoList){
                    deviceInfoItems[i] = d;
                    i++;
                }
            }
        }
        else {
            //allDevices.color = groups.get(selectedGroup-1).groupColor;

            deviceCount += groups.get(selectedGroup-1).deviceInfoList.size();

            deviceInfoItems = new DeviceInfo[deviceCount];
            deviceInfoItems[0] = allDevices;
            for (DeviceInfo d: groups.get(selectedGroup-1).deviceInfoList){
                deviceInfoItems[i] = d;
                i++;
            }
        }

        personSpinner.setAdapter(new PersonAdapter(getContext(), R.layout.list_person_line, R.id.list_person_item_text, deviceInfoItems));

        personSpinner.setSelection(selectedPerson);
    }

    public void createArrows(){

    }

    /**
     * Redraws the compass area based on the ArrayList<GroupInfo> groups list.
     */
    public void redrawCompass(){
        
    }

    /**
     * Custom ArrayAdapter for GroupInfo in the Spinner
     */
    public class GroupAdapter extends ArrayAdapter<GroupInfo> {

        public GroupAdapter(Context ctx, int lineLayout, int txtViewResourceId, GroupInfo[] objects) {
            super(ctx, lineLayout, txtViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View spinnerElement = inflater.inflate(R.layout.list_group_line, parent, false);


            TextView groupSizeText = (TextView) spinnerElement.findViewById(R.id.list_group_item_text);
            groupSizeText.setText(groupInfoItems[position].toString());

/**
            ImageView groupIcon = (ImageView) spinnerElement.findViewById(R.id.list_group_item_img);
            Drawable icon = getResources().getDrawable(R.drawable.list_group_none);
            icon = icon.mutate();
            icon.setColorFilter(getResources().getColor(groupInfoItems[position].groupColor), PorterDuff.Mode.MULTIPLY);
            groupIcon.setImageDrawable(icon);
*/
            return spinnerElement;
        }

    }

    /**
     * Custom ArrayAdapter for DeviceInfo in the Spinner
     */
    public class PersonAdapter extends ArrayAdapter<DeviceInfo> {

        public PersonAdapter(Context ctx, int lineLayout, int txtViewResourceId, DeviceInfo[] objects) {
            super(ctx, lineLayout, txtViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View spinnerElement = inflater.inflate(R.layout.list_person_line, parent, false);


            TextView personText = (TextView) spinnerElement.findViewById(R.id.list_person_item_text);
            personText.setText(deviceInfoItems[position].toString());

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
