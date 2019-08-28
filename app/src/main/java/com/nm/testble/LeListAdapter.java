package com.nm.testble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LeListAdapter extends BaseAdapter implements View.OnClickListener {

    private ArrayList<BluetoothDevice> deviceList;
    private LayoutInflater mLayoutInflater;

    private Activity activity;
    private ArrayList data;
    private static LayoutInflater inflater;
    public Resources res;


    public LeListAdapter(Activity activity){
        super();
        this.activity = activity;
        deviceList = new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device){
        if (!deviceList.contains(device)){
            deviceList.add(device);
        }
    }

    public BluetoothDevice getDevice(int position)
    {
        return deviceList.get(position);
    }

    public void clear(){
        deviceList.clear();
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = activity.getLayoutInflater();
        View myView = inflater.inflate(R.layout.le_device_list_view, null, true);
        TextView tvDeviceName = myView.findViewById(R.id.le_device_name);
        TextView tvAddName = myView.findViewById(R.id.le_device_address);

        tvDeviceName.setText(deviceList.get(position).getName());
        tvAddName.setText(deviceList.get(position).getAddress());

        return myView;
    }

    @Override
    public void onClick(View v) {

    }
}
