package adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

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

        WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.txtName);
            txtRole = itemView.findViewById(R.id.txtRole);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtDistance = itemView.findViewById(R.id.txtDistance);
        }
    }
}
