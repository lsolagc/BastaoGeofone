package br.com.gasi.bastogeofone;


import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class InspecaoFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    CanvasDrawing mCanvasDrawing;

    private final String TAG = this.getClass().getSimpleName();

    public InspecaoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_inspecao, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<PointOfInterest> poi = generatePoi();
//        ListView listView = getView().findViewById(R.id.listview_poi);
//        ArrayAdapter<PointOfInterest> adapter = new ArrayAdapter<>(this.getContext(),android.R.layout.simple_list_item_1, poi);
        //Log.i(TAG, "onViewCreated: poi: "+poi);
//        listView.setAdapter(adapter);

        SupportMapFragment supportMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map2);
        //Log.i(TAG, "onCreateView: supportMapFragment: "+supportMapFragment);
        supportMapFragment.getMapAsync(this);

        TextView tv_status = getView().findViewById(R.id.tv_statusInspecao);
        tv_status.setText(R.string.inspStatusRunning);

        mCanvasDrawing = getView().findViewById(R.id.canvas);
        //mCanvasDrawing.draw("rect",0,0);

    }

    private List<PointOfInterest> generatePoi() {
        //TODO: povoar List
        List<PointOfInterest> list = new ArrayList();
        for (int i=1;i<=20;i++){
            PointOfInterest poi = new PointOfInterest();
            poi.setAddress("Endereço "+i);
            poi.setCoordinates(new LatLng(i,i));
            list.add(poi);
        }
        return list;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getContext());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            LatLng locale = new LatLng(location.getLatitude(),location.getLongitude());
                            mGoogleMap.addMarker(new MarkerOptions().position(locale).title("Localização atual"));
                            CameraPosition position = CameraPosition.builder().target(locale).zoom(20).build();
                            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                        }
                    }
                });

    }
}
