package clients.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import models.ChatroomModel;
import data.FirebaseUtil;
import com.example.ConstructionApp.R;
import adapters.RecentChatRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecentChatRecyclerAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressLoading;
    private TextView txtEmpty;

    private boolean firstLoad = true;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressLoading = view.findViewById(R.id.progressLoading);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        recyclerView = view.findViewById(R.id.rvMessages);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(null);

        progressLoading.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        setupRecyclerView();

        swipeRefresh.setColorSchemeResources(
                R.color.primary,
                R.color.icon_share,
                R.color.icon_comment
        );

        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.postDelayed(
                    () -> swipeRefresh.setRefreshing(false),
                    600
            );
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
                if (firstLoad) {
                    progressLoading.setVisibility(View.GONE);
                    firstLoad = false;
                }

                swipeRefresh.setRefreshing(false);

                if (getItemCount() == 0) {
                    txtEmpty.setVisibility(View.VISIBLE);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                }
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

