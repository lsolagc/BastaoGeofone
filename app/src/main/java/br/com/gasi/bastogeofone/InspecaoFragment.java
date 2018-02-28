package br.com.gasi.bastogeofone;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsClient;
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
public class InspecaoFragment extends Fragment implements OnMapReadyCallback, AdapterView.OnItemClickListener {

    GoogleMap mGoogleMap;
    CanvasDrawing mCanvasDrawing;
    LocationManager locationManager;
    long minTime = 500, minDistance = 1;
    Marker marker;
    Location currentLocation, lastLocation;
    FusedLocationProviderClient mFusedLocationClient;
    SettingsClient mSettingsClient;
    LocationRequest mLocationRequest;
    ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    ListView listView;
    BluetoothAdapter mBluetoothAdapter;

    private final String TAG = this.getClass().getSimpleName();
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            if(lastLocation==null) lastLocation = location;
            //Toast.makeText(getContext(), "Localização Alterada: "+location, Toast.LENGTH_SHORT).show();
            LatLng locale = new LatLng(location.getLatitude(),location.getLongitude());
            if(marker!=null) marker.remove();
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
        Log.d(TAG, "getDisplacement: currentLocation: "+currentLocation);
        Log.d(TAG, "getDisplacement: lastLocation: "+lastLocation);
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
        //Toast.makeText(getContext(), "Mod: "+disp+";Ang: "+angle, Toast.LENGTH_SHORT).show();
        if(angle>=337.5 || angle<22.5){
            //Toast.makeText(getContext(), "Right", Toast.LENGTH_SHORT).show();
            mCanvasDrawing.move(CanvasDrawing.RIGHT);
        }else{
            if(angle>=22.5 && angle<67.5){
               // Toast.makeText(getContext(), "Upright", Toast.LENGTH_SHORT).show();
                mCanvasDrawing.move(CanvasDrawing.UPRIGHT);
            }else{
                if(angle>=67.5 && angle<112.5){
                    //Toast.makeText(getContext(), "Up", Toast.LENGTH_SHORT).show();
                    mCanvasDrawing.move(CanvasDrawing.UP);
                }else{
                    if(angle>=112.5 && angle<157.5){
                        //Toast.makeText(getContext(), "Upleft", Toast.LENGTH_SHORT).show();
                        mCanvasDrawing.move(CanvasDrawing.UPLEFT);
                    }else{
                        if(angle>=157.5 && angle<202.5){
                            //Toast.makeText(getContext(), "Left", Toast.LENGTH_SHORT).show();
                            mCanvasDrawing.move(CanvasDrawing.LEFT);
                        }else{
                            if(angle>=202.5 && angle<247.5){
                                //Toast.makeText(getContext(), "Downleft", Toast.LENGTH_SHORT).show();
                                mCanvasDrawing.move(CanvasDrawing.DOWNLEFT);
                            }else{
                                if(angle>=247.5 && angle<292.5){
                                    //Toast.makeText(getContext(), "Down", Toast.LENGTH_SHORT).show();
                                    mCanvasDrawing.move(CanvasDrawing.DOWN);
                                }else{
                                    if(angle>=292.5 && angle<337.5){
                                        //Toast.makeText(getContext(), "Downright", Toast.LENGTH_SHORT).show();
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

    private void showAlertLocation() {
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

    private void showAlertBluetooth(){
        Log.d(TAG, "showAlertBluetooth: started");
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View mView = getLayoutInflater().inflate(R.layout.dialog_bluetooth_devices, null);
        listView = mView.findViewById(R.id.listviewBTdevices);
        mDeviceListAdapter = new DeviceListAdapter(getContext(), R.layout.array_list_item, mBTDevices);
        listView.setAdapter(mDeviceListAdapter);
        listView.setOnItemClickListener(this);
        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
        Log.d(TAG, "showAlertBluetooth: ended");
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
            showAlertLocation();
        }

        TextView tv_status = getView().findViewById(R.id.tv_statusInspecao);
        tv_status.setText(R.string.inspStatusRunning);

        mCanvasDrawing = getView().findViewById(R.id.canvas);

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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getContext().registerReceiver(mBroadcastReceiver1, BTIntent);

        if(mBluetoothAdapter.isEnabled()){
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }
            //checkBTpermissions();
            mBluetoothAdapter.startDiscovery();
            if(mBluetoothAdapter.isDiscovering()) Log.d(TAG, "onViewCreated: Blutooth discovering");
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getContext().registerReceiver(mBroadcastReceiverDiscover, discoverDevicesIntent);
        }

        IntentFilter bondDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getContext().registerReceiver(mBroadcastReceiverBond, bondDevicesIntent);

    }

    private void checkBTpermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = getContext().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += getContext().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck!=0){
                getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},0);
            }
        }
    }

    private final BroadcastReceiver mBroadcastReceiverBond = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(mDevice.getBondState()==BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "mBroadcastReceiverBond: BONDED");
                }
                if(mDevice.getBondState()==BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "mBroadcastReceiverBond: BONDING");
                }
                if(mDevice.getBondState()==BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "mBroadcastReceiverBond: BOND NONE");
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiverDiscover = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                Log.d(TAG, "mBroadcastReceiverDiscover: started");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                showAlertBluetooth();
                Log.d(TAG, "mBroadcastReceiverDiscover: ended");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: destroying InspeçãoFragment.java");
        getContext().unregisterReceiver(mBroadcastReceiver1);

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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getContext());
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
                            currentLocation = location;
                            lastLocation = location;
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onItemClick: DEVICE CLICKED");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();
        Log.d(TAG, "onItemClick: device name: "+deviceName);
        Log.d(TAG, "onItemClick: device address: "+deviceAddress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "onItemClick: Trying to pair with "+ deviceName);
            mBTDevices.get(i).createBond();
        }
    }
}
