package clients.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import adapters.SearchUserRecyclerAdapter;
import data.FirebaseUtil;
import models.UserModel;

public class SearchUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchUserRecyclerAdapter adapter;
    private EditText searchInput;
    private ImageButton backBtn;
    private TextView clearAllBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        recyclerView = findViewById(R.id.search_user_recycler_view);
        searchInput = findViewById(R.id.seach_username_input);
        backBtn = findViewById(R.id.back_btn);
        clearAllBtn = findViewById(R.id.clear_all);

        View root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            if (isFinishing() || isDestroyed()) {
                return insets;
            }

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);

        loadRecentSearches();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim().toLowerCase();
                if (text.isEmpty()) {
                    loadRecentSearches();
                } else {
                    loadSearchResults(text);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        backBtn.setOnClickListener(v -> finish());

        clearAllBtn.setOnClickListener(v ->
                FirebaseUtil.clearAllRecentSearches()
        );
    }

    private void loadRecentSearches() {
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseUtil.currentUserId())
                .collection("recent_searches")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        setAdapter(query, true);
    }

    private void loadSearchResults(String text) {
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .orderBy("username_lower")
                .startAt(text)
                .endAt(text + "\uf8ff");

        setAdapter(query, false);
    }

    private void setAdapter(Query query, boolean isRecent) {

        FirestoreRecyclerOptions<UserModel> options =
                new FirestoreRecyclerOptions.Builder<UserModel>()
                        .setQuery(query, snapshot -> {
                            UserModel user = new UserModel();
                            user.setUserId(snapshot.getId());
                            user.setUsername(snapshot.getString("username"));
                            user.setEmail(snapshot.getString("email"));
                            user.setLocation(snapshot.getString("location"));
                            return user;
                        })
                        .build();

        if (adapter != null) {
            adapter.stopListening();
            recyclerView.setAdapter(null);
        }

        adapter = new SearchUserRecyclerAdapter(options, this, isRecent);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            recyclerView.post(adapter::startListening);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}










