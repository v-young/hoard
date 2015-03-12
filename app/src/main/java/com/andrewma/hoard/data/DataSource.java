package com.andrewma.hoard.data;

import java.util.List;

public interface DataSource {

    public Device findDeviceById(String serial);

    public List<Device> findDevicesByUser(String email);

    public List<Device> getAllDevices();

    public void addDevice(Device device);

    public void updateDeviceMetadata(Device device);

    public void checkout(String serial, String email);

    public void checkin(String serial);
}
