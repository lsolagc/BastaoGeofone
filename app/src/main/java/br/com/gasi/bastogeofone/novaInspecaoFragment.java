package br.com.gasi.bastogeofone;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link novaInspecaoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class novaInspecaoFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 6514;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 6515;
    private static final String TAG = "novaInspecaoFragment";
    private View rootView;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Location mLocation;
    private Context context = getContext();
    private Marker marker;

    public novaInspecaoFragment() {
        // Required empty public constructor
    }

    /**

     */
    public static novaInspecaoFragment newInstance() {
        return new novaInspecaoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this.getContext();
        List<Address> addresses;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onCreate: getView(): " + getView());
        Button btn_iniciarInspecao = getView().findViewById(R.id.button_iniciarInspecao);
        btn_iniciarInspecao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                try {
                    fm.beginTransaction().addToBackStack(null).replace(R.id.flContent, InspecaoFragment.class.newInstance()).commit();
                } catch (java.lang.InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_nova_inspecao, container, false);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map1);
        Log.i(TAG, "onCreateView: supportMapFragment: " + supportMapFragment);
        supportMapFragment.getMapAsync(this);

        return rootView;
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLocation = location;
                            // Logic to handle location object
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                /*
                                Log.i(TAG, "onSuccess: Address: "+addresses.get(0).getAddressLine(0));
                                Log.i(TAG, "onSuccess: SubLocality: "+addresses.get(0).getSubLocality());
                                Log.i(TAG, "onSuccess: Thoroughfare: "+addresses.get(0).getThoroughfare());
                                Log.i(TAG, "onSuccess: SubThoroughfare: "+addresses.get(0).getSubThoroughfare());
                                Log.i(TAG, "onSuccess: Admin: "+addresses.get(0).getAdminArea());
                                Log.i(TAG, "onSuccess: SubAdmin: "+addresses.get(0).getSubAdminArea());
                                */
                                String inspecaoName = "Inspecao em " + addresses.get(0).getThoroughfare() + "," + addresses.get(0).getSubThoroughfare() + "-" + addresses.get(0).getSubAdminArea();
                                EditText nomeInspecao = (EditText) getView().findViewById(R.id.editText_nomeInspecao);
                                EditText enderecoInspecao = (EditText) getView().findViewById(R.id.editText_enderecoInspecao);
                                nomeInspecao.setText(inspecaoName);
                                enderecoInspecao.setText(addresses.get(0).getAddressLine(0));
                                LatLng currentLocale = new LatLng(location.getLatitude(), location.getLongitude());
                                if(marker != null){
                                    marker.remove();
                                }
                                marker = googleMap.addMarker(new MarkerOptions().position(currentLocale).title("Localização atual"));
                                CameraPosition cameraPosition = CameraPosition.builder().target(currentLocale).zoom(20).tilt(45).build();
                                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, R.string.geocoderFail, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    this.getActivity().onBackPressed();
                }
                break;
            case PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    this.getActivity().onBackPressed();
                }
                break;
        }
    }

}
