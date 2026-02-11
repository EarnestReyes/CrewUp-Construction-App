package workers.works;

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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display filtered list of projects for workers
 */
public class WorkerProjectListFragment extends Fragment {

    private static final String TAG = "WorkerProjectList";
    private static final String ARG_STATUS = "status";

    private RecyclerView rvProjects;
    private WorkerProjectAdapter adapter;
    private List<WorkerProjectModel> projectList;

    private TextView tvEmptyMessage;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String workerId;
    private String statusFilter;

    public static WorkerProjectListFragment newInstance(String status) {
        WorkerProjectListFragment fragment = new WorkerProjectListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusFilter = getArguments().getString(ARG_STATUS, "all");
        }

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            workerId = currentUser.getUid();
            Log.d(TAG, "Worker ID: " + workerId);
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
        loadWorkerProjects();

        return view;
    }

    private void initializeViews(View view) {
        rvProjects = view.findViewById(R.id.rvProjects);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new WorkerProjectAdapter(projectList);
        rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProjects.setAdapter(adapter);
    }

    private void loadWorkerProjects() {
        if (workerId == null) {
            Log.e(TAG, "Worker ID is null");
            Toast.makeText(getContext(), "Please log in to view projects", Toast.LENGTH_SHORT).show();
            showLoading(false);
            updateUI();
            return;
        }

        showLoading(true);
        Log.d(TAG, "Loading projects for worker: " + workerId + ", filter: " + statusFilter);

        // Build query based on status filter
        Query query = db.collection("BookingOrder")
                .whereEqualTo("workerId", workerId);

        // IMPORTANT: Apply status filter if not "all"
        if (!"all".equals(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
            Log.d(TAG, "Filtering by status: " + statusFilter);
        } else {
            Log.d(TAG, "Showing all statuses");
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);
                    projectList.clear();

                    Log.d(TAG, "Found " + querySnapshot.size() + " documents for status: " + statusFilter);

                    for (DocumentSnapshot doc : querySnapshot) {
                        Log.d(TAG, "Document: " + doc.getId() + ", status: " + doc.getString("status"));

                        WorkerProjectModel model = new WorkerProjectModel();

                        model.setProjectId(doc.getId());
                        model.setClientId(doc.getString("userId"));
                        model.setClientName(doc.getString("Name"));
                        model.setClientPhone(doc.getString("Mobile Number"));
                        model.setClientEmail(doc.getString("Email"));
                        model.setWorkDescription(doc.getString("Description"));
                        model.setLocation(doc.getString("Site_Address"));
                        model.setStatus(doc.getString("status"));
                        model.setWorkerId(doc.getString("workerId"));
                        model.setServiceType(doc.getString("Service_Type"));

                        // Parse budget
                        String budgetStr = doc.getString("Budget");
                        if (budgetStr != null && !budgetStr.isEmpty()) {
                            try {
                                model.setTotalCost(Double.parseDouble(budgetStr));
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing budget: " + budgetStr, e);
                                model.setTotalCost(0);
                            }
                        }

                        // Get timestamp
                        String dateTime = doc.getString("Date & Time");
                        if (dateTime != null) {
                            model.setCreatedAt(dateTime);
                        }

                        projectList.add(model);
                    }

                    adapter.updateList(projectList);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading projects", e);
                    Toast.makeText(getContext(), "Failed to load projects: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void updateUI() {
        if (projectList.isEmpty()) {
            rvProjects.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);

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
        switch (statusFilter) {
            case "pending":
                return "No pending projects";
            case "active":
                return "No active projects";
            case "completed":
                return "No completed projects";
            case "cancelled":
                return "No cancelled projects";
            default:
                return "No projects yet.\nStart accepting projects to see them here!";
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
        loadWorkerProjects(); // Refresh when returning to fragment
    }
}