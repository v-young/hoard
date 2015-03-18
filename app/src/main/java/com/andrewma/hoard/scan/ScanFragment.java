package com.andrewma.hoard.scan;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewma.hoard.HoardApplication;
import com.andrewma.hoard.R;
import com.andrewma.hoard.data.Device;
import com.andrewma.hoard.tags.DeviceTag;
import com.andrewma.hoard.tags.Tag;
import com.andrewma.hoard.tags.UserTag;
import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment {

    public int SCANNER_REQUEST_CODE = 123;

    @InjectView(R.id.device_model) TextView mDeviceModelTextView;
    @InjectView(R.id.device_serial) TextView mDeviceSerialTextView;
    @InjectView(R.id.user) EditText mUserEmail;
    @InjectView(R.id.checkout_button) Button mCheckoutButton;
    @InjectView(R.id.checkin_button) Button mCheckinButton;

    private String mScannedDeviceModel;
    private String mScannedDeviceSerial;

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_scan, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    private String getScannedUserEmail() {
        return mUserEmail.getText().toString().trim();
    }

    @OnClick(R.id.scan_button)
    void scanButtonClick() {
        final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, SCANNER_REQUEST_CODE);
    }

    @OnClick(R.id.checkout_button)
    void checkoutClick() {
        if(TextUtils.isEmpty(getScannedUserEmail())) {
            Toast.makeText(getActivity(), "Please type e-mail or scan a user tag", Toast.LENGTH_LONG).show();
            return;
        }

        if(!TextUtils.isEmpty(mScannedDeviceSerial)) {
            mCheckoutButton.setVisibility(View.GONE);
            HoardApplication.getDataSource().checkout(mScannedDeviceSerial, getScannedUserEmail());
        }
    }

    @OnClick(R.id.checkin_button)
    void checkinClick() {
        if(!TextUtils.isEmpty(mScannedDeviceSerial)) {
            mCheckoutButton.setVisibility(View.GONE);
            HoardApplication.getDataSource().checkin(mScannedDeviceSerial);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCANNER_REQUEST_CODE) {
            // Handle scan intent
            if (resultCode == Activity.RESULT_OK) {
                // Handle successful scan
                final String contents = intent.getStringExtra("SCAN_RESULT");

                try {
                    final Tag tag = LoganSquare.parse(contents, Tag.class);

                    switch (tag.type) {
                        case DEVICE:
                            final DeviceTag device = LoganSquare.parse(contents, DeviceTag.class);
                            mScannedDeviceModel = device.model;
                            mScannedDeviceSerial = device.serial;
                            mDeviceModelTextView.setText(mScannedDeviceModel);
                            mDeviceSerialTextView.setText(mScannedDeviceSerial);
                            new GetDeviceTask().execute(mScannedDeviceSerial);
                            break;
                        case USER:
                            final UserTag user = LoganSquare.parse(contents, UserTag.class);
                            mUserEmail.setText(user.email);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetDeviceTask extends AsyncTask<String, Void, Device> {

        @Override
        protected Device doInBackground(String... serial) {
            return HoardApplication.getDataSource().findDeviceById(serial[0]);
        }

        @Override
        protected void onPostExecute(Device device) {
            super.onPostExecute(device);

            if(device == null) {
                Toast.makeText(getActivity(), "Device has not been registered. Adding to database", Toast.LENGTH_LONG).show();
                mCheckoutButton.setEnabled(false);
                new AddDeviceTask().execute(new Device(mScannedDeviceModel, mScannedDeviceSerial));
                return;
            }

            if(TextUtils.isEmpty(device.checkedOutTo)) {
                mCheckoutButton.setVisibility(View.VISIBLE);
                mCheckinButton.setVisibility(View.GONE);
            } else {
                mUserEmail.setText(device.checkedOutTo);
                mCheckoutButton.setVisibility(View.GONE);
                mCheckinButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private class AddDeviceTask extends AsyncTask<Device, Void, Void> {

        @Override
        protected Void doInBackground(Device... device) {
            HoardApplication.getDataSource().addDevice(device[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mCheckoutButton.setVisibility(View.VISIBLE);
            mCheckinButton.setVisibility(View.GONE);
        }
    }
}
