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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class BluetoothModule extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private LeListAdapter leAdapter;
    private static final long SCANNING_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_module);



        leAdapter = new LeListAdapter();

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
                            Log.d("BluetoothModule:","device::"+device.getAddress());
                        }
                    });
                }
            };

    private void addDevice(BluetoothDevice device){

    }
}
