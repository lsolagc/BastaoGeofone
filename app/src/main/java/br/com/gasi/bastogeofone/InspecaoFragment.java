package br.com.gasi.bastogeofone;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
    LocationManager locationManager;
    long minTime = 500, minDistance = 1;
    Marker marker;
    Location currentLocation, lastLocation;

    private final String TAG = this.getClass().getSimpleName();
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            Toast.makeText(getContext(), "Localização Alterada: "+location, Toast.LENGTH_SHORT).show();
            LatLng locale = new LatLng(location.getLatitude(),location.getLongitude());
            marker.remove();
            marker = mGoogleMap.addMarker(new MarkerOptions().position(locale).title("Localização atual"));
            CameraPosition position = CameraPosition.builder().target(locale).zoom(20).build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
            getDisplacement();
            lastLocation = currentLocation;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Toast.makeText(getContext(), "onStatusChanged Called", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(getContext(), "onProviderEnabled Called", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(getContext(), "onProviderDisabled Called", Toast.LENGTH_SHORT).show();
        }
    };

    private void getDisplacement() {
        double deltay = currentLocation.getLatitude()-lastLocation.getLatitude();
        double deltax = currentLocation.getLongitude()-lastLocation.getLongitude();
        double angularCoef = deltay/deltax;
        double angle = Math.toDegrees(Math.atan(angularCoef));
        if(deltax<0){
            angle = angle-180;
        }
        if(angle<0){
            angle = 360 - angle;
        }
        while(angle>360){
            angle = angle-360;
        }
        double disp = Math.hypot(deltax,deltay);
        Toast.makeText(getContext(), "Mod: "+disp+";Ang: "+angle, Toast.LENGTH_SHORT).show();
        if(angle>=337.5 || angle<22.5){
            //TODO: Move right
            Toast.makeText(getContext(), "Right", Toast.LENGTH_SHORT).show();
            mCanvasDrawing.move(CanvasDrawing.RIGHT);
        }else{
            if(angle>=22.5 && angle<67.5){
                //TODO: Move upright
                Toast.makeText(getContext(), "Upright", Toast.LENGTH_SHORT).show();
                mCanvasDrawing.move(CanvasDrawing.UPRIGHT);
            }else{
                if(angle>=67.5 && angle<112.5){
                    //TODO: Move up
                    Toast.makeText(getContext(), "Up", Toast.LENGTH_SHORT).show();
                    mCanvasDrawing.move(CanvasDrawing.UP);
                }else{
                    if(angle>=112.5 && angle<157.5){
                        //TODO: Move upleft
                        Toast.makeText(getContext(), "Upleft", Toast.LENGTH_SHORT).show();
                        mCanvasDrawing.move(CanvasDrawing.UPLEFT);
                    }else{
                        if(angle>=157.5 && angle<202.5){
                            //TODO: Move left
                            Toast.makeText(getContext(), "Left", Toast.LENGTH_SHORT).show();
                            mCanvasDrawing.move(CanvasDrawing.LEFT);
                        }else{
                            if(angle>=202.5 && angle<247.5){
                                //TODO: Move downleft
                                Toast.makeText(getContext(), "Downleft", Toast.LENGTH_SHORT).show();
                                mCanvasDrawing.move(CanvasDrawing.DOWNLEFT);
                            }else{
                                if(angle>=247.5 && angle<292.5){
                                    //TODO: Move down
                                    Toast.makeText(getContext(), "Down", Toast.LENGTH_SHORT).show();
                                    mCanvasDrawing.move(CanvasDrawing.DOWN);
                                }else{
                                    if(angle>=292.5 && angle<337.5){
                                        //TODO: Move downright
                                        Toast.makeText(getContext(), "Downright", Toast.LENGTH_SHORT).show();
                                        mCanvasDrawing.move(CanvasDrawing.DOWNRIGHT);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        List<PointOfInterest> poi = generatePoi();
//        ListView listView = getView().findViewById(R.id.listview_poi);
//        ArrayAdapter<PointOfInterest> adapter = new ArrayAdapter<>(this.getContext(),android.R.layout.simple_list_item_1, poi);
        //Log.i(TAG, "onViewCreated: poi: "+poi);
//        listView.setAdapter(adapter);

        if (isLocationEnabled()) {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map2);
            //Log.i(TAG, "onCreateView: supportMapFragment: "+supportMapFragment);
            supportMapFragment.getMapAsync(this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
        } else {
            showAlert();
        }

        TextView tv_status = getView().findViewById(R.id.tv_statusInspecao);
        tv_status.setText(R.string.inspStatusRunning);

        mCanvasDrawing = getView().findViewById(R.id.canvas);
        // mCanvasDrawing.draw("rect",0,0);
        /*
        Button addPoint = getView().findViewById(R.id.button_novoPonto);
        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvasDrawing.drawCenterPoint();
            }
        });
        */
        //mCanvasDrawing.drawCenterPoint();

        Button up = getView().findViewById(R.id.up);
        Button down = getView().findViewById(R.id.down);
        Button left = getView().findViewById(R.id.left);
        Button right = getView().findViewById(R.id.right);
        Button upleft = getView().findViewById(R.id.upleft);
        Button upright = getView().findViewById(R.id.upright);
        Button downleft = getView().findViewById(R.id.downleft);
        Button downright = getView().findViewById(R.id.downright);

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvasDrawing.move(CanvasDrawing.UP);
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mCanvasDrawing.move(CanvasDrawing.DOWN);
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mCanvasDrawing.move(CanvasDrawing.LEFT);
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mCanvasDrawing.move(CanvasDrawing.RIGHT);
            }
        });

        upleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mCanvasDrawing.move(CanvasDrawing.UPLEFT);
            }
        });

        upright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mCanvasDrawing.move(CanvasDrawing.UPRIGHT);
            }
        });

        downleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mCanvasDrawing.move(CanvasDrawing.DOWNLEFT);
            }
        });

        downright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mCanvasDrawing.move(CanvasDrawing.DOWNRIGHT);
            }
        });

    }

    private List<PointOfInterest> generatePoi() {
        List<PointOfInterest> list = new ArrayList();
        for (int i = 1; i <= 20; i++) {
            PointOfInterest poi = new PointOfInterest();
            poi.setAddress("Endereço " + i);
            poi.setCoordinates(new LatLng(i, i));
            list.add(poi);
        }
        return list;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        /*
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
        */
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            // Logic to handle location object
            LatLng locale = new LatLng(location.getLatitude(),location.getLongitude());
            marker = mGoogleMap.addMarker(new MarkerOptions().position(locale).title("Localização atual"));
            CameraPosition position = CameraPosition.builder().target(locale).zoom(20).build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
            currentLocation = location;
            lastLocation = location;
        }
    }

}
