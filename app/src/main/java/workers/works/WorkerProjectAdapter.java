package workers.works;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkerProjectAdapter extends RecyclerView.Adapter<WorkerProjectAdapter.ProjectViewHolder> {

    private List<WorkerProjectModel> projects;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public WorkerProjectAdapter(List<WorkerProjectModel> projects) {
        this.projects = projects;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proposal_worker, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        WorkerProjectModel project = projects.get(position);

        holder.tvClientName.setText(project.getClientName() != null ?
                project.getClientName() : "Client");

        holder.tvWorkDescription.setText(project.getWorkDescription() != null ?
                project.getWorkDescription() : "No description");

        holder.tvLocation.setText(project.getLocation() != null ?
                "📍 " + project.getLocation() : "Location not specified");

        if (project.getCreatedAt() != null) {
            String dateStr = project.getCreatedAt();
            holder.tvDate.setText("Created: " + dateStr);
        } else {
            holder.tvDate.setText("Created: Recently");
        }

        String status = project.getStatus() != null ? project.getStatus() : "pending";
        holder.tvStatus.setText(getStatusText(status));
        holder.tvStatus.setBackgroundResource(getStatusBackground(status));

        // Total cost (if available)
        if (project.getTotalCost() > 0) {
            holder.tvTotalCost.setVisibility(View.VISIBLE);
            holder.tvTotalCost.setText("₱" + currencyFormat.format(project.getTotalCost()));
        } else {
            holder.tvTotalCost.setVisibility(View.GONE);
        }

        if ("active".equals(status)) {
            holder.tvActiveIndicator.setVisibility(View.VISIBLE);
            holder.tvActiveIndicator.setText("🔨 In Progress");
        } else if ("pending".equals(status)) {
            holder.tvActiveIndicator.setVisibility(View.VISIBLE);
            holder.tvActiveIndicator.setText("⏳ Awaiting Response");
        } else {
            holder.tvActiveIndicator.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), WorkerProjectDetailsActivity.class);
            intent.putExtra("projectId", project.getProjectId());
            v.getContext().startActivity(intent);
        });

        if ("active".equals(status)) {
            holder.cardView.setCardElevation(8f);
        } else {
            holder.cardView.setCardElevation(4f);
        }
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    private String getStatusText(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "Pending";
            case "active":
                return "Active";
            case "completed":
                return "Completed";
            case "cancelled":
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    private int getStatusBackground(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return R.drawable.bg_status_pending;
            case "active":
                return R.drawable.bg_status_accepted;
            case "completed":
                return R.drawable.bg_status_completed;
            case "cancelled":
                return R.drawable.bg_status_declined;
            default:
                return R.drawable.bg_status_pending;
        }
    }
    public void updateList(List<WorkerProjectModel> newList) {
        this.projects = newList;
        notifyDataSetChanged();
    }


    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvClientName, tvStatus, tvWorkDescription, tvLocation;
        TextView tvDate, tvTotalCost, tvActiveIndicator;

        ProjectViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvWorkDescription = itemView.findViewById(R.id.tvWorkDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvProposalDate);
            tvTotalCost = itemView.findViewById(R.id.tvProposalAmount);

            tvActiveIndicator = itemView.findViewById(R.id.tvActiveIndicator);
        }
    }
}