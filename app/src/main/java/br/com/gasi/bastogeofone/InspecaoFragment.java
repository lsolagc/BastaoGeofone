package br.com.gasi.bastogeofone;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;


/**
 * A simple {@link Fragment} subclass.
 */
public class InspecaoFragment extends Fragment implements OnMapReadyCallback, AdapterView.OnItemClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 6514;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 6515;
    private static final int REQUEST_CHECK_SETTINGS = 6520;
    GoogleMap mGoogleMap;
    CanvasDrawing mCanvasDrawing;
    LocationManager locationManager;
    long minTime = 500, minDistance = 1;
    Marker marker;
    Location currentLocation, lastLocation;
    FusedLocationProviderClient mFusedLocationClient;
    SettingsClient mSettingsClient;
    LocationRequest mLocationRequest;
    Task<LocationSettingsResponse> task;
    ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    ListView listView;
    BluetoothAdapter mBluetoothAdapter;
    AlertDialog alert = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    TextView tv_BTStatus;
    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mDevice = null;
    private LocationCallback mLocationCallback;

    private final String TAG = this.getClass().getSimpleName();
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            if (lastLocation == null) lastLocation = location;
            //Toast.makeText(getContext(), "Localização Alterada: "+location, Toast.LENGTH_SHORT).show();
            LatLng locale = new LatLng(location.getLatitude(), location.getLongitude());
            if (marker != null) marker.remove();
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
    private boolean mRequestingLocationUpdates;


    private void getDisplacement() {
        double deltay = currentLocation.getLatitude() - lastLocation.getLatitude();
        double deltax = currentLocation.getLongitude() - lastLocation.getLongitude();
        double angularCoef = deltay / deltax;
        double angle = Math.toDegrees(Math.atan(angularCoef));
        if (deltax < 0) {
            angle = angle - 180;
        }
        if (angle < 0) {
            angle = 360 - angle;
        }
        while (angle > 360) {
            angle = angle - 360;
        }
        double disp = Math.hypot(deltax, deltay);
        //Toast.makeText(getContext(), "Mod: "+disp+";Ang: "+angle, Toast.LENGTH_SHORT).show();
        if (angle >= 337.5 || angle < 22.5) {
            //Toast.makeText(getContext(), "Right", Toast.LENGTH_SHORT).show();
            mCanvasDrawing.move(CanvasDrawing.RIGHT);
        } else {
            if (angle >= 22.5 && angle < 67.5) {
                // Toast.makeText(getContext(), "Upright", Toast.LENGTH_SHORT).show();
                mCanvasDrawing.move(CanvasDrawing.UPRIGHT);
            } else {
                if (angle >= 67.5 && angle < 112.5) {
                    //Toast.makeText(getContext(), "Up", Toast.LENGTH_SHORT).show();
                    mCanvasDrawing.move(CanvasDrawing.UP);
                } else {
                    if (angle >= 112.5 && angle < 157.5) {
                        //Toast.makeText(getContext(), "Upleft", Toast.LENGTH_SHORT).show();
                        mCanvasDrawing.move(CanvasDrawing.UPLEFT);
                    } else {
                        if (angle >= 157.5 && angle < 202.5) {
                            //Toast.makeText(getContext(), "Left", Toast.LENGTH_SHORT).show();
                            mCanvasDrawing.move(CanvasDrawing.LEFT);
                        } else {
                            if (angle >= 202.5 && angle < 247.5) {
                                //Toast.makeText(getContext(), "Downleft", Toast.LENGTH_SHORT).show();
                                mCanvasDrawing.move(CanvasDrawing.DOWNLEFT);
                            } else {
                                if (angle >= 247.5 && angle < 292.5) {
                                    //Toast.makeText(getContext(), "Down", Toast.LENGTH_SHORT).show();
                                    mCanvasDrawing.move(CanvasDrawing.DOWN);
                                } else {
                                    if (angle >= 292.5 && angle < 337.5) {
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        createLocationRequest();
        createLocationCallback();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        mSettingsClient = LocationServices.getSettingsClient(getContext());
        task = mSettingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
        return v;
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()){
                    //TODO: Update UI
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

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

    private void showAlertBluetooth() {
        try {
            Log.d(TAG, "showAlertBluetooth: started");
            alert = new AlertDialog.Builder(getActivity()).create();
            View mView = getLayoutInflater().inflate(R.layout.dialog_bluetooth_devices, null);
            listView = mView.findViewById(R.id.listviewBTdevices);
            mDeviceListAdapter = new DeviceListAdapter(getContext(), R.layout.array_list_item, mBTDevices);
            listView.setAdapter(mDeviceListAdapter);
            listView.setOnItemClickListener(this);
            mView.findViewById(R.id.btn_dialogDispBTEncontrados).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    getActivity().onBackPressed();
                }
            });
            alert.setView(mView);
            alert.setCancelable(false);
            alert.show();
            Log.d(TAG, "showAlertBluetooth: ended");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "showAlertBluetooth: " + e.getMessage(), e);
        }

    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (isLocationEnabled()) {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map2);
            supportMapFragment.getMapAsync(this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
        } else {
            showAlertLocation();
        }

        TextView tv_status = getView().findViewById(R.id.tv_statusInspecao);
        tv_status.setText(R.string.inspStatusRunning);
        tv_BTStatus = getView().findViewById(R.id.tv_statusBt);

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
            public void onClick(View view) {
                mCanvasDrawing.move(CanvasDrawing.DOWN);
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvasDrawing.move(CanvasDrawing.LEFT);
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvasDrawing.move(CanvasDrawing.RIGHT);
            }
        });

        upleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvasDrawing.move(CanvasDrawing.UPLEFT);
            }
        });

        upright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvasDrawing.move(CanvasDrawing.UPRIGHT);
            }
        });

        downleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvasDrawing.move(CanvasDrawing.DOWNLEFT);
            }
        });

        downright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvasDrawing.move(CanvasDrawing.DOWNRIGHT);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getContext().registerReceiver(mBroadcastReceiver1, BTIntent);

        if (mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            //checkBTpermissions();
            mBluetoothAdapter.startDiscovery();
            if (mBluetoothAdapter.isDiscovering())
                Log.d(TAG, "onViewCreated: Blutooth discovering");
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);

            getContext().registerReceiver(mBroadcastReceiverDiscover, discoverDevicesIntent);
        }

        IntentFilter bondDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getContext().registerReceiver(mBroadcastReceiverBond, bondDevicesIntent);

        mBluetoothConnection = new BluetoothConnectionService(getContext());

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(dataReceiver, new IntentFilter("BastaoGeofoneIncomingData"));

    }

    private void checkBTpermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = getContext().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += getContext().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }
    }

    BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("DATA");
            //Log.d(TAG, "onReceive: data: "+data);
            try {
                JSONObject jsonObject = new JSONObject(data);
                Log.d(TAG, "onDataReceive: JSON: " + jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiverBond = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "mBroadcastReceiverBond: BONDED");
                    if (alert != null) {
                        alert.dismiss();
                        String text = getString(R.string.BTStatusConnectedTo) + device.getName();
                        tv_BTStatus.setText(text);
                        mDevice = device;
                        startConnection();
                    }
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "mBroadcastReceiverBond: BONDING");
                }
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "mBroadcastReceiverBond: BOND NONE");
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
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
            if (action != null && action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.d(TAG, "mBroadcastReceiverDiscover: started");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                if (alert == null) {
                    showAlertBluetooth();
                } else {
                    mDeviceListAdapter.notifyDataSetChanged();
                }
                Log.d(TAG, "mBroadcastReceiverDiscover: ended");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: destroying InspeçãoFragment.java");
        getContext().unregisterReceiver(mBroadcastReceiver1);
        getContext().unregisterReceiver(mBroadcastReceiverDiscover);
        getContext().unregisterReceiver(mBroadcastReceiverBond);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng locale = new LatLng(location.getLatitude(), location.getLongitude());
                    marker = mGoogleMap.addMarker(new MarkerOptions().position(locale).title("Localização atual"));
                    CameraPosition position = CameraPosition.builder().target(locale).zoom(20).build();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                    currentLocation = location;
                    lastLocation = location;
                }
            }
        });

        /*
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            // Logic to handle location object

        }
        */

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onItemClick: DEVICE CLICKED");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();
        Log.d(TAG, "onItemClick: device name: "+deviceName);
        Log.d(TAG, "onItemClick: device address: "+deviceAddress);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0 && pairedDevices.contains(mBTDevices.get(i))){
            if(alert!=null){
                alert.dismiss();
                String text = getString(R.string.BTStatusConnectedTo)+ deviceName;
                tv_BTStatus.setText(text);
                mDevice = mBTDevices.get(i);
                startConnection();
            }
        }
        else{
            Log.d(TAG, "onItemClick: Trying to pair with "+ deviceName);
            mBTDevices.get(i).createBond();
        }

    }

    private void startConnection() {
        if(mDevice!=null) {
            startBTConnection(mDevice, MY_UUID);
        }
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: initializing RFCOM Bluetooth connection.");
        mBluetoothConnection.startClient(device, uuid);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    this.getActivity().onBackPressed();
                }
                return;
            case PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    this.getActivity().onBackPressed();
                }
                return;
        }
    }

}
