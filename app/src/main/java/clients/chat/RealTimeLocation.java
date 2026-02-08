package clients.chat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.*;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class RealTimeLocation extends AppCompatActivity {
    private MapView map;
    private Marker myMarker;
    private GeoPoint myCurrentPoint;

    // WORKERS
    private final Map<String, Marker> workerMarkers = new HashMap<>();
    private GeoPoint selectedWorkerPoint;

    // ROUTE
    private Polyline routeLine;

    // LOCATION
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    // FIREBASE
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    String WorkerId;
    String projectId;

    private ListenerRegistration bookingListener;
    private ListenerRegistration workerListener;
    private String currentWorkerId;
    private long lastRouteUpdate = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_location);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        projectId = getIntent().getStringExtra("projectId");

        map = findViewById(R.id.map);
        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

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
                saveLocationToFirestore(myCurrentPoint);
                redrawRoute();
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

    // ================= YOUR MARKER =================

    private void updateMyMarker(GeoPoint point) {
        if (myMarker == null) {
            myMarker = new Marker(map);
            myMarker.setPosition(point);
            myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            myMarker.setTitle("You");

            map.getOverlays().add(myMarker);
            map.getController().setZoom(18.0);
            map.getController().setCenter(point);
        } else {
            myMarker.setPosition(point);
        }
        map.invalidate();
    }

    // ================= FIRESTORE SAVE =================

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

    // ================= WORKERS =================

    private void listenToWorkers() {

        bookingListener = db.collection("BookingOrder")
                .document(projectId)
                .addSnapshotListener((bookingSnapshot, error) -> {

                    if (error != null || bookingSnapshot == null || !bookingSnapshot.exists()) return;

                    String workerId = bookingSnapshot.getString("workerId");

                    if (workerId == null) {
                        removeWorkerMarker(currentWorkerId);
                        detachWorkerListener();
                        currentWorkerId = null;
                        return;
                    }

                    if (workerId.equals(currentWorkerId)) return;

                    detachWorkerListener();
                    currentWorkerId = workerId;

                    workerListener = db.collection("users")
                            .document(workerId)
                            .addSnapshotListener((userSnapshot, userError) -> {

                                if (userError != null || userSnapshot == null || !userSnapshot.exists())
                                    return;

                                Double lat = userSnapshot.getDouble("lat");
                                Double lng = userSnapshot.getDouble("lng");
                                String profileUrl = userSnapshot.getString("profilePicUrl");

                                if (lat == null || lng == null) return;

                                GeoPoint point = new GeoPoint(lat, lng);
                                updateWorkerMarker(workerId, point, profileUrl);
                            });
                });
    }
    private void detachWorkerListener() {
        if (workerListener != null) {
            workerListener.remove();
            workerListener = null;
        }
    }
    private void removeWorkerMarker(String workerId) {
        if (workerId == null) return;

        Marker marker = workerMarkers.remove(workerId);
        if (marker != null) {
            map.getOverlays().remove(marker);
            map.invalidate();
        }

        selectedWorkerPoint = null;

        if (routeLine != null) {
            map.getOverlays().remove(routeLine);
            routeLine = null;
        }
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

            map.getOverlays().add(marker);
            workerMarkers.put(workerId, marker);

            // First worker = auto select
            if (selectedWorkerPoint == null) {
                selectedWorkerPoint = point;
            }

            loadMarkerImage(this, marker, profileUrl);

        } else {
            marker = workerMarkers.get(workerId);
            marker.setPosition(point);
        }

        map.invalidate();
        redrawRoute();
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
                        map.invalidate();
                    }

                    @Override
                    public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {}
                });
    }

    // ================= ROUTE =================

    private void redrawRoute() {
        if (myCurrentPoint == null || selectedWorkerPoint == null) return;

        long now = System.currentTimeMillis();

        // only redraw every 10 seconds
        if (now - lastRouteUpdate < 10_000) return;

        lastRouteUpdate = now;
        drawRouteToWorker(myCurrentPoint, selectedWorkerPoint);
    }


    private void drawRouteToWorker(GeoPoint from, GeoPoint to) {

        String url =
                "https://router.project-osrm.org/route/v1/driving/"
                        + from.getLongitude() + "," + from.getLatitude()
                        + ";" + to.getLongitude() + "," + to.getLatitude()
                        + "?overview=full&geometries=geojson";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {

                if (!response.isSuccessful()) return;

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray coords =
                            json.getJSONArray("routes")
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONArray("coordinates");

                    List<GeoPoint> points = new ArrayList<>();

                    for (int i = 0; i < coords.length(); i++) {
                        JSONArray c = coords.getJSONArray(i);
                        points.add(new GeoPoint(c.getDouble(1), c.getDouble(0)));
                    }

                    runOnUiThread(() -> {
                        if (routeLine != null) {
                            map.getOverlays().remove(routeLine);
                        }

                        routeLine = new Polyline();
                        routeLine.setPoints(points);
                        routeLine.setColor(Color.BLUE);
                        routeLine.setWidth(8f);
                        routeLine.setGeodesic(true);

                        map.getOverlays().add(routeLine);
                        map.invalidate();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ================= LIFECYCLE =================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.removeLocationUpdates(locationCallback);
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
    protected void onStop() {
        super.onStop();

        if (bookingListener != null) {
            bookingListener.remove();
            bookingListener = null;
        }

        if (workerListener != null) {
            workerListener.remove();
            workerListener = null;
        }
    }

}

