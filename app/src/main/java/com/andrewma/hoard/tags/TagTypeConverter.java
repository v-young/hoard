package com.andrewma.hoard.tags;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

public class TagTypeConverter extends StringBasedTypeConverter<TagType> {
    @Override
    public TagType getFromString(String s) {
        return TagType.valueOf(s.toUpperCase());
    }

    public String convertToString(TagType object) {
        return object.toString();
    }

}
