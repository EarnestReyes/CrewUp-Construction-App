package app;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

public class MyAppOneSignal extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // -------- OneSignal --------
        OneSignal.initWithContext(this);

        // -------- Cloudinary (SAFE: unsigned upload) --------
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "djkzs1iso"); // from Cloudinary dashboard

        MediaManager.init(this, config);
    }
}
