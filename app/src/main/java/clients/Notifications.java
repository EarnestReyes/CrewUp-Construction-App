package clients;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import adapters.NotificationAdapter;
import models.NotificationModel;

public class Notifications extends AppCompatActivity {

    private ImageButton backBtn;

    private RecyclerView rvRecent, rvEarlier, rvFewDays;
    private View emptyState, scrollContent;

    private NotificationAdapter recentAdapter, earlierAdapter, fewDaysAdapter;

    private final ArrayList<NotificationModel> recentList = new ArrayList<>();
    private final ArrayList<NotificationModel> earlierList = new ArrayList<>();
    private final ArrayList<NotificationModel> fewDaysList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());

        rvRecent = findViewById(R.id.rvRecent);
        rvEarlier = findViewById(R.id.rvEarlier);
        rvFewDays = findViewById(R.id.rvFewDays);

        emptyState = findViewById(R.id.empty_state);
        scrollContent = findViewById(R.id.scroll_content);

        setupRecycler(rvRecent);
        setupRecycler(rvEarlier);
        setupRecycler(rvFewDays);

        recentAdapter = new NotificationAdapter(this, recentList);
        earlierAdapter = new NotificationAdapter(this, earlierList);
        fewDaysAdapter = new NotificationAdapter(this, fewDaysList);

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

        rvRecent.setAdapter(recentAdapter);
        rvEarlier.setAdapter(earlierAdapter);
        rvFewDays.setAdapter(fewDaysAdapter);

        loadNotifications();
    }

    private void setupRecycler(RecyclerView rv) {
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);
    }

    private void loadNotifications() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("toUserId", uid) // 🔥 FIX
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    recentList.clear();
                    earlierList.clear();
                    fewDaysList.clear();

                    long now = System.currentTimeMillis();

                    for (var doc : value.getDocuments()) {

                        NotificationModel model =
                                doc.toObject(NotificationModel.class);

                        if (model == null || model.getTimestamp() == null) continue;

                        model.setId(doc.getId());

                        long diff =
                                now - model.getTimestamp().toDate().getTime();

                        if (diff <= 24 * 60 * 60 * 1000) {
                            recentList.add(model);
                        } else if (diff <= 3 * 24 * 60 * 60 * 1000) {
                            earlierList.add(model);
                        } else {
                            fewDaysList.add(model);
                        }
                    }

                    recentAdapter.notifyDataSetChanged();
                    earlierAdapter.notifyDataSetChanged();
                    fewDaysAdapter.notifyDataSetChanged();

                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        boolean isEmpty =
                recentList.isEmpty()
                        && earlierList.isEmpty()
                        && fewDaysList.isEmpty();

        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        scrollContent.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}


