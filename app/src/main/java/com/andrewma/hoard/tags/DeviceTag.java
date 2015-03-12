package com.andrewma.hoard.tags;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class DeviceTag {

    @JsonField
    public String name;

    @JsonField
    public String serial;
}
