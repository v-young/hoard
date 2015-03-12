package com.andrewma.hoard;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;

public class HoardApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        initParse();
    }

    private void initParse() {
        Parse.enableLocalDatastore(this);
        Log.i("ajma", BuildConfig.PARSE_APPLICATION_ID);
        Parse.initialize(this, BuildConfig.PARSE_APPLICATION_ID, BuildConfig.PARSE_CLIENT_KEY);
    }
}
