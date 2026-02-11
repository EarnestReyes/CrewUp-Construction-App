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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Fragment to display filtered list of both projects and proposals for workers
 * Loads from both BookingOrder (projects) and WorkerInput (proposals) collections
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

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

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
        loadAllWorkerData();

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

    private void loadAllWorkerData() {
        if (workerId == null) {
            Log.e(TAG, "Worker ID is null");
            Toast.makeText(getContext(), "Please log in to view projects", Toast.LENGTH_SHORT).show();
            showLoading(false);
            updateUI();
            return;
        }

        showLoading(true);
        Log.d(TAG, "Loading all data for worker: " + workerId + ", filter: " + statusFilter);

        projectList.clear();

        // Load both BookingOrder projects and WorkerInput proposals
        loadBookingOrders();
    }

    private void loadBookingOrders() {
        // Build query for BookingOrder (projects where worker is assigned)
        Query query = db.collection("BookingOrder")
                .whereEqualTo("workerId", workerId);

        if (!"all".equals(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Found " + querySnapshot.size() + " BookingOrder documents");

                    for (DocumentSnapshot doc : querySnapshot) {
                        WorkerProjectModel model = new WorkerProjectModel();
                        model.setProposal(false); // This is a BookingOrder project

                        model.setProjectId(doc.getId());
                        model.setUserId(doc.getString("userId"));
                        model.setClientName(doc.getString("Name"));
                        model.setClientPhone(doc.getString("Mobile Number"));
                        model.setClientEmail(doc.getString("Email"));
                        model.setWorkDescription(doc.getString("Description"));
                        model.setLocation(doc.getString("Site_Address"));
                        model.setStatus(doc.getString("status"));
                        model.setWorkerId(doc.getString("workerId"));
                        model.setServiceType(doc.getString("Service_Type"));

                        String budgetStr = doc.getString("Budget");
                        if (budgetStr != null && !budgetStr.isEmpty()) {
                            try {
                                model.setTotalCost(Double.parseDouble(budgetStr));
                            } catch (NumberFormatException e) {
                                model.setTotalCost(0);
                            }
                        }

                        String dateTime = doc.getString("Date & Time");
                        if (dateTime != null) {
                            model.setCreatedAt(dateTime);
                        }

                        projectList.add(model);
                    }

                    // After loading BookingOrders, load WorkerInput proposals
                    loadWorkerInputProposals();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading BookingOrder projects", e);
                    loadWorkerInputProposals(); // Continue to load proposals even if projects fail
                });
    }

    private void loadWorkerInputProposals() {
        // Build query for WorkerInput (proposals created by worker)
        Query query = db.collection("WorkerInput")
                .whereEqualTo("workerId", workerId);

        if (!"all".equals(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Found " + querySnapshot.size() + " WorkerInput proposals");

                    for (DocumentSnapshot doc : querySnapshot) {
                        WorkerProjectModel model = new WorkerProjectModel();
                        model.setProposal(true); // This is a WorkerInput proposal

                        model.setProjectId(doc.getId());
                        model.setUserId(doc.getString("userId"));
                        model.setWorkDescription(doc.getString("workDescription"));
                        model.setLocation(doc.getString("location"));
                        model.setStatus(doc.getString("status"));
                        model.setWorkerId(doc.getString("workerId"));
                        model.setWorkerName(doc.getString("workerName"));

                        // Cost breakdown
                        Double materials = doc.getDouble("materialsCost");
                        Double labor = doc.getDouble("laborCost");
                        Double misc = doc.getDouble("miscCost");
                        Double total = doc.getDouble("totalCost");

                        model.setMaterialsCost(materials);
                        model.setLaborCost(labor);
                        model.setMiscCost(misc);
                        model.setTotalCost(total != null ? total : 0);


                        String createdAtTs = doc.getString("createdAt").toString().trim();;

                        projectList.add(model);
                    }


                    showLoading(false);
                    adapter.updateList(projectList);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading WorkerInput proposals", e);
                    Toast.makeText(getContext(), "Failed to load some data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Still show BookingOrder projects even if proposals fail
                    adapter.updateList(projectList);
                    updateUI();
                });
    }



    private void updateUI() {
        if (projectList.isEmpty()) {
            rvProjects.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);

            String message = getEmptyMessage();
            tvEmptyMessage.setText(message);
            Log.d(TAG, "No items found. Showing empty state: " + message);
        } else {
            rvProjects.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            Log.d(TAG, "Displaying " + projectList.size() + " items (projects + proposals)");
        }
    }

    private String getEmptyMessage() {
        switch (statusFilter) {
            case "pending":
                return "No pending items";
            case "active":
                return "No active projects or proposals";
            case "completed":
                return "No completed items";
            case "cancelled":
                return "No cancelled items";
            default:
                return "No projects or proposals yet.\nStart accepting projects or creating proposals!";
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
        Log.d(TAG, "Fragment resumed, reloading all data");
        loadAllWorkerData(); // Refresh when returning to fragment
    }
}