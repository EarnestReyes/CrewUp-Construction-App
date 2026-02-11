package clients.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ConstructionApp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import adapters.RecentChatRecyclerAdapter;
import data.FirebaseUtil;
import models.ChatroomModel;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecentChatRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressLoading;
    private TextView txtEmpty;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        recyclerView = view.findViewById(R.id.rvMessages);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressLoading = view.findViewById(R.id.progressLoading);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(null);

        progressLoading.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        setupRecyclerView();

        View root = view.findViewById(R.id.main);
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

        swipeRefresh.setOnRefreshListener(() -> {
            if (adapter != null) {
                adapter.stopListening();
                adapter.startListening();
            }
        });

        return view;
    }

    private void setupRecyclerView() {

        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatroomModel> options =
                new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                        .setQuery(query, ChatroomModel.class)
                        .build();

        adapter = new RecentChatRecyclerAdapter(options, requireContext()) {

            @Override
            public void onDataChanged() {
                progressLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                txtEmpty.setVisibility(
                        getItemCount() == 0 ? View.VISIBLE : View.GONE
                );
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                progressLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                Log.e("CHAT_ERROR", "Firestore error", e);

                // Error ≠ empty list
                txtEmpty.setVisibility(View.GONE);
            }
        };

        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
