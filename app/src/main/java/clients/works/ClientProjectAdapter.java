package clients.works;

import android.content.Intent;
import android.util.Log;
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

/**
 * Adapter for displaying client's projects in RecyclerView
 */
public class ClientProjectAdapter extends RecyclerView.Adapter<ClientProjectAdapter.ProjectViewHolder> {

    private static final String TAG = "ClientProjectAdapter";
    private List<ClientProjectModel> projects;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public ClientProjectAdapter(List<ClientProjectModel> projects) {
        this.projects = projects;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proposal_client, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ClientProjectModel project = projects.get(position);

        // Materials cost
        holder.tvMaterialsCost.setText("₱" + currencyFormat.format(project.getMaterialsCost()));

        holder.tvLaborCost.setText("₱" + currencyFormat.format(project.getLaborCost()));

        holder.tvMiscCost.setText("₱" + currencyFormat.format(project.getMiscCost()));

        Log.d(TAG, "Binding project at position " + position + ": " + project.getProjectId());


        String workerName = project.getWorkerName();
        if (workerName != null && !workerName.isEmpty()) {
            holder.tvWorkerName.setText(workerName);
        } else {
            holder.tvWorkerName.setText("Contractor TBD");
        }


        String workDescription = project.getWorkDescription();
        if (workDescription != null && !workDescription.isEmpty()) {
            holder.tvWorkDescription.setText(workDescription);
        } else {
            holder.tvWorkDescription.setText("No description");
        }



        // Date
        if (project.getCreatedAt() != null) {
            try {
                String dateStr =project.getCreatedAt().toString();
                holder.tvDate.setText("Created: " + dateStr);
            } catch (Exception e) {
                Log.e(TAG, "Error formatting date", e);
                holder.tvDate.setText("Created: Recently");
            }
        } else {
            holder.tvDate.setText("Created: Recently");
        }

        // Status
        String status = project.getStatus();
        if (status == null || status.isEmpty()) {
            status = "pending";
        }
        holder.tvStatus.setText(getStatusText(status));
        holder.tvStatus.setBackgroundResource(getStatusBackground(status));

        // Total cost (if available)
        if (project.getTotalCost() > 0) {
            holder.tvTotalCost.setVisibility(View.VISIBLE);
            holder.tvTotalCost.setText("₱" + currencyFormat.format(project.getTotalCost()));
        } else {
            holder.tvTotalCost.setVisibility(View.GONE);
        }



        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ClientProjectDetailsActivity.class);
            intent.putExtra("projectId", project.getProjectId());
            v.getContext().startActivity(intent);
        });

        // Set card elevation based on status
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
        if (status == null) return "Unknown";

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
        if (status == null) return R.drawable.bg_status_pending;

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

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvWorkerName, tvStatus, tvWorkDescription;
        TextView tvDate, tvTotalCost, tvProposalIndicator;
        TextView tvMaterialsCost, tvLaborCost, tvMiscCost;

        ProjectViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;

            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvWorkDescription = itemView.findViewById(R.id.tvProjectDescription);

            tvDate = itemView.findViewById(R.id.tvProposalDate);
            tvTotalCost = itemView.findViewById(R.id.tvTotalCost);
            tvProposalIndicator = itemView.findViewById(R.id.tvProposalIndicator);

            tvMaterialsCost = itemView.findViewById(R.id.tvMaterialsCost);
            tvLaborCost = itemView.findViewById(R.id.tvLaborCost);
            tvMiscCost = itemView.findViewById(R.id.tvMiscCost);
        }
    }

}
