package com.example.ConstructionApp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Messages extends Fragment {

    RecyclerView rvMessages;
    ArrayList<MessageUser> list;

    private FirebaseFirestore db;

    private String userLocation = "";

    private TextView txtLocation;

    public Messages() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null)  {

        }
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // INIT RECYCLERVIEW
        rvMessages = view.findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        txtLocation = view.findViewById(R.id.txtLocation);
        getUserLocationFromDatabase();

        // DATA
        list = new ArrayList<>();
        list.add(new MessageUser("Alex Reyes", "Let’s meet tomorrow", "1:20 PM"));
        list.add(new MessageUser("Maria Cruz", "Thanks!", "Yesterday"));
        list.add(new MessageUser("Construction Team", "New update posted", "Mon"));

        // ADAPTER
        MessageAdapter adapter = new MessageAdapter(requireContext(), list);
        rvMessages.setAdapter(adapter);

        return view;
    }

    private void getUserLocationFromDatabase() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    if (!isAdded() || document == null || !document.exists()) return;

                    userLocation = document.getString("location");

                    if (userLocation != null && !userLocation.isEmpty()) {
                        txtLocation.setText(userLocation);
                    } else {
                        txtLocation.setText("Location not specified");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FIRESTORE", "Failed to get location", e));
    }
}
