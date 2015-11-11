package cz.vutbr.fit.tam.meetme.schema;

/**
 * Created by Lada on 13.10.2015.
 */
public class DeviceInfo {
    public Integer id;
    public String name;
    public Double latitude;
    public Double longitude;

    public float bearing;
    public float distance;

    @Override
    public String toString(){
        return name + " (" + getDistance() + ")";
    }

    public String getDistance(){
        if (distance < 1000){
            return String.format("%.0f", distance) + "m";
        }
        else if (distance < 10000){
            return String.format("%.2f", distance/1000) + "km" ;
        }
        else if (distance < 100000){
            return String.format("%.1f", distance/1000) + "km" ;
        }

        return String.format("%.0f", distance/1000) + "km" ;
    }
}
