package clients.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import org.osmdroid.views.overlay.*;

import java.io.IOException;
import java.util.*;

import clients.profile.UserProfile;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class RealTimeLocation extends AppCompatActivity {

    private MapView map;
    private Marker myMarker;
    private Marker otherMarker;
    private Polyline routeLine;

    private GeoPoint myPoint;
    private GeoPoint otherPoint;

    private TextView tvDistance;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String bookingUserId;
    private String bookingWorkerId;
    private String currentUserId;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_location);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.map);
        tvDistance = findViewById(R.id.tvDistance);

        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                101
        );

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        setupLocationCallback();
        startLocationUpdates();
        loadBooking();
    }

    // ================= LOAD BOOKING =================

    private void loadBooking() {

        // Check if current user is CLIENT
        db.collection("BookingOrder")
                .whereEqualTo("userId", currentUserId)
                .whereIn("status", Arrays.asList("pending", "active"))
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.isEmpty()) {

                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        bookingUserId = doc.getString("userId");
                        bookingWorkerId = doc.getString("workerId");

                        listenToUsers();
                        return;
                    }

                    // If not client, check if WORKER
                    db.collection("BookingOrder")
                            .whereEqualTo("workerId", currentUserId)
                            .whereIn("status", Arrays.asList("pending", "active"))
                            .limit(1)
                            .get()
                            .addOnSuccessListener(workerSnap -> {

                                if (workerSnap.isEmpty()) {
                                    Toast.makeText(this,
                                            "No active booking found",
                                            Toast.LENGTH_LONG).show();
                                    finish();
                                    return;
                                }

                                DocumentSnapshot doc = workerSnap.getDocuments().get(0);
                                bookingUserId = doc.getString("userId");
                                bookingWorkerId = doc.getString("workerId");

                                listenToUsers();
                            });
                });
    }

    // ================= LISTEN TO BOTH USERS =================

    private void listenToUsers() {

        listenToUserLocation(bookingUserId);
        listenToUserLocation(bookingWorkerId);
    }

    private void listenToUserLocation(String userId) {

        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {

                    if (snapshot == null || !snapshot.exists()) return;

                    Double lat = snapshot.getDouble("lat");
                    Double lng = snapshot.getDouble("lng");
                    String profileUrl = snapshot.getString("profilePicUrl");

                    if (lat == null || lng == null) return;

                    GeoPoint point = new GeoPoint(lat, lng);

                    if (userId.equals(currentUserId)) {
                        myPoint = point;
                        updateMyMarker(point);
                    } else {
                        otherPoint = point;
                        updateOtherMarker(userId, point, profileUrl);
                    }

                    redrawRoute();
                });
    }

    // ================= SAVE LOCATION =================

    private void saveLocationToFirestore(GeoPoint point) {

        Map<String, Object> data = new HashMap<>();
        data.put("lat", point.getLatitude());
        data.put("lng", point.getLongitude());
        data.put("locationUpdatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(currentUserId)
                .set(data, SetOptions.merge());
    }

    // ================= LOCATION CALLBACK =================

    private void setupLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {

                Location loc = result.getLastLocation();
                if (loc == null) return;

                myPoint = new GeoPoint(
                        loc.getLatitude(),
                        loc.getLongitude()
                );

                updateMyMarker(myPoint);

                // 🔥 SAVE YOUR LIVE LOCATION
                saveLocationToFirestore(myPoint);

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
                Priority.PRIORITY_HIGH_ACCURACY, 4000
        ).build();

        locationClient.requestLocationUpdates(
                request,
                locationCallback,
                getMainLooper()
        );
    }

    // ================= MARKERS =================

    private void updateMyMarker(GeoPoint point) {

        if (myMarker == null) {
            myMarker = new Marker(map);
            myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            myMarker.setTitle("You");
            map.getOverlays().add(myMarker);
        }

        myMarker.setPosition(point);
        map.getController().setZoom(17.0);
        map.getController().setCenter(point);
        map.invalidate();
    }

    private void updateOtherMarker(String userId,
                                   GeoPoint point,
                                   String profileUrl) {

        if (otherMarker == null) {
            otherMarker = new Marker(map);
            otherMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(otherMarker);
        }

        otherMarker.setPosition(point);

        if (profileUrl != null && !profileUrl.isEmpty()) {

            Glide.with(this)
                    .asBitmap()
                    .load(profileUrl)
                    .circleCrop()
                    .into(new CustomTarget<Bitmap>(100, 100) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap,
                                                    Transition<? super Bitmap> transition) {

                            otherMarker.setIcon(
                                    new BitmapDrawable(getResources(), bitmap));

                            otherMarker.setOnMarkerClickListener((m, mapView) -> {
                                Intent intent = new Intent(
                                        RealTimeLocation.this,
                                        UserProfile.class
                                );
                                intent.putExtra("userId", userId);
                                startActivity(intent);
                                return true;
                            });

                            map.invalidate();
                        }

                        @Override
                        public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {}
                    });
        }
    }

    // ================= ROUTE =================

    private void redrawRoute() {

        if (myPoint == null || otherPoint == null) {
            tvDistance.setText("");
            return;
        }

        String url =
                "https://router.project-osrm.org/route/v1/driving/"
                        + myPoint.getLongitude() + "," + myPoint.getLatitude()
                        + ";"
                        + otherPoint.getLongitude() + "," + otherPoint.getLatitude()
                        + "?overview=full&geometries=geojson";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> tvDistance.setText("Route unavailable"));
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {

                if (!response.isSuccessful()) return;

                try {

                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject route =
                            json.getJSONArray("routes").getJSONObject(0);

                    double km =
                            route.getDouble("distance") / 1000.0;

                    JSONArray coords =
                            route.getJSONObject("geometry")
                                    .getJSONArray("coordinates");

                    List<GeoPoint> points = new ArrayList<>();

                    for (int i = 0; i < coords.length(); i++) {
                        JSONArray c = coords.getJSONArray(i);
                        points.add(new GeoPoint(c.getDouble(1), c.getDouble(0)));
                    }

                    runOnUiThread(() -> {

                        if (routeLine != null)
                            map.getOverlays().remove(routeLine);

                        routeLine = new Polyline();
                        routeLine.setPoints(points);
                        routeLine.setColor(Color.rgb(255, 183, 77));
                        routeLine.setWidth(10f);

                        map.getOverlays().add(routeLine);
                        map.invalidate();

                        tvDistance.setText(
                                String.format(Locale.getDefault(),
                                        "%.2f km away", km)
                        );
                    });

                } catch (Exception ignored) {}
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.removeLocationUpdates(locationCallback);
        map.onDetach();
    }
}
