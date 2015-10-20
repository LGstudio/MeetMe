package cz.vutbr.fit.tam.meetme.schema;

/**
 * Created by Lada on 13.10.2015.
 */
public class DeviceResponseInfo extends ResponseInfo{


    private DeviceInfo deviceInfo;


    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
