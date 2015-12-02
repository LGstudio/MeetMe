package cz.vutbr.fit.tam.meetme.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cz.vutbr.fit.tam.meetme.MainActivity;
import cz.vutbr.fit.tam.meetme.R;
import cz.vutbr.fit.tam.meetme.asynctasks.GroupLeaveAsyncTask;
import cz.vutbr.fit.tam.meetme.asynctasks.GroupShareAsyncTask;
import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.gui.RoundImageView;
import cz.vutbr.fit.tam.meetme.gui.SquareButton;
import cz.vutbr.fit.tam.meetme.gui.SquareImageView;
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

    private final static int ROTATION_DURATION = 80;
    private static final String LOG_TAG = "CompassFragment";

    private View view;

    private SquareButton addButton;
    private Button leaveButton;
    private RelativeLayout arrowArea;
    private Spinner groupSpinner;

    private float degree;
    
    protected ArrayList<GroupInfo> groups;
    protected ArrayList<ArrowView> arrows;

    protected AllConnectionData data;

    public void addData(AllConnectionData d){
        data = d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_compass, container, false);
        this.view = v;

        arrows = new ArrayList<>();

        degree = 0.0f;

        groupSpinner = (Spinner) view.findViewById(R.id.list_group);
        arrowArea = (RelativeLayout) view.findViewById(R.id.arrow_area);
        addButton = (SquareButton) view.findViewById(R.id.button_add);
        leaveButton = (Button) view.findViewById(R.id.button_exit);

        addButton.setOnClickListener(this);
        leaveButton.setOnClickListener(this);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.selectedGroup = position;
                this.handleServiceSwitch();
                createArrows();
            }

            private void handleServiceSwitch() {

              if(data.selectedGroup!= -1) {
                  GroupInfo gi = groups.get(data.selectedGroup);
                  String selectedGroupHash = gi.hash;

                  if (MainActivity.getActivity().getBinder() == null) {
                      Log.d(LOG_TAG, "binder is null");

                      //null takze zadna service nebezela -> zapnem service

                      //bind to new service
                      MainActivity.getActivity().doBindService(selectedGroupHash);

                  } else if (selectedGroupHash.equals(MainActivity.getActivity().getBinder().getService().getGroupHash())) {
                      //selectnuta skupina je rovna ty pro kterou service prave bezi, nic nedelame
                      Log.d(LOG_TAG, "same group selected");
                  } else {
                      //prepojeni na novou skupinu
                      Log.d(LOG_TAG, "joining to new group");

                      MainActivity.getActivity().doUnbindService();

                      //bind to new service
                      MainActivity.getActivity().doBindService(selectedGroupHash);
                  }
              }
              else
                MainActivity.getActivity().doUnbindService();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateView();
        createArrows();

        return view;
    }

    /**
     * Changes the data in the spinners and arrow rotation
     * based on the dara.grops array
     */
    public void updateView() {
        groups = (ArrayList<GroupInfo>) data.groups.clone();
        if (groups.size() == 0){
            data.selectedGroup = -1;
            leaveButton.setVisibility(View.INVISIBLE);
        }
        else {
            groupSpinner.setSelection(data.selectedGroup);
            leaveButton.setVisibility(View.VISIBLE);
        }
        groupSpinner.setAdapter(new GroupAdapter(getContext(), R.layout.list_group_line, R.id.list_group_item_text, groups));

        if(data.selectedGroup!=-1)
            groupSpinner.setSelection(data.selectedGroup, false);

        createArrows();
    }


    public void createArrows() {
        for (ArrowView a: arrows){
            arrowArea.removeView(a);
        }
        arrows.clear();

        if (data.selectedGroup > -1){
            GroupInfo g = groups.get(data.selectedGroup);
            int c = getResources().getColor(data.groupColor.get(g.id));
            for (DeviceInfo d: g.getDeviceInfoList()){
                ArrowView a = new ArrowView(getContext());
                a.arrow.setColorFilter(c, PorterDuff.Mode.MULTIPLY);
                a.contactName.setText(d.name);
                a.contectDistance.setText(d.getDistance());
                a.setRotation(d.bearing);
                arrowArea.addView(a);
                arrows.add(a);
            }
        }
    }

    /**
     * Called when device rotation is changed
     * @param x
     */
    public void setDeviceRotation(float x) {

        RotateAnimation r = new RotateAnimation(degree, -x, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        r.setDuration(ROTATION_DURATION);
        r.setFillAfter(true);
        arrowArea.startAnimation(r);

        degree = -x;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add:
                askWhereToInvite();
                break;
            case R.id.button_exit:
                leaveGroup();
                break;
        }
    }

    /**
     * Creates new group by sending request to server, then ask the user to share the link with someone
     */
    private void createNewGroup(final int id) {

        GroupShareAsyncTask gs = new GroupShareAsyncTask(this.getContext(), id, this.data, MainActivity.getActivity().getResourceCrafter());
        //gs.execute();
        //gs.executeOnExecutor(threadPoolExecutor);
        //jen zavolani metody asynctasku kvuli app performance
        gs.groupShareThread();

    }

    private void askWhereToInvite(){

        if (data.selectedGroup < 0){
            createNewGroup(-1);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Invite into ..");
            builder.setItems(new CharSequence[]
                            {"New group", "Actual group"},
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            switch (which) {
                                case 0:
                                    createNewGroup(-1);
                                    break;
                                case 1:
                                    createNewGroup(data.selectedGroup);
                                    break;
                            }
                        }
                    });
            builder.create().show();
        }
    }

    /**
     * Leaves the appropriate group based on the group spinners selection
     */
    private void leaveGroup() {

        String hash = this.data.groups.get(data.selectedGroup).hash;

        GroupLeaveAsyncTask gl = new GroupLeaveAsyncTask(hash, MainActivity.getActivity().getResourceCrafter());
        gl.execute();

        data.disconnectFromGroup(data.selectedGroup);

        data.selectedGroup -= 1;//automaticky oznacime za selectlou skupinu na pozici -1
        updateView();
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

            SquareImageView imageView = (SquareImageView) spinnerElement.findViewById(R.id.list_group_item_img);
            imageView.setBackgroundColor(getResources().getColor(data.groupColor.get(groups.get(position).id)));

            return spinnerElement;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent){
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_group_line, null);
            }

            TextView groupSizeText = (TextView) v.findViewById(R.id.list_group_item_text);
            groupSizeText.setText(groups.get(position).toString());

            SquareImageView imageView = (SquareImageView) v.findViewById(R.id.list_group_item_img);
            imageView.setBackgroundColor(getResources().getColor(data.groupColor.get(groups.get(position).id)));

            return v;
        }

    }

}
