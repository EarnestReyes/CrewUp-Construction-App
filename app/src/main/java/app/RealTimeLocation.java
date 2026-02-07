package app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ConstructionApp.R;
import com.google.android.gms.location.*;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class RealTimeLocation extends AppCompatActivity {

    private MapView map;
    private Marker myMarker;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_location);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                101
        );

        setupLocationCallback();
        startLocationUpdates();
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc == null) return;

                updateMarker(loc.getLatitude(), loc.getLongitude());
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) return;

        LocationRequest request = LocationRequest.create()
                .setInterval(3000)
                .setFastestInterval(2000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationClient.requestLocationUpdates(
                request,
                locationCallback,
                getMainLooper()
        );
    }

    private void updateMarker(double lat, double lng) {
        GeoPoint point = new GeoPoint(lat, lng);

        if (myMarker == null) {
            myMarker = new Marker(map);
            myMarker.setPosition(point);
            myMarker.setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
            );
            myMarker.setTitle("You");
            map.getOverlays().add(myMarker);

            map.getController().setZoom(18.0);
            map.getController().setCenter(point);
        } else {
            myMarker.setPosition(point);
            map.invalidate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDetach();
    }
}

