package com.andrewma.hoard.data;

import java.util.Date;

public class Device {
    public String model;
    public String serial;
    public String checkedOutTo;
    public Date checkedOutAt;

    public Device() { }


    public Device(String model, String serial) {
        this.model = model;
        this.serial = serial;
    }
}
