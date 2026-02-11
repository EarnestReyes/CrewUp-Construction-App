package clients.works;

import com.google.firebase.Timestamp;

public class ClientProjectModel {

    private String projectId;
    private String userId;
    private String workerId;

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

    // NEW: VAT and Grand Total
    private Double vat;           // VAT amount (calculated from totalCost)
    private Double grandTotal;    // Total + VAT

    private String createdAt;
    private Timestamp startDate;
    private Timestamp completionDate;

    public ClientProjectModel() {}

    // -------------------------
    // GETTERS AND SETTERS
    // -------------------------

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
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public double getMaterialsCost() {
        return materialsCost != null ? materialsCost : 0;
    }

    public void setMaterialsCost(Double materialsCost) {
        this.materialsCost = materialsCost;
    }

    public double getLaborCost() {
        return laborCost != null ? laborCost : 0;
    }

    public void setLaborCost(Double laborCost) {
        this.laborCost = laborCost;
    }

    public double getMiscCost() {
        return miscCost != null ? miscCost : 0;
    }

    public void setMiscCost(Double miscCost) {
        this.miscCost = miscCost;
    }

    public double getTotalCost() {
        return totalCost != null ? totalCost : 0;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    // NEW: VAT getters/setters
    public double getVat() {
        return vat != null ? vat : 0;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public double getGrandTotal() {
        return grandTotal != null ? grandTotal : 0;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerPhone() {
        return workerPhone;
    }

    public void setWorkerPhone(String workerPhone) {
        this.workerPhone = workerPhone;
    }

    public String getWorkerEmail() {
        return workerEmail;
    }

    public void setWorkerEmail(String workerEmail) {
        this.workerEmail = workerEmail;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getBudget() {
        return Budget;
    }

    public void setBudget(String budget) {
        Budget = budget;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
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
}