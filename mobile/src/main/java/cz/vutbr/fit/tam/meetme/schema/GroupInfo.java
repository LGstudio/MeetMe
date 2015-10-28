package cz.vutbr.fit.tam.meetme.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lada on 13.10.2015.
 */
public class GroupInfo {

    public Integer id;
    public String hash;
    public List<DeviceInfo> deviceInfoList;

    public String toString(){
        if (id != 0)
            return String.valueOf(deviceInfoList.size());
        else
            return hash;
    }
}
