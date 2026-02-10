package app;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

public class MyAppOneSignal extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("APP_INIT", " MyAppOneSignal LOADED !!");

        // ✅ Explicit App ID (fixes v5 warning)
        OneSignal.initWithContext(
                this,
                "b1a8faac-dde0-4e43-b1f1-8dbd1cd3d340"
        );

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "djkzs1iso");
        MediaManager.init(this, config);
    }
}
