package app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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

import clients.profile.UserProfile;

public class WorkersLocationMap extends AppCompatActivity {

    private MapView map;
    // CLIENT
    private Marker myMarker;
    private GeoPoint myCurrentPoint;
    // WORKERS
    private final Map<String, Marker> workerMarkers = new HashMap<>();
    // LOCATION
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    // FIREBASE
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    // 🔐 LISTENERS
    private ListenerRegistration workersListener;
    private ListenerRegistration selfListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_location);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.setTileSource(
                org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK
        );

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                101
        );

        setupLocationCallback();
        startLocationUpdates();
        listenToWorkers();
    }

    // ================= LOCATION =================

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
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) return;

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000
        )
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationClient.requestLocationUpdates(
                request,
                locationCallback,
                getMainLooper()
        );
    }

    // ================= CLIENT MARKER =================

    private void updateMyMarker(GeoPoint point) {
        if (myMarker == null) {
            myMarker = new Marker(map);
            myMarker.setPosition(point);
            myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            myMarker.setTitle("You");

            map.getOverlays().add(myMarker);
            map.getController().setZoom(16.5);
            map.getController().setCenter(point);
        } else {
            myMarker.setPosition(point);
        }
        map.invalidate();
    }

    // ================= WORKERS (PRIVACY ENFORCED) =================

    private void listenToWorkers() {

        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        // 🔥 LISTEN TO CURRENT USER shareLocation
        selfListener = db.collection("users")
                .document(uid)
                .addSnapshotListener((userDoc, error) -> {

                    if (error != null || userDoc == null || !userDoc.exists()) return;

                    Boolean shareLocation = userDoc.getBoolean("shareLocation");

                    if (shareLocation == null || !shareLocation) {
                        // 🚫 USER DISABLED SHARING
                        removeAllWorkerMarkers();
                        stopWorkersListener();
                    } else {
                        // ✅ USER ENABLED SHARING
                        startWorkersListener();
                    }
                });
    }

    private void startWorkersListener() {

        if (workersListener != null) return;

        workersListener = db.collection("users")
                .whereEqualTo("Role", "worker")
                .whereEqualTo("shareLocation", true) // 🔐 ONLY SHARING WORKERS
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null || snapshots == null) return;

                    for (DocumentSnapshot doc : snapshots) {

                        // ❌ Skip self
                        if (auth.getCurrentUser() != null &&
                                doc.getId().equals(auth.getCurrentUser().getUid()))
                            continue;

                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String profileUrl = doc.getString("profilePicUrl");

                        if (lat == null || lng == null) continue;

                        GeoPoint point = new GeoPoint(lat, lng);
                        updateWorkerMarker(doc.getId(), point, profileUrl);
                    }
                });
    }

    private void stopWorkersListener() {
        if (workersListener != null) {
            workersListener.remove();
            workersListener = null;
        }
    }

    private void removeAllWorkerMarkers() {
        for (Marker marker : workerMarkers.values()) {
            map.getOverlays().remove(marker);
        }
        workerMarkers.clear();
        map.invalidate();
    }

    private void updateWorkerMarker(
            String workerId,
            GeoPoint point,
            String profileUrl
    ) {

        Marker marker;

        if (!workerMarkers.containsKey(workerId)) {

            marker = new Marker(map);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle("Worker");

            marker.setRelatedObject(workerId);

            marker.setOnMarkerClickListener((m, mapView) -> {
                openWorkerProfile((String) m.getRelatedObject());
                return true;
            });

            map.getOverlays().add(marker);
            workerMarkers.put(workerId, marker);

            loadMarkerImage(this, marker, profileUrl);

        } else {
            marker = workerMarkers.get(workerId);
            marker.setPosition(point);
        }

        map.invalidate();
    }

    // ================= PROFILE PIC MARKER =================

    private void loadMarkerImage(
            Context context,
            Marker marker,
            String imageUrl
    ) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .circleCrop()
                .into(new CustomTarget<Bitmap>(96, 96) {
                    @Override
                    public void onResourceReady(
                            @NonNull Bitmap bitmap,
                            Transition<? super Bitmap> transition
                    ) {
                        marker.setIcon(new BitmapDrawable(getResources(), bitmap));
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        map.invalidate();
                    }

                    @Override
                    public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {}
                });
    }

    private void openWorkerProfile(String workerId) {
        Intent intent = new Intent(this, UserProfile.class);
        intent.putExtra("userId", workerId);
        startActivity(intent);
    }

    // ================= LIFECYCLE =================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }

        if (workersListener != null) {
            workersListener.remove();
            workersListener = null;
        }

        if (selfListener != null) {
            selfListener.remove();
            selfListener = null;
        }

        map.onDetach();
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

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startLocationUpdates();
        }
    }
}
