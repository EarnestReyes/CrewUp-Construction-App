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

        try {
            // ✅ OneSignal MUST be first
            OneSignal.initWithContext(getApplicationContext());
            Log.d("APP_INIT", "OneSignal initialized");

        } catch (Exception e) {
            Log.e("APP_INIT", "OneSignal init failed", e);
        }

        // ✅ Cloudinary (safe init)
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "djkzs1iso");
            MediaManager.init(this, config);
            Log.d("APP_INIT", "Cloudinary initialized");

        } catch (IllegalStateException e) {
            Log.w("APP_INIT", "Cloudinary already initialized");
        }
    }
}
