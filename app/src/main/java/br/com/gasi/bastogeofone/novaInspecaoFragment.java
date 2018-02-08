package br.com.gasi.bastogeofone;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link novaInspecaoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class novaInspecaoFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "novaInspecaoFragment";
    private View rootView;
    GoogleMap mGoogleMap;
    MapView mMapView;
    Location mLocation;
    Context context = getContext();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public novaInspecaoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment novaInspecaoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static novaInspecaoFragment newInstance(String param1, String param2) {
        novaInspecaoFragment fragment = new novaInspecaoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        final Context context = this.getContext();
        List<Address> addresses;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onCreate: getView(): " + getView());
        Button btn_iniciarInspecao = (Button) getView().findViewById(R.id.button_iniciarInspecao);
        btn_iniciarInspecao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                try {
                    fm.beginTransaction().addToBackStack(null).replace(R.id.flContent, InspecaoFragment.class.newInstance()).commit();
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
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
                                googleMap.addMarker(new MarkerOptions().position(currentLocale).title("Localização atual"));
                                CameraPosition cameraPosition = CameraPosition.builder().target(currentLocale).zoom(20).tilt(45).build();
                                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

    }
}
