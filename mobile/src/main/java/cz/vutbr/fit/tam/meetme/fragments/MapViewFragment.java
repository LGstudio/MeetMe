package cz.vutbr.fit.tam.meetme.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;


import cz.vutbr.fit.tam.meetme.R;
import cz.vutbr.fit.tam.meetme.schema.AllConnectionData;
import cz.vutbr.fit.tam.meetme.schema.DeviceInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;


/**
 * Created by Gabriel Lehocky on 15/10/10.
 */
public class MapViewFragment extends Fragment {

    private static MapView mMapView;
    private static GoogleMap googleMap;

    private static View v;
    private static AllConnectionData data;

    public void addData(AllConnectionData d){
        data = d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null) {
            return null;
        }
        v = inflater.inflate(R.layout.fragment_map, container, false);

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

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

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
            int colorResource = data.groupColor.get(g.id).intValue();
            for (DeviceInfo d: g.getDeviceInfoList()){
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(d.latitude, d.longitude));
                markerOption.title(d.name);

                IconGenerator generator = new IconGenerator(getContext());
                generator.setColor(getResources().getColor(colorResource));
                generator.setTextAppearance(R.style.mapIconText);
                Bitmap btmp = generator.makeIcon(d.name);
                markerOption.icon(BitmapDescriptorFactory.fromBitmap(btmp));

                googleMap.addMarker(markerOption);
            }
        }
    }
}
