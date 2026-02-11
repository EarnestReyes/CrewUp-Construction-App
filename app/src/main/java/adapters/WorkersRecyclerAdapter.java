package adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import clients.profile.UserProfile;
import data.AndroidUtil;
import models.UserModel;
import models.WorkerModel;

public class WorkersRecyclerAdapter
        extends FirestoreRecyclerAdapter<WorkerModel, WorkersRecyclerAdapter.WorkerViewHolder> {

    private final Context context;

    public WorkersRecyclerAdapter(
            @NonNull FirestoreRecyclerOptions<WorkerModel> options,
            Context context
    ) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(
            @NonNull WorkerViewHolder holder,
            int position,
            @NonNull WorkerModel model
    ) {
        holder.txtName.setText(model.getUsername());
        holder.txtRole.setText(model.getRole());

        String rawLocation = getSnapshots()
                .getSnapshot(position)
                .getString("location");

        holder.txtDistance.setText("📍 " + rawLocation);

        Log.e("MODEL_DEBUG", "Model class = " + model.getClass().getName());
        Log.e("LOCATION_DEBUG", "rawLocation = " + rawLocation);

        if (model.getProfilePicUrl() != null) {
            Glide.with(context)
                    .load(model.getProfilePicUrl())
                    .circleCrop()
                    .into(holder.imgProfile);
        }

        holder.worker_field.setOnClickListener(v -> {

            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return; // safety check

            Toast.makeText(context, "User " + model.getUsername() + " is clicked!", Toast.LENGTH_SHORT).show();

            String userId = getSnapshots()
                    .getSnapshot(pos)
                    .getId();

            Intent intent = new Intent(context, UserProfile.class);
            AndroidUtil.passUserModelAsIntent(intent, new UserModel(), userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }


    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_worker, parent, false);
        return new WorkerViewHolder(view);
    }

    static class WorkerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView txtName, txtRole, txtRating, txtDistance;
        LinearLayout worker_field;

        WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.txtName);
            txtRole = itemView.findViewById(R.id.txtRole);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtDistance = itemView.findViewById(R.id.txtDistance);
            worker_field = itemView.findViewById(R.id.worker_field);
        }
    }
}
