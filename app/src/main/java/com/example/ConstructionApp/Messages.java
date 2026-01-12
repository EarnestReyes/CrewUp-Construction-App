package com.example.ConstructionApp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class Messages extends Fragment {

    RecyclerView rvMessages;
    ArrayList<MessageUser> list;

    public Messages() {
        // Required empty public constructor
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
}
