package br.com.gasi.bastogeofone;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Resende on 22/02/2018.
 */

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "Bastao Geofone";
    private static final UUID MY_UUID = UUID.fromString("045b19d2-bfab-423c-9227-139c4243d90d");
    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID);
                Log.d(TAG, "AcceptThread: Setting up Server using: "+MY_UUID);
            }
            catch(Exception e){
                Log.d(TAG, "AcceptThread: Exception: "+e.getMessage());
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread running");

            BluetoothSocket socket = null;

            try {
                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection");
            } catch (IOException e) {
                Log.e(TAG, "run: IOException: "+e.getMessage(), e);
                e.printStackTrace();
            }

            if(socket != null){
                connected(socket, mmDevice);
            }

            Log.i(TAG, "run: END mAcceptThread");
        }

        public void cancel(){
            try{
                mmServerSocket.close();
            }catch (IOException e){
                Log.e(TAG, "cancel: Close of AcceptThread server socket failed: "+e.getMessage(), e );
            }
        }

    }

    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: started");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "run: run mConnectThread");
            try {
                Log.d(TAG, "run: ConnectThread: Trying to create InsecureRfCommSocket using: "+MY_UUID);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "run: could not create InsecureRfCommSocket:"+e.getMessage(), e);
                e.printStackTrace();
            }
            mmSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected");
            } catch (IOException e) {
                Log.e(TAG, "run: could not connect to device", e);
                try{
                    mmSocket.close();
                    Log.d(TAG, "run: socket closed");
                }catch (IOException el){
                    Log.e(TAG, "run: mConnectThread: unable to close connection in socket: "+ el.getMessage(), el);
                    el.printStackTrace();
                }
                e.printStackTrace();
            }

            connected(mmSocket, mmDevice);

        }

        public void cancel(){
            try{
                Log.d(TAG, "cancel: Closing Client socket");
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG, "cancel: close() if socket in ConnectThread failed: "+e.getMessage(), e);
                e.printStackTrace();
            }
        }

    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread: starting");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mProgressDialog.dismiss();

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: couldn't get input/output stream from socket:"+e.getMessage(), e);
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run(){
            byte[] buffer = new byte[1024];

            int bytes;

            while (true){
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "run: InputStream: incominMessage: "+incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "run: couldn't read input stream", e);
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG, "cancel: could not close socket", e);
                e.printStackTrace();
            }
        }

    }

    public synchronized void start(){
        Log.d(TAG, "start");
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid){
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please wait...", true);
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice){
        Log.d(TAG, "connected: Starting");
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }
}
