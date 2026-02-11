package clients.works;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display filtered list of projects for clients
 */
public class ClientProjectListFragment extends Fragment {

    private static final String TAG = "ClientProjectList";
    private static final String ARG_STATUS = "status";

    private RecyclerView rvProjects;
    private ClientProjectAdapter adapter;
    private List<ClientProjectModel> projectList;

    private TextView tvEmptyMessage;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String clientId;
    private String statusFilter;

    public static ClientProjectListFragment newInstance(String status) {
        ClientProjectListFragment fragment = new ClientProjectListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statusFilter = "all";

        if (getArguments() != null) {
            String argStatus = getArguments().getString(ARG_STATUS);
            if (argStatus != null) {
                statusFilter = argStatus;
            }
        }

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            clientId = currentUser.getUid();
            Log.d(TAG, "Client ID: " + clientId);
        }

        projectList = new ArrayList<>();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_list, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadProjects();

        return view;
    }

    private void initializeViews(View view) {
        rvProjects = view.findViewById(R.id.rvProjects);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new ClientProjectAdapter(projectList);
        rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProjects.setAdapter(adapter);
    }

    private void loadProjects() {
        if (clientId == null) {
            Log.e(TAG, "Client ID is null");
            Toast.makeText(getContext(), "Please log in to view projects", Toast.LENGTH_SHORT).show();
            showLoading(false);
            updateUI();
            return;
        }

        showLoading(true);
        Log.d(TAG, "Loading projects for client: " + clientId + ", filter: " + statusFilter);

        // FIXED: Query using 'userId' instead of 'clientId' to match Firebase structure
        var query = db.collection("WorkerInput")
                .whereEqualTo("userId", clientId);

        // Apply status filter if not "all"
        if (!"all".equals(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showLoading(false);
                    projectList.clear();

                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " documents");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "Document ID: " + document.getId());
                        Log.d(TAG, "Document data: " + document.getData());

                        ClientProjectModel project = document.toObject(ClientProjectModel.class);
                        project.setProjectId(document.getId());
                        projectList.add(project);
                    }

                    adapter.notifyDataSetChanged();
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading projects", e);
                    Toast.makeText(getContext(), "Error loading projects: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void updateUI() {
        if (projectList.isEmpty()) {
            rvProjects.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);

            // Set appropriate empty message based on filter
            String message = getEmptyMessage();
            tvEmptyMessage.setText(message);
            Log.d(TAG, "No projects found. Showing empty state: " + message);
        } else {
            rvProjects.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            Log.d(TAG, "Displaying " + projectList.size() + " projects");
        }
    }

    private String getEmptyMessage() {

        if (statusFilter == null) {
            Log.w(TAG, "statusFilter is null, using default empty message");
            return "No projects yet.\nCreate a booking request to get started!";
        }

        switch (statusFilter) {
            case "pending":
                return "No pending requests";
            case "active":
                return "No active projects";
            case "completed":
                return "No completed projects";
            case "cancelled":
                return "No cancelled projects";
            default:
                return "No projects yet.\nCreate a booking request to get started!";
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvProjects != null) {
            rvProjects.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (layoutEmptyState != null && !show) {
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed, reloading projects");
        loadProjects(); // Refresh when returning to fragment
    }
}