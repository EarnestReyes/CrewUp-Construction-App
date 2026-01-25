package com.example.ConstructionApp;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, Object> config = new HashMap<>();

        config.put("cloud_name", "djkzs1iso");

        MediaManager.init(this, config);
    }
}

