package app;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyAppCloud extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "djkzs1iso");
        config.put("api_key", "855327696723219");
        config.put("api_secret", "CCThf1FvgclVr_8Aedob6AzWo7w");

        MediaManager.init(this, config);
    }
}
