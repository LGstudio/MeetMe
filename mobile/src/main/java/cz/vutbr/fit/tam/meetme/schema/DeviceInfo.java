package cz.vutbr.fit.tam.meetme.schema;

/**
 * Created by Lada on 13.10.2015.
 */
public class DeviceInfo {
    public Integer id;
    public String name;
    public Double latitude;
    public Double longitude;

    @Override
    public String toString(){
        return "(" + id + ")";
    }
}
