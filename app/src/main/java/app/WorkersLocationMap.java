package app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
    private Marker myMarker;
    private GeoPoint myCurrentPoint;

    private final Map<String, Marker> workerMarkers = new HashMap<>();

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ListenerRegistration workersListener;

    private boolean isPinned = false;

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

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                101
        );

        setupLocationCallback();
        startLocationUpdates();
        listenToWorkers();
        enableLongPressPin();
    }

    // ================= LOCATION =================

    private void setupLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {

                Location loc = result.getLastLocation();
                if (loc == null) return;

                GeoPoint gpsPoint = new GeoPoint(
                        loc.getLatitude(),
                        loc.getLongitude()
                );

                myCurrentPoint = gpsPoint;
                checkIfPinnedThenUpdate(gpsPoint);
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

    // ================= PIN LOGIC =================

    private void enableLongPressPin() {

        map.setOnTouchListener(new View.OnTouchListener() {

            private long pressStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    pressStartTime = System.currentTimeMillis();
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    long duration = System.currentTimeMillis() - pressStartTime;

                    if (duration > 600) {

                        GeoPoint point = (GeoPoint) map.getProjection()
                                .fromPixels((int) event.getX(), (int) event.getY());

                        pinLocation(point);
                        return true;
                    }
                }

                return false;
            }
        });
    }

    private void pinLocation(GeoPoint point) {

        if (auth.getCurrentUser() == null) return;

        isPinned = true;

        Map<String, Object> update = new HashMap<>();
        update.put("lat", point.getLatitude());
        update.put("lng", point.getLongitude());
        update.put("isPinned", true);

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .update(update);

        updateMyMarker(point);
    }

    private void checkIfPinnedThenUpdate(GeoPoint gpsPoint) {

        if (auth.getCurrentUser() == null) return;

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    Boolean pinned = doc.getBoolean("isPinned");

                    if (pinned != null && pinned) {

                        isPinned = true;

                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");

                        if (lat != null && lng != null) {
                            updateMyMarker(new GeoPoint(lat, lng));
                        }

                    } else {

                        isPinned = false;

                        updateMyMarker(gpsPoint);

                        Map<String, Object> update = new HashMap<>();
                        update.put("lat", gpsPoint.getLatitude());
                        update.put("lng", gpsPoint.getLongitude());
                        update.put("isPinned", false);

                        db.collection("users")
                                .document(auth.getCurrentUser().getUid())
                                .update(update);
                    }
                });
    }

    public void unpinLocation() {

        if (auth.getCurrentUser() == null) return;

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .update("isPinned", false);

        isPinned = false;
    }

    // ================= CLIENT MARKER =================

    private void updateMyMarker(GeoPoint point) {

        if (myMarker == null) {
            myMarker = new Marker(map);
            myMarker.setPosition(point);
            myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            myMarker.setTitle(isPinned ? "Pinned Location" : "You");

            map.getOverlays().add(myMarker);
            map.getController().setZoom(16.5);
            map.getController().setCenter(point);

        } else {
            myMarker.setPosition(point);
            myMarker.setTitle(isPinned ? "Pinned Location" : "You");
        }

        map.invalidate();
    }

    // ================= WORKERS =================

    private void listenToWorkers() {

        workersListener = db.collection("users")
                .whereEqualTo("Role", "worker")
                .whereEqualTo("shareLocation", true)
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null || snapshots == null) return;

                    for (DocumentSnapshot doc : snapshots) {

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

    private void loadMarkerImage(
            Context context,
            Marker marker,
            String imageUrl
    ) {

        Object imageSource = (imageUrl == null || imageUrl.isEmpty())
                ? R.drawable.ic_profile_marker
                : imageUrl;

        Glide.with(context)
                .asBitmap()
                .load(imageSource)
                .circleCrop()
                .into(new CustomTarget<Bitmap>(96, 96) {

                    @Override
                    public void onResourceReady(
                            @NonNull Bitmap bitmap,
                            Transition<? super Bitmap> transition
                    ) {
                        marker.setIcon(
                                new BitmapDrawable(context.getResources(), bitmap)
                        );
                        map.invalidate();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
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
