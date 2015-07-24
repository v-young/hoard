package com.andrewma.hoard.scan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.appcompat.BuildConfig;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import com.andrewma.hoard.HoardApplication;
import com.andrewma.hoard.R;
import com.andrewma.hoard.data.Device;
import com.andrewma.hoard.data.parse.ParseDataSource;
import com.andrewma.hoard.tags.DeviceTag;
import com.andrewma.hoard.tags.Tag;
import com.andrewma.hoard.tags.UserTag;
import com.bluelinelabs.logansquare.LoganSquare;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
//import butterknife.OnTouch;

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
    @InjectView(R.id.progress_bar) ProgressBar mProgressBar;
    @InjectView(R.id.img_button_scan) ImageButton mScanButton;
    @InjectView(R.id.this_device) Button mThisDeviceButton;
    @InjectView(R.id.version) TextView mVersion;
    @InjectView(R.id.spinnerRecent) Spinner mRecentSpinner;
    @InjectView(R.id.checkOut) TextView mCheckOut;

    private String mScannedDeviceModel;
    private String mScannedDeviceSerial;
    private String mCheckedOutDate;
    private String mScannedUserEmail;
    private ParseDataSource ds = new ParseDataSource();
    private ArrayList<String> userList = ds.GetAllUsers();

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_scan, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_bar_scan:
                scanButtonClick();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_scan, container, false);
        ButterKnife.inject(this, view);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mRecentSpinner.setEnabled(true);
        mRecentSpinner.setPrompt("Select User");

        ArrayAdapter<String>aUserList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, userList);
        mRecentSpinner.setAdapter(aUserList);

        //mVersion.setText(BuildConfig.VERSION_CODE);
        //mUserEmail.setText("Enter e-mail or select from list");

        return view;
    }

    private String getScannedUserEmail() {
        return mUserEmail.getText().toString().trim();
    }

    @OnClick(R.id.this_device)
    void useThisDevice() {
        String serial = Build.SERIAL;

        //Toast.makeText(getActivity(), serial, Toast.LENGTH_LONG).show();
        new GetDeviceTask().execute(serial);
    }

    @OnClick(R.id.img_button_scan)
    void scanButtonClick() {
        final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, SCANNER_REQUEST_CODE);
    }

    private boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    private boolean validateCheckout() {
        if(TextUtils.isEmpty(getScannedUserEmail())) {
            Toast.makeText(getActivity(), "Please type e-mail or scan a user tag", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!isEmailValid(getScannedUserEmail().toLowerCase())) {
            Toast.makeText(getActivity(), "The user field should be a valid email address", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @OnClick(R.id.checkout_button)
    void checkoutClick() {
        if (!validateCheckout()) {
            return;
        }

        if(!TextUtils.isEmpty(mScannedDeviceSerial)) {
            mCheckoutButton.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

            new CheckOutTask().execute(new Device(mScannedDeviceModel, mScannedDeviceSerial));
        }
    }


    @OnClick(R.id.checkin_button)
    void checkinClick() {
        if(!TextUtils.isEmpty(mScannedDeviceSerial)) {
            mCheckoutButton.setVisibility(View.VISIBLE);
            mCheckinButton.setVisibility(View.GONE);

            mProgressBar.setVisibility(View.VISIBLE);
            mCheckOut.setText(null);

            new CheckInTask().execute(new Device(mScannedDeviceModel, mScannedDeviceSerial));
        }
    }

    @OnItemSelected(R.id.spinnerRecent)
    void spinnerClick()
    {
        mUserEmail.setText(mRecentSpinner.getSelectedItem().toString());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCANNER_REQUEST_CODE) {
            // Handle scan intent
            if (resultCode == Activity.RESULT_OK) {
                // Handle successful scan
                final String contents = intent.getStringExtra("SCAN_RESULT");
                boolean updated = false;

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
                            updated = true;
                            break;
                        case USER:
                            final UserTag user = LoganSquare.parse(contents, UserTag.class);
                            mScannedUserEmail = user.email;
                            mUserEmail.setText(mScannedUserEmail);
                            updated = true;
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
                mProgressBar.setVisibility(View.VISIBLE);

                new AddDeviceTask().execute(new Device(mScannedDeviceModel, mScannedDeviceSerial));
                return;
            }

            // Override the value using the value from parse
            mScannedDeviceModel = device.model;
            mScannedDeviceSerial = device.serial;
            mCheckedOutDate = device.checkedOutAt.toString();
            mDeviceModelTextView.setText(mScannedDeviceModel);
            mDeviceSerialTextView.setText(mScannedDeviceSerial);

            if(TextUtils.isEmpty(device.checkedOutTo)) {
                mCheckoutButton.setVisibility(View.VISIBLE);
                mCheckinButton.setVisibility(View.GONE);
                mRecentSpinner.setEnabled(true);
            } else {
                mUserEmail.setText(device.checkedOutTo);
                mCheckOut.setText("Checked out at " + mCheckedOutDate);
                mCheckoutButton.setVisibility(View.GONE);
                mCheckinButton.setVisibility(View.VISIBLE);
                mRecentSpinner.setEnabled(false);
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

            mProgressBar.setVisibility(View.GONE);
        }
    }

    private class CheckOutTask extends AsyncTask<Device, Void, Void> {

        @Override
        protected Void doInBackground(Device... device) {
            HoardApplication.getDataSource().checkout(mScannedDeviceSerial, getScannedUserEmail());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getActivity(), "Device is checked out successfully!", Toast.LENGTH_SHORT).show();
            mCheckoutButton.setVisibility(View.GONE);
            mCheckinButton.setVisibility(View.VISIBLE);
            mRecentSpinner.setEnabled(false);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private class CheckInTask extends AsyncTask<Device, Void, Void> {

        @Override
        protected Void doInBackground(Device... device) {
            HoardApplication.getDataSource().checkin(mScannedDeviceSerial);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getActivity(), "Device is now checked in!", Toast.LENGTH_SHORT).show();
            mCheckoutButton.setVisibility(View.VISIBLE);
            mCheckinButton.setVisibility(View.GONE);
            mRecentSpinner.setEnabled(true);
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
