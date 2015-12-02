package cz.vutbr.fit.tam.meetme.release.schema;

/**
 * Created by Jakub on 22. 11. 2015.
 */
public class ConnectionData {

    public String name;
    public int groupId;

    public float bearing;
    public float distance;

    public ConnectionData(String name, int groupId, float bearing, float distance) {
        this.name = name;
        this.groupId = groupId;
        this.bearing = bearing;
        this.distance = distance;
    }

    public boolean isSame(String n) {
        return name.equals(n);
    }

    public void updateData(int groupId, float bearing, float distance) {
        this.groupId = groupId;
        this.bearing = bearing;
        this.distance = distance;
    }
}
