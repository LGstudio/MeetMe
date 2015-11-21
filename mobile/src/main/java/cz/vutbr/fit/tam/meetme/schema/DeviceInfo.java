package cz.vutbr.fit.tam.meetme.schema;

/**
 * @author Vlada Svoboda, Gabriel Lehocky
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
        return name;
    }

    public String getDistance(){
        if (distance < 1000){
            return String.format("%.0f", distance) + "\nm";
        }
        else if (distance < 10000){
            return String.format("%.2f", distance/1000) + "\nkm" ;
        }
        else if (distance < 100000){
            return String.format("%.1f", distance/1000) + "\nkm" ;
        }

        return String.format("%.0f", distance/1000) + "\nkm" ;
    }
}
