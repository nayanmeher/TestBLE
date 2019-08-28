package com.nm.testble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;

import java.util.List;

public class BluetoothLeServices extends Service {

    private final static String TAG = "BluetoothLeServices";
    public final static String ACTION_GATT_CONNECTED = "com.nm.testble.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.nm.testble.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICE_DISCOVERED = "com.nm.testble.le.ACTION_SERVICE_DISCOVERED";
    public final static String ACTION_GATT_ACTION_DATA_AVAILABLE = "com.nm.testble.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.nm.testble.le.EXTRA_DATA";

    public final static int STATE_DISCONNECTED = 0;
    public final static int STATE_CONNECTING =1;
    public final static int STATE_CONNECTED =2;

    public  int conntectionState = STATE_DISCONNECTED;
    private BluetoothGatt bluetoothGatt;
    private BluetoothManager mBtManager;
    private BluetoothAdapter mBtAdapter;
    private String mBtDeviceAddress;
    private Context mContext;

    public BluetoothLeServices(Context context){
        mContext = context;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange()");
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                conntectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.d(TAG, "Connected to broadCast server.");
                //Toast.makeText(mContext, "Ble device Connected", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Attempting to start broadcast discovery:"+bluetoothGatt.discoverServices());

            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                conntectionState = STATE_DISCONNECTED;
                Log.d(TAG, "Disconnected from the GATT server.");
                broadcastUpdate(intentAction);
            }

            //super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServiceDiscovered()");
            if (status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICE_DISCOVERED);
            }
            else {
                Log.w(TAG, "onServiceDiscovered: received status: "+status);
            }
            //super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead()");

            if (status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_ACTION_DATA_AVAILABLE, characteristic);
            }
            //super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged()");
            broadcastUpdate(ACTION_GATT_ACTION_DATA_AVAILABLE, characteristic);

            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    private void broadcastUpdate(final String action){
        Log.d(TAG, "broadcastUpdte(-)");
       // final Intent intent = new Intent(action);
        //sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic){
        Log.d(TAG, "brodcastUpdate(-,-)");
    }

    public class LocalBinder extends Binder{
        BluetoothLeServices getService(){
            return BluetoothLeServices.this;
        }
    }

    public IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        close();
        return super.onUnbind(intent);
    }


    //initialize a ref to local bluetooth adapter
    public boolean initialize(){
        if (mBtManager == null){
            mBtManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBtManager == null){
                Log.e(TAG, "Error in initializing bluetooth manager.");
                return false;
            }
        }
        mBtAdapter = mBtManager.getAdapter();
        if (mBtAdapter == null){
            Log.e(TAG, "Error in initializing Bluetooth Adapter.");
            return false;
        }
        return  true;
    }

    public boolean connect(final String address){
        if (mBtAdapter == null || mBtManager == null){
            Log.e(TAG, "Bluetooth adapter or bluetooth manager failed to load.");
        }

        if (mBtDeviceAddress != null && address.equals(mBtDeviceAddress) && bluetoothGatt != null){
            Log.d(TAG, "Trying to use existing gatt for the connection.");
            if (bluetoothGatt.connect()){
                conntectionState = STATE_CONNECTING;
                return true;
            }
            else {
                return false;
            }
        }

        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        if (device == null){
            Log.w(TAG, "Device not found");
            return false;
        }
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBtDeviceAddress = address;
        conntectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect(){
        if (mBtAdapter == null || bluetoothGatt == null){
            Log.w(TAG, "Bluetooth adapter not initialized.");
            return;
        }
        bluetoothGatt.disconnect();
    }


    public void close(){
        if (bluetoothGatt == null){
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public List<BluetoothGattService> getSupportedGattService(){
        if (bluetoothGatt != null){
            return bluetoothGatt.getServices();
        }
        else {
            return null;
        }
    }
}
