package cz.vutbr.fit.tam.meetme.data;

/**
 * @author Gabriel Lehocky
 *
 * Class that holds data of a connected device
 */
public class DeviceInfo {
    public Integer deviceId;
    public String name;
    public Double latitude;
    public Double longitude;
    public int color;

    public String toString(){
        return name;
    }
}
