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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display filtered list of projects for clients
 * Loads from BookingOrder collection
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
     * 🔥 LOAD FROM BOOKINGORDER COLLECTION (NOT WorkerInput!)
     */
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

        // 🔥 Query BookingOrder where userId = client's UID
        Query query = db.collection("BookingOrder")
                .whereEqualTo("userId", clientId);

        // Apply status filter if not "all"
        if (!"all".equals(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
            Log.d(TAG, "Filtering by status: " + statusFilter);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);
                    projectList.clear();

                    Log.d(TAG, "Found " + querySnapshot.size() + " documents for status: " + statusFilter);

                    for (DocumentSnapshot doc : querySnapshot) {
                        Log.d(TAG, "Document ID: " + doc.getId() + ", status: " + doc.getString("status"));

                        ClientProjectModel project = new ClientProjectModel();

                        // Set IDs
                        project.setProjectId(doc.getId());
                        project.setUserId(doc.getString("userId"));
                        project.setWorkerId(doc.getString("workerId"));

                        // Set project info from BookingOrder
                        project.setWorkDescription(doc.getString("Description"));
                        project.setLocation(doc.getString("Site_Address"));
                        project.setServiceType(doc.getString("Service_Type"));
                        project.setStatus(doc.getString("status"));
                        project.setBudget(doc.getString("Budget"));

                        // Get timestamp
                        Timestamp dateTime = doc.getTimestamp("Date & Time");
                        if (dateTime != null) {
                            project.setCreatedAt(dateTime);
                        }

                        // Load worker info if workerId exists
                        String workerId = project.getWorkerId();
                        if (workerId != null && !workerId.isEmpty()) {
                            loadWorkerInfo(project, workerId);
                        }

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


    private void loadWorkerInfo(ClientProjectModel project, String workerId) {
        db.collection("users")
                .document(workerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Build full name
                        String firstName = doc.getString("FirstName");
                        String middleInitial = doc.getString("MiddleInitial");
                        String lastName = doc.getString("LastName");

                        StringBuilder fullName = new StringBuilder();
                        if (firstName != null) fullName.append(firstName);
                        if (middleInitial != null) {
                            if (fullName.length() > 0) fullName.append(" ");
                            fullName.append(middleInitial);
                            if (!middleInitial.endsWith(".")) fullName.append(".");
                        }
                        if (lastName != null) {
                            if (fullName.length() > 0) fullName.append(" ");
                            fullName.append(lastName);
                        }

                        project.setWorkerName(fullName.toString());
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading worker info", e);
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
        if (statusFilter == null) {
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
        loadProjects();
    }
}