package com.andrewma.hoard.devicelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andrewma.hoard.R;
import com.andrewma.hoard.data.Device;

import java.util.List;

class DeviceListAdapter extends ArrayAdapter<Device> {

    private final Context mContext;
    private final List<Device> mDevices;

    public DeviceListAdapter(Context context, List<Device> devices) {
        super(context, R.layout.fragment_device_list_item, devices);
        mContext = context;
        mDevices = devices;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.fragment_device_list_item, parent, false);

        final TextView name = (TextView) rowView.findViewById(R.id.name);
        final TextView serial = (TextView) rowView.findViewById(R.id.serial);
        final TextView checkedOut = (TextView) rowView.findViewById(R.id.checkedOut);
        final TextView checkoutDate = (TextView) rowView.findViewById(R.id.checkoutDate);

        final Device device = mDevices.get(position);
        name.setText(device.model);
        serial.setText(device.serial);
        if (device.checkedOutTo != null) {
            checkedOut.setText(device.checkedOutTo);
            checkoutDate.setText("Checked out: " + device.checkedOutAt);
        }
        else {
            checkedOut.setHeight(1);
            checkoutDate.setText (null);
            checkoutDate.setHeight(1);
        }
        return rowView;
    }

    public String getSerial(int position) {
        return mDevices.get(position).serial;
    }
}