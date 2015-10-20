package cz.vutbr.fit.tam.meetme.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import cz.vutbr.fit.tam.meetme.R;
import cz.vutbr.fit.tam.meetme.data.DeviceInfo;
import cz.vutbr.fit.tam.meetme.data.GroupInfo;
import cz.vutbr.fit.tam.meetme.gui.RoundImageView;

/**
 * @author Gebriel Lehocky
 *
 * Fragment that shows the compass.
 */
public class CompassFragment extends MeetMeFragment{

    private View view;

    private Spinner groupSpinner;
    private Spinner personSpinner;
    protected int selectedGroup = 0;
    protected int selectedPerson = 0;

    private GroupInfo[] groupInfoItems;
    private DeviceInfo[] deviceInfoItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_compass, container, false);
        this.view = v;

        groupSpinner = (Spinner) view.findViewById(R.id.list_group);
        personSpinner = (Spinner) view.findViewById(R.id.list_person);

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
        allGroup.groupHash = getString(R.string.dropdown_all_group);
        allGroup.groupColor = R.color.flat_brightness_difference;
        allGroup.groupId = 0;

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
        allDevices.color = R.color.flat_brightness_difference;

        int deviceCount = 1;
        int i = 1;

        if (selectedGroup == 0){
            for (GroupInfo g: groups) deviceCount += g.getSize();

            deviceInfoItems = new DeviceInfo[deviceCount];
            deviceInfoItems[0] = allDevices;

            for (GroupInfo g: groups){
                //int c = g.groupColor;
                for (DeviceInfo d: g.getDeviceList()){
                    deviceInfoItems[i] = d;
                    i++;
                }
            }
        }
        else {
            deviceCount += groups.get(selectedGroup-1).getSize();
            //int c = groups.get(selectedGroup-1).groupColor;
            deviceInfoItems = new DeviceInfo[deviceCount];
            deviceInfoItems[0] = allDevices;
            for (DeviceInfo d: groups.get(selectedGroup-1).getDeviceList()){
                deviceInfoItems[i] = d;
                i++;
            }
        }

        personSpinner.setAdapter(new PersonAdapter(getContext(), R.layout.list_person_line, R.id.list_person_item_text, deviceInfoItems));

        personSpinner.setSelection(selectedPerson);
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
        public View getView(int pos, View convertView, ViewGroup prnt) {
            return getGroupView(pos, convertView, prnt);
        }

        public View getGroupView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View spinnerElement = inflater.inflate(R.layout.list_group_line, parent, false);


            TextView groupSizeText = (TextView) spinnerElement.findViewById(R.id.list_group_item_text);
            groupSizeText.setText(groupInfoItems[position].toString());


            RoundImageView groupIcon = (RoundImageView) spinnerElement.findViewById(R.id.list_group_item_img);
            groupIcon.setColorFilter(getResources().getColor(groupInfoItems[position].groupColor), PorterDuff.Mode.MULTIPLY);

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
        public View getView(int pos, View convertView, ViewGroup prnt) {
            return getGroupView(pos, convertView, prnt);
        }

        public View getGroupView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View spinnerElement = inflater.inflate(R.layout.list_person_line, parent, false);


            TextView personText = (TextView) spinnerElement.findViewById(R.id.list_person_item_text);
            personText.setText(deviceInfoItems[position].toString());


            RoundImageView personImage = (RoundImageView) spinnerElement.findViewById(R.id.list_person_item_img);
            personImage.setColorFilter(getResources().getColor(deviceInfoItems[position].color), PorterDuff.Mode.MULTIPLY);

            return spinnerElement;
        }

    }
}
