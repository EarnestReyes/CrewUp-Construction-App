package clients.workers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import adapters.WorkersRecyclerAdapter;
import models.WorkerModel;

public class workers extends Fragment {

    private RecyclerView recyclerView;
    private WorkersRecyclerAdapter adapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_workers, container, false);

        recyclerView = view.findViewById(R.id.rvWorkers);
        recyclerView.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );

        Query query = FirebaseFirestore.getInstance()
                .collection("workers");

        FirestoreRecyclerOptions<WorkerModel> options =
                new FirestoreRecyclerOptions.Builder<WorkerModel>()
                        .setQuery(query, WorkerModel.class)
                        .build();

        adapter = new WorkersRecyclerAdapter(options, requireContext());
        recyclerView.setAdapter(adapter);

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
