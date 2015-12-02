package cz.vutbr.fit.tam.meetme.release.schema;

import android.app.Activity;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that is shared by the Activity and MeetMe fragmants,
 * Contains and manages all group data
 *
 * @author Gabriel Lehocky
 */
public class AllConnectionData {

    public String myName;
    public double myLatitude;
    public double myLongitude;
    public GroupColor colors;
    public HashMap<Integer, Integer> groupColor;
    public ArrayList<GroupInfo> groups;
    public int selectedGroup = -1;

    private Activity parent;

    public AllConnectionData(Activity a){
        parent = a;

        colors = new GroupColor();
        groupColor = new HashMap<>();
        groups = new ArrayList<>();
    }

    public ArrayList<DataMap> getDataMap() {
        ArrayList<DataMap> allData = new ArrayList<>();

        for (GroupInfo group : groups) {
            int groupId = group.id;

            for (DeviceInfo device : group.getDeviceInfoList()) {

                DataMap data = new DataMap();
                data.putString("name", device.name);
                data.putInt("groupId", groupId);
                data.putFloat("distance", device.distance);
                data.putFloat("bearing",  device.bearing);

                allData.add(data);
            }
        }

        return allData;
    }

    /**
     * Updates groups data
     * @param newGroup
     */
    public void updateGroupInfo(GroupInfo newGroup){

        //if (newGroup.getDeviceInfoList() != null)
            for (DeviceInfo d: newGroup.getDeviceInfoList()){
                float[] calc = computeDistanceAndBearing(myLatitude,myLongitude,d.latitude,d.longitude);
                d.distance = calc[0];
                d.bearing = calc[1];
            }

        boolean groupExists = false;
        for (GroupInfo g: groups){
            if (g.id.equals(newGroup.id)){
                groupExists = true;
                g.setDeviceInfoList(newGroup.getDeviceInfoList());
                break;
            }
        }

        if (!groupExists){
            groups.add(newGroup);
            groupColor.put(newGroup.id, colors.getNextColor());
        }

    }

    /**
     * TEST DATA ----------------------------------------
     */
    public void addShit(){

        GroupInfo testGroup = new GroupInfo();
        testGroup.hash = "ABC";
        testGroup.id = 1;
        groupColor.put(1, colors.getNextColor());

        //-------------------------------------------------------------------
        DeviceInfo d1 = new DeviceInfo();
        d1.name = "User 1";
        d1.id = 1;
        d1.latitude = myLatitude * 0.99;
        d1.longitude = myLongitude * 1.1;
        float[] calc1 = computeDistanceAndBearing(myLatitude,myLongitude,d1.latitude,d1.longitude);
        d1.distance = calc1[0];
        d1.bearing = calc1[1];

        //-------------------------------------------------------------------
        DeviceInfo d2 = new DeviceInfo();
        d2.name = "User 2";
        d2.id = 2;
        d2.latitude = myLatitude * 1.05;
        d2.longitude = myLongitude * 0.995;
        float[] calc2 = computeDistanceAndBearing(myLatitude,myLongitude,d2.latitude,d2.longitude);
        d2.distance = calc2[0];
        d2.bearing = calc2[1];

        //-------------------------------------------------------------------
        ArrayList<DeviceInfo> l = new ArrayList<>();
        l.add(d1);
        l.add(d2);
        testGroup.setDeviceInfoList(l);
        groups.add(testGroup);

        //-------------------------------------------------------------------
        GroupInfo testGroup2 = new GroupInfo();
        testGroup2.hash = "XYZ";
        testGroup2.id = 2;
        groupColor.put(2, colors.getNextColor());

        DeviceInfo d3 = new DeviceInfo();
        d3.name = "User 3";
        d3.id = 3;
        d3.latitude = myLatitude * 1.001;
        d3.longitude = myLongitude * 0.999;
        float[] calc3 = computeDistanceAndBearing(myLatitude,myLongitude,d3.latitude,d3.longitude);
        d3.distance = calc3[0];
        d3.bearing = calc3[1];

        //-------------------------------------------------------------------
        ArrayList<DeviceInfo> l2 = new ArrayList<>();
        l2.add(d3);
        testGroup2.setDeviceInfoList(l2);
        groups.add(testGroup2);
    }

    public void disconnectFromGroup(int number){
        groupColor.remove(groups.get(number).id);
        groups.remove(number);

    }

    /**
     * Got from android.location.Location
     *
     * @param lat1 from - latitude
     * @param lon1 from - longitude
     * @param lat2 to - latitude
     * @param lon2 to - logitude
     * @return result - arrat of float - [0]:distance | [1]:bearing
     */
    private static float[] computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2) {

        float[] result = new float[2];

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 : cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) *  (4096.0 + uSquared * (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * (256.0 + uSquared * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * (cos2SM + (B / 4.0) * (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0) * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SMSq)));

            lambda = L + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SM + C * cosSigma * (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        result[0] = (float) (b * A * (sigma - deltaSigma));

        result[1] = (float) Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
        result[1] *= 180.0 / Math.PI;

        return result;
    }
}
