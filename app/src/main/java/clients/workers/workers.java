package clients.workers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ConstructionApp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import adapters.WorkersRecyclerAdapter;
import models.WorkerModel;

public class workers extends Fragment {

    private RecyclerView recyclerView;
    private WorkersRecyclerAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressLoading;
    private TextView txtEmpty;

    private boolean firstLoad = true; // 🔥 IMPORTANT

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_workers, container, false);

        recyclerView = view.findViewById(R.id.rvWorkers);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressLoading = view.findViewById(R.id.progressLoading);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );

        // 🔥 Initial state
        progressLoading.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("Role", "worker");

        FirestoreRecyclerOptions<WorkerModel> options =
                new FirestoreRecyclerOptions.Builder<WorkerModel>()
                        .setQuery(query, WorkerModel.class)
                        .build();

        adapter = new WorkersRecyclerAdapter(options, requireContext()) {

            @Override
            public void onDataChanged() {
                // Called automatically when Firestore finishes loading

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

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
