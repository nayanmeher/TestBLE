package com.nm.testble;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class LeListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> deviceList;
    private LayoutInflater mLayoutInflater;

    public LeListAdapter(){
        super();
        deviceList = new ArrayList<>();

    }

    public void addDevice(BluetoothDevice device){
        if (!deviceList.contains(device)){
            deviceList.add(device);
        }
    }

    public BluetoothDevice getDevice(int position){
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
        return null;
    }
}
