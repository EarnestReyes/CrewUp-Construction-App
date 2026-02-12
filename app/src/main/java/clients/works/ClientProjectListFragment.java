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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads proposals from WorkerInput collection
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

    /**
     * Load proposals from WorkerInput collection
     */
    private void loadProjects() {
        if (clientId == null) {
            Log.e(TAG, "Client ID is null");
            Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
            showLoading(false);
            updateUI();
            return;
        }

        showLoading(true);
        Log.d(TAG, "Loading from WorkerInput for client: " + clientId + ", status: " + statusFilter);

        // Query WorkerInput where userId = clientId
        Query query = db.collection("WorkerInput")
                .whereEqualTo("userId", clientId);

        // Filter by status
        if (!"all".equals(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);
                    projectList.clear();

                    Log.d(TAG, "Found " + querySnapshot.size() + " proposals");

                    for (QueryDocumentSnapshot doc : querySnapshot) {

                        ClientProjectModel project = doc.toObject(ClientProjectModel.class);
                        project.setProjectId(doc.getId());

                        projectList.add(project);

                        Log.d(TAG, "Loaded: " + project.getWorkerName() + ", status: " + project.getStatus());
                    }

                    adapter.notifyDataSetChanged();
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading proposals", e);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void updateUI() {
        if (projectList.isEmpty()) {
            rvProjects.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(getEmptyMessage());
        } else {
            rvProjects.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private String getEmptyMessage() {
        switch (statusFilter) {
            case "pending":
                return "No pending proposals";
            case "active":
                return "No active projects";
            case "completed":
                return "No completed projects";
            case "cancelled":
                return "No cancelled projects";
            default:
                return "No proposals yet";
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
        loadProjects();
    }
}