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
import java.util.List;

/**
 * Adapter for displaying worker's projects AND proposals
 *
 * Layout purpose (based on actual XML content):
 * ─────────────────────────────────────────────────────────────────
 * item_proposal_client.xml  → designed for the CLIENT's perspective
 *   title: tvWorkerName, has cost breakdown (materials/labor/misc)
 *   Used here for: WorkerInput PROPOSALS (worker viewing their own
 *                  submitted cost breakdown, isProposal = true)
 *
 * item_proposal_worker.xml  → designed for the WORKER's perspective
 *   title: tvClientName, has location + active indicator
 *   Used here for: BookingOrder PROJECTS (worker viewing the client
 *                  who booked them, isProposal = false)
 * ─────────────────────────────────────────────────────────────────
 */
public class WorkerProjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // TYPE_PROJECT → item_proposal_worker.xml (BookingOrder, isProposal = false)
    // TYPE_PROPOSAL → item_proposal_client.xml (WorkerInput, isProposal = true)
    private static final int TYPE_PROJECT  = 0;
    private static final int TYPE_PROPOSAL = 1;

    private List<WorkerProjectModel> projects;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    public WorkerProjectAdapter(List<WorkerProjectModel> projects) {
        this.projects = projects;
    }

    @Override
    public int getItemViewType(int position) {
        // isProposal = true  → WorkerInput   → TYPE_PROPOSAL → item_proposal_client.xml
        // isProposal = false → BookingOrder  → TYPE_PROJECT  → item_proposal_worker.xml
        return projects.get(position).isProposal() ? TYPE_PROPOSAL : TYPE_PROJECT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_PROPOSAL) {
            // WorkerInput proposals → use item_proposal_client.xml (has cost breakdown)
            View view = inflater.inflate(R.layout.item_proposal_client, parent, false);
            return new ProposalViewHolder(view);
        } else {
            // BookingOrder projects → use item_proposal_worker.xml (has client name / location)
            View view = inflater.inflate(R.layout.item_proposal_worker, parent, false);
            return new ProjectViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WorkerProjectModel project = projects.get(position);
        String status = project.getStatus() != null
                ? project.getStatus().trim().toLowerCase()
                : "pending";

        if (holder instanceof ProposalViewHolder) {
            bindProposalHolder((ProposalViewHolder) holder, project, status);
        } else if (holder instanceof ProjectViewHolder) {
            bindProjectHolder((ProjectViewHolder) holder, project, status);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // WorkerInput PROPOSAL card → item_proposal_client.xml
    // Worker views their own submitted proposal with cost breakdown
    // ─────────────────────────────────────────────────────────────────
    private void bindProposalHolder(ProposalViewHolder h, WorkerProjectModel project, String status) {

        // tvWorkerName in this layout → show the worker's own name (this is their proposal)
        h.tvWorkerName.setText(project.getWorkerName() != null
                ? project.getWorkerName()
                : "My Proposal");

        h.tvProjectDescription.setText(project.getWorkDescription() != null
                ? project.getWorkDescription()
                : "No description");

        h.tvProposalDate.setText(project.getCreatedAt() != null
                ? "Sent: " + project.getCreatedAt()
                : "Sent: Recently");

        h.tvStatus.setText(getStatusText(status));
        h.tvStatus.setBackgroundResource(getStatusBackground(status));

        // Cost breakdown
        h.tvMaterialsCost.setText("₱" + currencyFormat.format(project.getMaterialsCost()));
        h.tvLaborCost.setText("₱" + currencyFormat.format(project.getLaborCost()));
        h.tvMiscCost.setText("₱" + currencyFormat.format(project.getMiscCost()));
        h.tvTotalCost.setText("₱" + currencyFormat.format(project.getTotalCost()));

        h.cardView.setCardElevation("active".equals(status) ? 8f : 4f);

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), WorkerProposalReceiptActivity.class);
            intent.putExtra("proposalId", project.getProjectId());
            v.getContext().startActivity(intent);
        });
    }

    // ─────────────────────────────────────────────────────────────────
    // BookingOrder PROJECT card → item_proposal_worker.xml
    // Worker views a project assigned to them (sees client info)
    // ─────────────────────────────────────────────────────────────────
    private void bindProjectHolder(ProjectViewHolder h, WorkerProjectModel project, String status) {

        // tvClientName → show who booked this worker
        h.tvClientName.setText(project.getClientName() != null
                ? project.getClientName()
                : "Client");

        h.tvWorkDescription.setText(project.getWorkDescription() != null
                ? project.getWorkDescription()
                : "No description");

        h.tvProposalDate.setText(project.getCreatedAt() != null
                ? project.getCreatedAt()
                : "Recently");

        h.tvLocation.setText("📍 " + (project.getLocation() != null
                ? project.getLocation()
                : "Location not set"));

        h.tvStatus.setText(getStatusText(status));
        h.tvStatus.setBackgroundResource(getStatusBackground(status));

        h.tvProposalAmount.setText("₱" + currencyFormat.format(project.getTotalCost()));

        if ("active".equals(status)) {
            h.tvActiveIndicator.setVisibility(View.VISIBLE);
            h.tvActiveIndicator.setText("🔨 In Progress");
        } else if ("pending".equals(status)) {
            h.tvActiveIndicator.setVisibility(View.VISIBLE);
            h.tvActiveIndicator.setText("⏳ Awaiting Response");
        } else {
            h.tvActiveIndicator.setVisibility(View.GONE);
        }

        h.cardView.setCardElevation("active".equals(status) ? 8f : 4f);

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), WorkerProjectDetailsActivity.class);
            intent.putExtra("projectId", project.getProjectId());
            v.getContext().startActivity(intent);
        });
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

    /**
     * For WorkerInput PROPOSALS → inflates item_proposal_client.xml
     * IDs: tvWorkerName, tvProjectDescription, tvProposalDate,
     *      tvMaterialsCost, tvLaborCost, tvMiscCost, tvTotalCost, tvStatus
     */
    static class ProposalViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvWorkerName;
        TextView tvProjectDescription;
        TextView tvProposalDate;
        TextView tvStatus;
        TextView tvMaterialsCost;
        TextView tvLaborCost;
        TextView tvMiscCost;
        TextView tvTotalCost;

        ProposalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvWorkerName        = itemView.findViewById(R.id.tvWorkerName);
            tvProjectDescription= itemView.findViewById(R.id.tvProjectDescription);
            tvProposalDate      = itemView.findViewById(R.id.tvProposalDate);
            tvStatus            = itemView.findViewById(R.id.tvStatus);
            tvMaterialsCost     = itemView.findViewById(R.id.tvMaterialsCost);
            tvLaborCost         = itemView.findViewById(R.id.tvLaborCost);
            tvMiscCost          = itemView.findViewById(R.id.tvMiscCost);
            tvTotalCost         = itemView.findViewById(R.id.tvTotalCost);
        }
    }

    /**
     * For BookingOrder PROJECTS → inflates item_proposal_worker.xml
     * IDs: tvClientName, tvWorkDescription, tvProposalDate, tvLocation,
     *      tvActiveIndicator, tvProposalAmount, tvStatus
     */
    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvClientName;
        TextView tvWorkDescription;
        TextView tvProposalDate;
        TextView tvLocation;
        TextView tvActiveIndicator;
        TextView tvStatus;
        TextView tvProposalAmount;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvClientName    = itemView.findViewById(R.id.tvClientName);
            tvWorkDescription = itemView.findViewById(R.id.tvWorkDescription);
            tvProposalDate  = itemView.findViewById(R.id.tvProposalDate);
            tvLocation      = itemView.findViewById(R.id.tvLocation);
            tvActiveIndicator = itemView.findViewById(R.id.tvActiveIndicator);
            tvStatus        = itemView.findViewById(R.id.tvStatus);
            tvProposalAmount= itemView.findViewById(R.id.tvProposalAmount);
        }
    }

    // ========================= HELPERS =========================

    private String getStatusText(String status) {
        switch (status.toLowerCase()) {
            case "pending":   return "Pending";
            case "active":    return "Active";
            case "completed": return "Completed";
            case "cancelled": return "Cancelled";
            default:          return "Unknown";
        }
    }

    private int getStatusBackground(String status) {
        switch (status.toLowerCase()) {
            case "pending":   return R.drawable.bg_status_pending;
            case "active":    return R.drawable.bg_status_accepted;
            case "completed": return R.drawable.bg_status_completed;
            case "cancelled": return R.drawable.bg_status_declined;
            default:          return R.drawable.bg_status_pending;
        }
    }
}