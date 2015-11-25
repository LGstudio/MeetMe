package cz.vutbr.fit.tam.meetme.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lada on 13.10.2015.
 */
public class GroupInfo {

    public Integer id;
    public String hash;
    private List<DeviceInfo> deviceInfoList;

    public GroupInfo(){
        deviceInfoList = new ArrayList<>();
    }

    public List<DeviceInfo> getDeviceInfoList() {
        return deviceInfoList;
    }

    public void setDeviceInfoList(List<DeviceInfo> deviceInfoList) {
        this.deviceInfoList = deviceInfoList;
    }

    public String toString(){
        int size = deviceInfoList.size();
        String text;
        switch (size){
            case 0:
                text = "(empty group)";
                break;
            case 1:
                text = deviceInfoList.get(0).name;
                break;
            case 2:
                text = deviceInfoList.get(0).name + " & " + deviceInfoList.get(1).name;
                break;
            case 3:
                text = deviceInfoList.get(0).name + ", " + deviceInfoList.get(1).name + " & 1 other";
                break;
            default:
                int s = size - 2;
                text = deviceInfoList.get(0).name + ", " + deviceInfoList.get(1).name + " & " + s + " others";
                break;
        }

        return text;
    }
}
