package workers.works;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProjectDetailsFragment extends Fragment {

    TextView tvName, tvEmail, tvMobile, tvHome,
            tvSite, tvBudget, tvDesc;

    Button accept, cancel, complete;
    ImageView imgPhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_project_details, container, false);

        tvName = v.findViewById(R.id.tvName);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvMobile = v.findViewById(R.id.tvMobile);
        tvHome = v.findViewById(R.id.tvHomeAddress);
        tvSite = v.findViewById(R.id.tvSiteAddress);
        tvBudget = v.findViewById(R.id.tvBudget);
        tvDesc = v.findViewById(R.id.tvDescription);

        accept = v.findViewById(R.id.btnAccept);
        cancel = v.findViewById(R.id.btnCancel);
        complete = v.findViewById(R.id.btnComplete);
        imgPhoto = v.findViewById(R.id.imgPhoto);


        Bundle args = getArguments();
        if (args == null) return v;

        String projectId = args.getString("projectId");
        if (projectId == null) return v;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref =
                db.collection("BookingOrder").document(projectId);

        ref.get().addOnSuccessListener(doc -> {
            if (!isAdded() || !doc.exists()) return;

            tvName.setText(doc.getString("Name"));
            tvEmail.setText(doc.getString("Email"));
            tvMobile.setText(doc.getString("Mobile Number"));
            tvHome.setText(doc.getString("Home_Address"));
            tvSite.setText(doc.getString("Site_Address"));
            tvBudget.setText(doc.getString("Budget"));
            tvDesc.setText(doc.getString("Description"));

            List<String> photos = (List<String>) doc.get("photos");

            if (photos != null && !photos.isEmpty()) {
                Glide.with(requireContext())
                        .load(photos.get(0))
                        .placeholder(R.drawable.bg_bottom_nav)
                        .error(R.drawable.ic_close)
                        .into(imgPhoto);
            } else {
                imgPhoto.setVisibility(View.GONE);
            }
        });

        accept.setOnClickListener(v1 ->
                ref.update("status", "active"));

        cancel.setOnClickListener(v1 ->
                ref.update("status", "cancelled"));

        complete.setOnClickListener(v1 ->
                ref.update("status", "completed"));

        return v;
    }
}


