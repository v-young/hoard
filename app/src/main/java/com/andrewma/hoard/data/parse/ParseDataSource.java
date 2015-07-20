package com.andrewma.hoard.data.parse;

import android.util.Log;

import com.andrewma.hoard.data.DataSource;
import com.andrewma.hoard.data.Device;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ParseDataSource implements DataSource {

    private static final String TAG = ParseDataSource.class.getSimpleName();

    private static final String DEVICES_CLASS_NAME = "Devices";
    private static final String DEVICES_CHECKED_OUT_TO = "checkedOutTo";
    private static final String DEVICES_CHECKED_OUT_AT = "checkedOutAt";
    private static final String DEVICES_SERIAL = "serial";
    private static final String DEVICES_MODEL = "model";

    private static final String AUDIT_CLASS_NAME = "Audit";
    private static final String AUDIT_SERIAL = DEVICES_SERIAL;
    private static final String AUDIT_CHECKED_OUT_TO = DEVICES_CHECKED_OUT_TO;
    private static final String AUDIT_CHECKED_OUT_AT = DEVICES_CHECKED_OUT_AT;
    private static final String AUDIT_CHECKED_IN_AT = "checkedInAt";

    @Override
    public Device findDeviceById(String serial) {
        ParseObject parseDevice = findParseDeviceById(serial);
        if(parseDevice == null) {
            parseDevice = findParseDeviceById(serial.toUpperCase());
        }
        if(parseDevice == null) {
            parseDevice = findParseDeviceById(serial.toLowerCase());
        }
        if (parseDevice == null) {
            return null;
        }
        return parseDeviceObject(parseDevice);
    }

    private ParseObject findParseDeviceById(String serial) {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery(DEVICES_CLASS_NAME);
        query.whereEqualTo(DEVICES_SERIAL, serial);
        try {
            final List<ParseObject> parseDevices = query.find();
            if(parseDevices.size() == 0) {
                return null;
            }
            return parseDevices.get(0);
        } catch (ParseException e) {
            Log.e(TAG, "Failure getting data", e);
            return null;
        }
    }

    @Override
    public List<Device> getAllDevices() {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery(DEVICES_CLASS_NAME);
        try {
            final List<ParseObject> parseDevices = query.find();
            if(parseDevices.size() == 0) {
                return null;
            }
            final List<Device> devices = new ArrayList<>();
            for(ParseObject parseDevice : parseDevices) {
                devices.add(parseDeviceObject(parseDevice));
            }
            return devices;
        } catch (ParseException e) {
            Log.e(TAG, "Failure getting data", e);
            return null;
        }
    }

    public void addDevice(Device device) {
        final ParseObject parseDevice = new ParseObject(DEVICES_CLASS_NAME);
        parseDevice.put(DEVICES_SERIAL, device.serial);
        parseDevice.put(DEVICES_MODEL, device.model);
        try {
            parseDevice.save();
        } catch (ParseException e) {
            Log.e(TAG, "Failure saving data", e);
        }
    }

    private Device parseDeviceObject(ParseObject parseDevice) {
        final Device device = new Device();
        device.serial = parseDevice.getString(DEVICES_SERIAL);
        device.model = parseDevice.getString(DEVICES_MODEL);
        device.checkedOutTo = parseDevice.getString(DEVICES_CHECKED_OUT_TO);
        device.checkedOutAt = parseDevice.getDate(DEVICES_CHECKED_OUT_AT);
        return device;
    }

    @Override
    public List<Device> findDevicesByUser(String email) {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery(DEVICES_CLASS_NAME);
        query.whereEqualTo(DEVICES_CHECKED_OUT_TO, email);
        try {
            final List<ParseObject> parseDevices = query.find();
            if(parseDevices.size() == 0) {
                return null;
            }
            final List<Device> devices = new ArrayList<>();
            for(ParseObject parseDevice : parseDevices) {
                devices.add(parseDeviceObject(parseDevice));
            }
            return devices;
        } catch (ParseException e) {
            Log.e(TAG, "Failure getting data", e);
            return null;
        }
    }

    @Override
    public void updateDeviceMetadata(Device device) {

    }

    @Override
    public void checkout(String serial, String email) {
        final Date checkoutDate = new Date();

        final ParseObject parseDevice = findParseDeviceById(serial);
        parseDevice.put(DEVICES_CHECKED_OUT_TO, email);
        parseDevice.put(DEVICES_CHECKED_OUT_AT, checkoutDate);
        parseDevice.saveEventually();
    }

    @Override
    public void checkin(String serial) {
        final ParseObject parseDevice = findParseDeviceById(serial);
        final String email = parseDevice.getString(DEVICES_CHECKED_OUT_TO);
        final Date checkoutDate = parseDevice.getDate(DEVICES_CHECKED_OUT_AT);
        parseDevice.remove(DEVICES_CHECKED_OUT_TO);
        parseDevice.remove(DEVICES_CHECKED_OUT_AT);
        parseDevice.saveEventually();

        final ParseObject parseAudit = new ParseObject(AUDIT_CLASS_NAME);
        parseAudit.put(AUDIT_SERIAL, serial);
        parseAudit.put(AUDIT_CHECKED_OUT_TO, email);
        parseAudit.put(AUDIT_CHECKED_OUT_AT, checkoutDate);
        parseAudit.put(AUDIT_CHECKED_IN_AT, new Date());
        parseAudit.saveEventually();
    }
}
