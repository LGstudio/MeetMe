package cz.vutbr.fit.tam.meetme.schema;

import android.app.Activity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import cz.vutbr.fit.tam.meetme.R;

/**
 * Class that is shared by the Activity and MeetMe fragmants,
 * Contains and manages all group data
 *
 * @author Gabriel Lehocky
 */
public class AllConnectionData {

    public GroupColor colors;
    public HashMap<Integer, Integer> groupColor;

    public ArrayList<GroupInfo> groups;

    private Activity parent;

    public AllConnectionData(Activity a){
        parent = a;

        colors = new GroupColor();
        groupColor = new HashMap<>();

        groups = new ArrayList<>();

        GroupInfo allGroup = new GroupInfo();
        allGroup.hash = parent.getString(R.string.dropdown_all_group);
        allGroup.id = 0;
        allGroup.deviceInfoList = new ArrayList<>();
        groups.add(allGroup);

        groupColor.put(0, R.color.flat_brightness_difference);
    }

    /**
     * Updates groups data
     * @param newGroup
     */
    public void updateGroupInfo(GroupInfo newGroup){

        boolean groupExists = false;
        for (GroupInfo g: groups){
            if (g.id == newGroup.id){
                groupExists = true;
                g.deviceInfoList = newGroup.deviceInfoList;
                break;
            }
        }

        if (!groupExists){
            groups.add(newGroup);
            groupColor.put(newGroup.id, colors.getNextColor());
        }

    }

    public void disconnectFromGroup(int id){
        groups.remove(id);
        groupColor.remove(id);
    }

}
