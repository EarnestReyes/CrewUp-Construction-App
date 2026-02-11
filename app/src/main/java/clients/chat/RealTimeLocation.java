package clients.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ConstructionApp.R;
import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

public class RealTimeLocation extends AppCompatActivity {

    private MapView map;
    private Marker myMarker;
    private GeoPoint myCurrentPoint;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String projectId;
    private ListenerRegistration bookingListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_location);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.map);
        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        projectId = getIntent().getStringExtra("projectId");

        setupLocationCallback();
        requestLocation();

        // 🔥 Only listen if projectId exists
        if (projectId != null) {
            listenToBooking();
        }
    }

    // ================= LOCATION =================

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100
            );
            return;
        }

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000
        ).build();

        locationClient.requestLocationUpdates(
                request,
                locationCallback,
                getMainLooper()
        );
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {

                Location loc = result.getLastLocation();
                if (loc == null) return;

                myCurrentPoint = new GeoPoint(
                        loc.getLatitude(),
                        loc.getLongitude()
                );

                updateMyMarker(myCurrentPoint);
                saveLocationToFirestore(myCurrentPoint);
            }
        };
    }

    private void updateMyMarker(GeoPoint point) {

        if (myMarker == null) {
            myMarker = new Marker(map);
            myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            myMarker.setTitle("You");
            map.getOverlays().add(myMarker);
            map.getController().setZoom(18.0);
        }

        myMarker.setPosition(point);
        map.getController().setCenter(point);
        map.invalidate();
    }

    private void saveLocationToFirestore(GeoPoint point) {
        if (auth.getCurrentUser() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("lat", point.getLatitude());
        data.put("lng", point.getLongitude());
        data.put("locationUpdatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .set(data, SetOptions.merge());
    }

    // ================= FIRESTORE LISTENER =================

    private void listenToBooking() {

        bookingListener = db.collection("BookingOrder")
                .document(projectId)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null || !snapshot.exists())
                        return;

                    String workerId = snapshot.getString("workerId");

                    if (workerId == null) {
                        Toast.makeText(this,
                                "Waiting for worker assignment...",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= LIFECYCLE =================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.removeLocationUpdates(locationCallback);
        map.onDetach();

        if (bookingListener != null) {
            bookingListener.remove();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}
