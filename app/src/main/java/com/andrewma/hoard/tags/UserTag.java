package com.andrewma.hoard.tags;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class UserTag {

    @JsonField
    public String email;
}
