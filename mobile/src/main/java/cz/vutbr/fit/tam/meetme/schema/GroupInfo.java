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
/*
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
*/
    public List<DeviceInfo> getDeviceInfoList() {
        if(deviceInfoList==null)
            deviceInfoList = new ArrayList<>();

        return deviceInfoList;
    }

    public void setDeviceInfoList(List<DeviceInfo> deviceInfoList) {
        this.deviceInfoList = deviceInfoList;
    }
}
