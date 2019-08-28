package com.nm.testble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothModule extends AppCompatActivity {

    private static final String TAG = "BluetoothModule:";

    private BluetoothAdapter mBtAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private LeListAdapter leAdapter;
    private static final long SCANNING_PERIOD = 10000;
    private ListView leDeviceList;
    private boolean mConnected;
    private String mLeDeviceAddress;


    private BluetoothLeServices bleService;
    //manage service lifecycle
   /* private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BluetoothLeServices.LocalBinder)service).getService();
            if (!bleService.initialize()){
                Log.e(TAG, "unable to initialize bluetooth.");
                finish();
            }
            bleService.initialize();
            bleService.connect(mLeDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };*/

    /*private final BroadcastReceiver mGattUpdateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeServices.ACTION_GATT_CONNECTED.equals(action)){
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }
            else if (BluetoothLeServices.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                updateConnectionState(R.string.disconnect);
                invalidateOptionsMenu();
            }
            else if (BluetoothLeServices.ACTION_GATT_SERVICE_DISCOVERED.equals(action)){
                  //  displayGattServices(bleService.getSupportedGattService());
            }
            else if (BluetoothLeServices.ACTION_GATT_ACTION_DATA_AVAILABLE.equals(action)){
               // displayData(intent.getStringExtra(BluetoothLeServices.EXTRA_DATA));
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_module);

        bleService = new BluetoothLeServices(getApplicationContext());

        leDeviceList = findViewById(R.id.le_device_list);


        leAdapter = new LeListAdapter(this);

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getApplicationContext(), "Ble not supported in your device.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //initialize the bluetooth adapter
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = manager.getAdapter();

        // enable bt
        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnableIntent, 0);
        }




    }

    @Override
    protected void onResume() {
        //registerReceiver(mGattUpdateReciever, makeGattUpdateIntentFilter());
       // if (bleService != null){
          //  bleService.initialize();
           //final boolean result = bleService.connect(mLeDeviceAddress);
            //Log.d(TAG, "Connect request result: "+result);
       // }
        super.onResume();
    }

    @Override
    protected void onPause() {
     //   unregisterReceiver(mGattUpdateReciever);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    //   unbindService(mServiceConnection);
        bleService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bt_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_device: {
                startScan();
                Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_LONG).show();

            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void startScan(){

        //ask permission for location
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location permission required.");
                builder.setMessage("This application does not location information, but scanning LE device require this permission.");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                    }
                });
                builder.show();
                return;
            }
        }

       // mBtAdapter.startDiscovery();
        scanLeDevice(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            scanLeDevice(true);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location permission denied.");
            builder.setMessage("As location permission not granted this application can not run.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void scanLeDevice(final boolean enable){
        if (enable){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBtAdapter.stopLeScan(leScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCANNING_PERIOD);
            mScanning = true;
            mBtAdapter.startLeScan(leScanCallback);

        }
        else {
            mScanning = false;
            mBtAdapter.stopLeScan(leScanCallback);
        }


    }
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leAdapter.addDevice(device);
                            leAdapter.notifyDataSetChanged();
                            leDeviceList.setAdapter(leAdapter);

                            //set onclick listener on the list view
                            leDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Log.d(TAG, "Postion:"+position +"parent: "+parent.getItemAtPosition(position));
                                    mLeDeviceAddress = ((BluetoothDevice)parent.getItemAtPosition(position)).getAddress();
                                    /*if (bleService.conntectionState == BluetoothLeServices.STATE_DISCONNECTED){
                                        Log.d(TAG, "device is connected state. disconnecting.");
                                        bleService.disconnect();
                                    }
                                    else {
                                        Log.d(TAG, "Device is disconnected state. connecting.");
                                    }*/
                                    bleService.initialize();
                                    bleService.connect(mLeDeviceAddress);

                                }
                            });
                            Log.d("BluetoothModule:","device::"+device.getAddress());
                        }
                    });
                }
            };

    private void updateConnectionState(final int resId){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeServices.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeServices.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeServices.ACTION_GATT_SERVICE_DISCOVERED);
        intentFilter.addAction(BluetoothLeServices.ACTION_GATT_ACTION_DATA_AVAILABLE);
        return intentFilter;
    }



}
