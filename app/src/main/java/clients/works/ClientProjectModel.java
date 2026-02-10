package clients.works;

import com.google.firebase.Timestamp;

/**
 * Model class for client projects/booking orders
 * Updated to match Firebase field names
 */
public class ClientProjectModel {

    private String projectId;

    // Firebase uses 'userId' not 'clientId'
    private String userId;
    private String clientId;

    private String clientName;
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

    private double materialsCost;
    private double laborCost;
    private double miscCost;


    public ClientProjectModel() {
        // Required empty constructor for Firestore
    }

    public ClientProjectModel(String projectId, String userId, String clientName,
                              String workerId, String workerName, String workDescription,
                              String location, String status, double totalCost,
                              Timestamp createdAt) {
        this.projectId = projectId;
        this.userId = userId;
        this.clientId = userId; // Set both for compatibility
        this.clientName = clientName;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        this.clientId = userId; // Keep both in sync
    }

    public String getClientId() {
        // Return userId if clientId is null (for backward compatibility)
        return clientId != null ? clientId : userId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
        this.userId = clientId; // Keep both in sync
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public double getMaterialsCost() {
        return materialsCost;
    }

    public void setMaterialsCost(double materialsCost) {
        this.materialsCost = materialsCost;
    }

    public double getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(double laborCost) {
        this.laborCost = laborCost;
    }

    public double getMiscCost() {
        return miscCost;
    }

    public void setMiscCost(double miscCost) {
        this.miscCost = miscCost;
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


}