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
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class InspecaoFragment extends Fragment implements OnMapReadyCallback, AdapterView.OnItemClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 6514;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 6515;
    private static final int REQUEST_CHECK_SETTINGS = 6520;
    private static final String PAUSE_COMM_MSG = "STOP";
    private static final String RESUME_COMM_MSG = "SEND";
    private static final float MINIMUM_DISTANCE = (float) 0.5;
    private GoogleMap mGoogleMap;
    private CanvasDrawing mCanvasDrawing;
    private LocationManager locationManager;
    private long FASTEST_INTERVAL = 1000, INTERVAL = 2000;
    private Marker marker;
    private Location currentLocation, lastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private Task<LocationSettingsResponse> task;
    private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    private DeviceListAdapter mDeviceListAdapter;
    private ListView listView;
    private BluetoothAdapter mBluetoothAdapter;
    private AlertDialog bluetoothDialog = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private TextView tv_BTStatus;
    private BluetoothConnectionService mBluetoothConnection;
    private BluetoothDevice mDevice = null;
    private LocationCallback mLocationCallback;
    private float mDistance;
    private boolean mInspecaoAtiva = false;
    private AlertDialog.Builder endDialog;
    private JSONArray coordValues = new JSONArray();
    private SQLiteHelper sqLiteHelper;
    private String filename;

    private final String TAG = this.getClass().getSimpleName();
    private boolean isWaitingForResponse = false;
    private JSONObject jsonObject;

    public InspecaoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_inspecao, container, false);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        sqLiteHelper = new SQLiteHelper(this.getContext());

        createLocationRequest();
        createLocationCallback();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        mSettingsClient = LocationServices.getSettingsClient(getActivity());
        task = mSettingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this.getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                }
                /*
                Log.d(TAG, "onSuccess: mLocationRequest: "+mLocationRequest.toString());
                Log.d(TAG, "onSuccess: mLocationCallback: "+mLocationCallback.toString());
                */
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        });

        task.addOnFailureListener(this.getActivity(), new OnFailureListener() {
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

    private float measure(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
        float R = (float) 6378.137; // Radius of earth in KM
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float d = (float) (R * c);
        return d * 1000; // meters
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Log.d(TAG, "onLocationResult: locationResult: "+locationResult.toString());
                    for(Location location : locationResult.getLocations()){
                        //Log.d(TAG, "onLocationResult: location: "+location.toString());
                        currentLocation = location;
                        if (lastLocation == null) {
                            lastLocation = location;
                        }
                        if (jsonObject != null && !isWaitingForResponse) {
                            Log.d(TAG, "onLocationResult: calling getDisplacement(double)");
                            double value;
                            if (jsonObject.has("Valor")) {
                                try {
                                    value = (double) jsonObject.get("Valor");
                                    getDisplacement(value);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (mDistance > MINIMUM_DISTANCE) {
                                    LatLng locale = new LatLng(location.getLatitude(), location.getLongitude());
                                    if (marker != null) {
                                        marker.remove();
                                    }
                                    marker = mGoogleMap.addMarker(new MarkerOptions().position(locale).title("Localização atual"));
                                    CameraPosition position = CameraPosition.builder().target(locale).zoom(20).build();
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                                    lastLocation = currentLocation;
                                    if(mBluetoothConnection.getmConnectedThread() != null && !isWaitingForResponse){
                                        mBluetoothConnection.write(RESUME_COMM_MSG.getBytes(Charset.defaultCharset()));
                                        isWaitingForResponse = true;
                                    }
                                }
                            }
                        }
                        //Toast.makeText(getContext(), "Localização Alterada: "+location, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    /**
     * Método dummy, utilizado para testes sem o recebimento de dados via Bluetooth
     */
    private void getDisplacement() {
        double deltay = currentLocation.getLatitude() - lastLocation.getLatitude();
        double deltax = currentLocation.getLongitude() - lastLocation.getLongitude();
        Log.d(TAG, "getDisplacement: [deltax deltay]: ["+deltax+" "+deltay+"]");
        double angularCoef = deltay / deltax;
        double angle = Math.toDegrees(Math.atan(angularCoef));
        if (deltax < 0 && deltay > 0) {
            angle = angle - 180;
        }else if(deltax < 0 && deltay < 0){
            angle = angle + 180;
        }
        while (angle < 0){
            angle = angle + 360;
        }
        while (angle > 360) {
            angle = angle - 360;
        }
        mDistance = measure(currentLocation.getLatitude(), currentLocation.getLongitude(), lastLocation.getLatitude(), lastLocation.getLongitude());
        Log.d(TAG, "getDisplacement: disp: "+mDistance+"; Angle: "+angle);
        if (mDistance > MINIMUM_DISTANCE) {
            if (angle >= 337.5 || angle < 22.5) {
                //Log.d(TAG, "getDisplacement: right");
                mCanvasDrawing.move(CanvasDrawing.RIGHT);
            } else {
                if (angle >= 22.5 && angle < 67.5) {
                    //Log.d(TAG, "getDisplacement: upright");
                    mCanvasDrawing.move(CanvasDrawing.UPRIGHT);
                } else {
                    if (angle >= 67.5 && angle < 112.5) {
                        //Log.d(TAG, "getDisplacement: up");
                        mCanvasDrawing.move(CanvasDrawing.UP);
                    } else {
                        if (angle >= 112.5 && angle < 157.5) {
                            //Log.d(TAG, "getDisplacement: upleft");
                            mCanvasDrawing.move(CanvasDrawing.UPLEFT);
                        } else {
                            if (angle >= 157.5 && angle < 202.5) {
                                //Log.d(TAG, "getDisplacement: left");
                                mCanvasDrawing.move(CanvasDrawing.LEFT);
                            } else {
                                if (angle >= 202.5 && angle < 247.5) {
                                    //Log.d(TAG, "getDisplacement: downleft");
                                    mCanvasDrawing.move(CanvasDrawing.DOWNLEFT);
                                } else {
                                    if (angle >= 247.5 && angle < 292.5) {
                                        //Log.d(TAG, "getDisplacement: down");
                                        mCanvasDrawing.move(CanvasDrawing.DOWN);
                                    } else {
                                        if (angle >= 292.5 && angle < 337.5) {
                                            //Log.d(TAG, "getDisplacement: downright");
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
    }

    private void getDisplacement(double receivedIntensity) {
        double deltay = currentLocation.getLatitude() - lastLocation.getLatitude();
        double deltax = currentLocation.getLongitude() - lastLocation.getLongitude();
        Log.d(TAG, "getDisplacement: [deltax deltay]: ["+deltax+" "+deltay+"]");
        double angularCoef = deltay / deltax;
        double angle = Math.toDegrees(Math.atan(angularCoef));
        if (deltax < 0 && deltay > 0) {
            angle = angle - 180;
        }else if(deltax < 0 && deltay < 0){
            angle = angle + 180;
        }
        while (angle < 0){
            angle = angle + 360;
        }
        while (angle > 360) {
            angle = angle - 360;
        }
        mDistance = measure(currentLocation.getLatitude(), currentLocation.getLongitude(), lastLocation.getLatitude(), lastLocation.getLongitude());
        Log.d(TAG, "getDisplacement: disp: "+mDistance+"; Angle: "+angle);
        if (mDistance > MINIMUM_DISTANCE) {
            storeLatLngVal(currentLocation.getLatitude(), currentLocation.getLongitude(), receivedIntensity);
            if (angle >= 337.5 || angle < 22.5) {
                //Log.d(TAG, "getDisplacement: right");
                mCanvasDrawing.move(CanvasDrawing.RIGHT, receivedIntensity);
            } else {
                if (angle >= 22.5 && angle < 67.5) {
                    //Log.d(TAG, "getDisplacement: upright");
                    mCanvasDrawing.move(CanvasDrawing.UPRIGHT, receivedIntensity);
                } else {
                    if (angle >= 67.5 && angle < 112.5) {
                        //Log.d(TAG, "getDisplacement: up");
                        mCanvasDrawing.move(CanvasDrawing.UP, receivedIntensity);
                    } else {
                        if (angle >= 112.5 && angle < 157.5) {
                            //Log.d(TAG, "getDisplacement: upleft");
                            mCanvasDrawing.move(CanvasDrawing.UPLEFT, receivedIntensity);
                        } else {
                            if (angle >= 157.5 && angle < 202.5) {
                                //Log.d(TAG, "getDisplacement: left");
                                mCanvasDrawing.move(CanvasDrawing.LEFT, receivedIntensity);
                            } else {
                                if (angle >= 202.5 && angle < 247.5) {
                                    //Log.d(TAG, "getDisplacement: downleft");
                                    mCanvasDrawing.move(CanvasDrawing.DOWNLEFT, receivedIntensity);
                                } else {
                                    if (angle >= 247.5 && angle < 292.5) {
                                        //Log.d(TAG, "getDisplacement: down");
                                        mCanvasDrawing.move(CanvasDrawing.DOWN, receivedIntensity);
                                    } else {
                                        if (angle >= 292.5 && angle < 337.5) {
                                            //Log.d(TAG, "getDisplacement: downright");
                                            mCanvasDrawing.move(CanvasDrawing.DOWNRIGHT, receivedIntensity);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void storeLatLngVal(double latitude, double longitude, double receivedIntensity) {
        try {
            JSONObject mJson = new JSONObject();
            mJson.put("lat",latitude);
            mJson.put("lng",longitude);
            mJson.put("val",receivedIntensity);
            coordValues.put(mJson);
            Log.d(TAG, "storeLatLngVal: coordValues: "+coordValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stopping location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        if(bluetoothDialog != null){
            bluetoothDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
        // Starting location updates
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        if(mBluetoothConnection.getmConnectedThread() != null){
            mBluetoothConnection.write(RESUME_COMM_MSG.getBytes(Charset.defaultCharset()));
            isWaitingForResponse = true;
        }
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showAlertLocation() {
        final AlertDialog.Builder locationDialog = new AlertDialog.Builder(this.getContext());
        locationDialog.setTitle("Enable Location")
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
        locationDialog.show();
    }

    private void showAlertBluetooth() {
        try {
            Log.d(TAG, "showAlertBluetooth: started");
            bluetoothDialog = new AlertDialog.Builder(getActivity()).create();
            View mView = getLayoutInflater().inflate(R.layout.dialog_bluetooth_devices, null);
            listView = mView.findViewById(R.id.listviewBTdevices);
            mDeviceListAdapter = new DeviceListAdapter(getContext(), R.layout.array_list_item, mBTDevices);
            listView.setAdapter(mDeviceListAdapter);
            listView.setOnItemClickListener(this);
            mView.findViewById(R.id.btn_dialogDispBTEncontrados).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bluetoothDialog.dismiss();
                    getActivity().onBackPressed();
                }
            });
            bluetoothDialog.setView(mView);
            bluetoothDialog.setCancelable(false);
            bluetoothDialog.show();
            Log.d(TAG, "showAlertBluetooth: ended");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "showAlertBluetooth: " + e.getMessage(), e);
        }

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (isLocationEnabled()) {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map2);
            supportMapFragment.getMapAsync(this);
        } else {
            showAlertLocation();
        }
        TextView tv_status;
        try {
            tv_status = getView().findViewById(R.id.tv_statusInspecao);
            tv_status.setText(R.string.inspStatusRunning);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        tv_BTStatus = getView().findViewById(R.id.tv_statusBt);
        mCanvasDrawing = getView().findViewById(R.id.canvas);

        Button endInspecao = getView().findViewById(R.id.btn_endInspecao);

        endInspecao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                encerrarInspecao();
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

    private void encerrarInspecao() {
        endDialog = new AlertDialog.Builder(getActivity());
        //View mView = getLayoutInflater().inflate(R.layout.dialog_save_inspecao, null);
        endDialog.setCancelable(false);
        endDialog.setTitle("Encerrando inspeção");
        endDialog.setMessage("Deseja salvar os resultados da inspeção?");
        endDialog.setPositiveButton(R.string.AlertYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try{
                    saveCanvas();
                    saveToSQLite();
                    Toast.makeText(getContext(), "Inspeção salva sob o nome \""+filename+"\"", Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                    getActivity().onBackPressed();
                }catch (Exception e){
                    Toast.makeText(getContext(), "Ocorreu um erro ao salvar a inspeção. Mensagem de erro: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }).setNegativeButton(R.string.AlertNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().onBackPressed();
            }
        }).show();
    }

    private void saveToSQLite() {
        DateFormat df = new SimpleDateFormat("dd-MMM-yyyy H:mm");
        Date date = Calendar.getInstance().getTime();
        String name = df.format(date);
        //Criar a tabela da inspeção
        sqLiteHelper.novaInspecao(name, coordValues);
    }

    private void saveCanvas() {
        try{
            View content = getView().findViewById(R.id.canvas);
            content.setDrawingCacheEnabled(true);
            content.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            Bitmap bitmap = content.getDrawingCache();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(TAG, "saveCanvas: path: "+path);
            File root = new File(path+"/BastaoGeofone");
            DateFormat df = new SimpleDateFormat("dd-MMM-yyyy H:mm");
            Date date = Calendar.getInstance().getTime();
            filename = "Inspeção " + df.format(date);
            if(!root.exists()){
                root.mkdir();
            }
            File file = new File(path+"/BastaoGeofone/"+filename+ ".jpeg");
            FileOutputStream ostream;
            try {
                file.createNewFile();
                ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.flush();
                ostream.close();
                //Toast.makeText(getContext(), "image saved", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(getContext(), "error", Toast.LENGTH_LONG).show();
            }
        }catch(NullPointerException err){
            err.printStackTrace();
        }

    }

    BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mInspecaoAtiva = true;
            try{
                Button endInspecao = getView().findViewById(R.id.btn_endInspecao);
                if (endInspecao != null){
                    endInspecao.setEnabled(true);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            String data = intent.getStringExtra("DATA");
            Log.d(TAG, "onReceive: data: "+data);
            try {
                if(data.contains(RESUME_COMM_MSG)){
                    data = data.replace(RESUME_COMM_MSG, "");
                }else if(data.contains(PAUSE_COMM_MSG)){
                    data = data.replace(PAUSE_COMM_MSG, "");
                }
                jsonObject = new JSONObject(data);
                Log.d(TAG, "onDataReceive: JSON: " + jsonObject);
                isWaitingForResponse = false;
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
                    if (bluetoothDialog != null) {
                        bluetoothDialog.dismiss();
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
                if (bluetoothDialog == null) {
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
            if(bluetoothDialog !=null){
                bluetoothDialog.dismiss();
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
                break;
            case PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    this.getActivity().onBackPressed();
                }
                break;
        }
    }

}
