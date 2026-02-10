package workers.works;

import com.google.firebase.Timestamp;

/**
 * Model class for worker projects/booking orders
 */
public class WorkerProjectModel {

    private String projectId;
    private String clientId;
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private String workerId;
    private String workerName;
    private String workDescription;
    private String location;
    private String status; // pending, active, completed, cancelled
    private double totalCost;
    private Timestamp createdAt;
    private Timestamp startDate;
    private Timestamp completionDate;
    private String notes;
    private boolean hasAcceptedProposal;

    public WorkerProjectModel() {
        // Required empty constructor for Firestore
    }

    public WorkerProjectModel(String projectId, String clientId, String clientName,
                              String clientPhone, String clientEmail, String workerId,
                              String workerName, String workDescription, String location,
                              String status, double totalCost, Timestamp createdAt) {
        this.projectId = projectId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.clientEmail = clientEmail;
        this.workerId = workerId;
        this.workerName = workerName;
        this.workDescription = workDescription;
        this.location = location;
        this.status = status;
        this.totalCost = totalCost;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Timestamp completionDate) {
        this.completionDate = completionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isHasAcceptedProposal() {
        return hasAcceptedProposal;
    }

    public void setHasAcceptedProposal(boolean hasAcceptedProposal) {
        this.hasAcceptedProposal = hasAcceptedProposal;
    }
}