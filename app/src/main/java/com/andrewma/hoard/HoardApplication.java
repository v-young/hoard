package com.andrewma.hoard;

import android.app.Application;

import com.andrewma.hoard.data.DataSource;
import com.andrewma.hoard.data.parse.ParseDataSource;
import com.andrewma.hoard.tags.TagType;
import com.andrewma.hoard.tags.TagTypeConverter;
import com.bluelinelabs.logansquare.LoganSquare;
import com.parse.Parse;

public class HoardApplication extends Application{

    private static DataSource sDataSource;

    @Override
    public void onCreate() {
        super.onCreate();

        initParse();
        initLoganSquare();
        sDataSource = new ParseDataSource();
    }

    private void initParse() {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, BuildConfig.PARSE_APPLICATION_ID, BuildConfig.PARSE_CLIENT_KEY);
    }

    private void initLoganSquare() {
        LoganSquare.registerTypeConverter(TagType.class, new TagTypeConverter());
    }

    public static DataSource getDataSource() {
        return sDataSource;
    }
}
