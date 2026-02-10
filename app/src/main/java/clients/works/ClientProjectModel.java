package clients.works;

import com.google.firebase.Timestamp;

public class ClientProjectModel {

    private String projectId;
    private String userId;

    private String workerName;
    private String workerPhone;
    private String workerEmail;

    private String workDescription;
    private String location;
    private String status;
    private String notes;

    // Firestore stores this as STRING
    private String Budget;

    // Optional numeric costs (if present)
    private Double materialsCost;
    private Double laborCost;
    private Double miscCost;
    private Double totalCost;
    private Timestamp createdAt;
    private Timestamp startDate;
    private Timestamp completionDate;

    public ClientProjectModel() {}

    // -------------------------
    // REQUIRED BY ADAPTER
    // -------------------------

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public double getMaterialsCost() {
        return materialsCost != null ? materialsCost : 0;
    }

    public double getLaborCost() {
        return laborCost != null ? laborCost : 0;
    }

    public double getMiscCost() {
        return miscCost != null ? miscCost : 0;
    }

    // -------------------------
    // TOTAL COST (STRING → DOUBLE SAFE)
    // -------------------------

    public double getTotalCost() {
        return totalCost != null ? totalCost : 0;
    }

    // -------------------------
    // OTHER GETTERS
    // -------------------------

    public String getWorkerName() { return workerName; }
    public String getWorkerPhone() { return workerPhone; }
    public String getWorkerEmail() { return workerEmail; }
    public String getWorkDescription() { return workDescription; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getStartDate() { return startDate; }
    public Timestamp getCompletionDate() { return completionDate; }
}
