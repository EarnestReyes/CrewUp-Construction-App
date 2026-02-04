package workers.works;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ConstructionApp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProjectListFragment extends Fragment {

    // 🔹 ARGUMENT KEY
    private static final String ARG_STATUS = "status";

    private String status;

    private RecyclerView recyclerView;
    private ProjectAdapter adapter;
    private List<ProjectModel> list;

    // 🔹 REQUIRED EMPTY CONSTRUCTOR
    public ProjectListFragment() {}

    // 🔹 CORRECT WAY TO CREATE FRAGMENT
    public static ProjectListFragment newInstance(String status) {
        ProjectListFragment fragment = new ProjectListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View v = inflater.inflate(R.layout.fragment_project_list, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new ProjectAdapter(list, this);
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query;

        if ("all".equals(status)) {
            query = db.collection("BookingOrder")
                    .whereNotIn("status",
                            Arrays.asList("pending", "cancelled"));
        } else {
            query = db.collection("BookingOrder")
                    .whereEqualTo("status", status);
        }

        query.addSnapshotListener((value, error) -> {

            if (error != null || value == null) {
                return;
            }

            list.clear();

            for (DocumentSnapshot doc : value.getDocuments()) {
                ProjectModel project = doc.toObject(ProjectModel.class);
                if (project == null) continue;

                project.id = doc.getId();
                list.add(project);
            }
            adapter.notifyDataSetChanged();
        });

        return v;
    }
}
