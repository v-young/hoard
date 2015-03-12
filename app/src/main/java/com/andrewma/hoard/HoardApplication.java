package com.andrewma.hoard;

import android.app.Application;

import com.andrewma.hoard.data.DataSource;
import com.andrewma.hoard.data.parse.ParseDataSource;
import com.parse.Parse;

public class HoardApplication extends Application{

    private static DataSource sDataSource;

    @Override
    public void onCreate() {
        super.onCreate();

        initParse();
        sDataSource = new ParseDataSource();
    }

    private void initParse() {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, BuildConfig.PARSE_APPLICATION_ID, BuildConfig.PARSE_CLIENT_KEY);
    }

    public static DataSource getDataSource() {
        return sDataSource;
    }
}
