package cz.vutbr.fit.tam.meetme.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import cz.vutbr.fit.tam.meetme.R;
import cz.vutbr.fit.tam.meetme.schema.DeviceInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;


/**
 * Created by Gabriel Lehocky on 15/10/10.
 */
public class MapViewFragment extends MeetMeFragment {

    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        googleMap.setMyLocationEnabled(true);

        LatLng myPos = new LatLng(data.myLatitude, data.myLongitude);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(myPos).zoom(14).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Perform any camera updates here
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void updateLocations(){

        googleMap.clear();

        for (GroupInfo g: data.groups){
            for (DeviceInfo d: g.getDeviceInfoList()){
                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(d.latitude, d.longitude));
                marker.title("(" + d.id + ")");

                Drawable icon = getResources().getDrawable(R.drawable.map_marker);
                icon = icon.mutate();
                icon.setColorFilter(getResources().getColor(data.groupColor.get(g.id)), PorterDuff.Mode.MULTIPLY);

                Canvas canvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                icon.draw(canvas);

                BitmapDescriptor i = BitmapDescriptorFactory.fromBitmap(bitmap);
                marker.icon(i);
                googleMap.addMarker(marker);
            }
        }
        // create marker
        //MarkerOptions marker = new MarkerOptions().position(new LatLng(data.myLatitude, data.myLongitude)).title(getString(R.string.map_me));

        // Changing marker icon
        //marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        // adding marker
        //
    }
}
