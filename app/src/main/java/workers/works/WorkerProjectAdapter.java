package workers.works;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Adapter for displaying worker's projects and proposals
 * Handles two types:
 * - TYPE_CLIENT: BookingOrder projects (from clients)
 * - TYPE_WORKER: WorkerInput proposals (created by worker)
 */
public class WorkerProjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_WORKER = 0;
    private static final int TYPE_CLIENT = 1;

    private List<WorkerProjectModel> projects;

    public WorkerProjectAdapter(List<WorkerProjectModel> projects) {
        this.projects = projects;
    }

    @Override
    public int getItemViewType(int position) {
        return projects.get(position).isForWorker() ? TYPE_WORKER : TYPE_CLIENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_WORKER) {
            View view = inflater.inflate(R.layout.item_proposal_worker, parent, false);
            return new WorkerViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_proposal_client, parent, false);
            return new ClientViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WorkerProjectModel project = projects.get(position);

        String status = project.getStatus() == null ? "pending" : project.getStatus();

        if (holder instanceof ClientViewHolder) {
            ClientViewHolder h = (ClientViewHolder) holder;

            h.tvWorkerName.setText(project.getWorkerName());
            h.tvProjectDescription.setText(project.getWorkDescription());
            h.tvStatus.setText(status.toUpperCase());
            h.tvTotalCost.setText("₱" + project.getTotalCost());

            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), WorkerProjectDetailsActivity.class);
                intent.putExtra("projectId", project.getProjectId());
                v.getContext().startActivity(intent);
            });



        } else if (holder instanceof WorkerViewHolder) {
            WorkerViewHolder h = (WorkerViewHolder) holder;

            h.tvProposalDate.setText("Sent: " + project.getCreatedAt());
            h.tvClientName.setText(project.getClientName());
            h.tvWorkDescription.setText(project.getWorkDescription());
            h.tvLocation.setText("📍 " + project.getLocation());
            status = project.getStatus() != null ? project.getStatus() : "pending";
            h.tvStatus.setText(getStatusText(status));
            h.tvStatus.setBackgroundResource(getStatusBackground(status));
            h.tvProposalAmount.setText("₱" + project.getTotalCost());



            if ("active".equals(status)) {
                h.tvActiveIndicator.setVisibility(View.VISIBLE);
                h.tvActiveIndicator.setText("🔨 In Progress");
            } else if ("pending".equals(status)) {
                h.tvActiveIndicator.setVisibility(View.VISIBLE);
                h.tvActiveIndicator.setText("⏳ Awaiting Response");
            } else {
                h.tvActiveIndicator.setVisibility(View.GONE);
            }

            if ("active".equals(status)) {
                h.cardView.setCardElevation(8f);
            } else {
                h.cardView.setCardElevation(4f);
            }

            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), WorkerProposalReceiptActivity.class);
                intent.putExtra("proposalId", project.getProposalId());
                v.getContext().startActivity(intent);
            });
        }

    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void updateList(List<WorkerProjectModel> projectList) {
        this.projects = projectList;
        notifyDataSetChanged();
    }

    // ========================= VIEW HOLDERS =========================

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkerName, tvProjectDescription, tvStatus, tvTotalCost;

        CardView cardView;
        ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvProjectDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotalCost = itemView.findViewById(R.id.tvTotalCost);
        }
    }

    static class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvActiveIndicator,tvProposalDate, tvWorkDescription, tvLocation, tvStatus, tvProposalAmount;

        CardView cardView;
        WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvProposalDate = itemView.findViewById(R.id.tvProposalDate);
            tvWorkDescription = itemView.findViewById(R.id.tvWorkDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvProposalAmount = itemView.findViewById(R.id.tvProposalAmount);
            tvActiveIndicator = itemView.findViewById(R.id.tvActiveIndicator);
        }
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
}
