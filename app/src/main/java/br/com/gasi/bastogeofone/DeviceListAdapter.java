package br.com.gasi.bastogeofone;

import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Resende on 22/02/2018.
 */

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;

    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices){
        super(context, tvResourceId, devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        convertView = mLayoutInflater.inflate(mViewResourceId, null);
        BluetoothDevice device = mDevices.get(position);
        if(device!=null){
            TextView deviceName = (TextView) convertView.findViewById(R.id.tv_deviceName);
            if(deviceName!=null){
                deviceName.setText(device.getName());
            }
        }

        return convertView;
    }

}
