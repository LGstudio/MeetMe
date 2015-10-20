package cz.vutbr.fit.tam.meetme.data;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.tam.meetme.R;

/**
 * @author Gabriel Lehocky
 *
 * Class that holds data of a group
 */
public class GroupInfo {
    public Integer groupId;
    public String groupHash;
    private ArrayList<DeviceInfo> deviceList;
    public Integer groupColor;

    public GroupInfo(){
        deviceList = new ArrayList<>();
    }

    public int getSize(){
        return deviceList.size();
    }

    public String toString(){
        if (groupId == 0){
            return groupHash;
        }
        return "(" + deviceList.size() + ")";
    }

    public void addDevice(DeviceInfo d){
        d.color = groupColor;
        deviceList.add(d);
    }

    public void addDeviceList(ArrayList<DeviceInfo> list){
        for(DeviceInfo d: list){
            d.color = groupColor;
        }
        deviceList = list;

    }

    public ArrayList<DeviceInfo> getDeviceList(){
        return deviceList;
    }
}
