package workers.works;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Model class for worker projects/booking orders
 * Updated to handle both BookingOrder projects and WorkerInput proposals
 */
public class WorkerProjectModel {

    private String proposalId;
    private String projectId;
    private String userId; // Maps to userId in Firebase
    private String clientName; // Maps to Name
    private String clientPhone; // Maps to Mobile Number
    private String clientEmail; // Maps to Email
    private String clientAddress; // Maps to Home_Address

    private String workerId;
    private String workerName;

    private String serviceType; // Maps to Service_Type
    private String workDescription; // Maps to Description
    private String location; // Maps to Site_Address
    private String status; // pending, active, completed, cancelled

    private double totalCost; // Maps to Budget
    private String createdAt; // Maps to Date & Time
    private Timestamp createdAtTimestamp; // For WorkerInput proposals
    private Timestamp startDate;
    private Timestamp completionDate;

    private List<String> photos; // Photo URLs from Firebase
    private String notes;
    private boolean hasAcceptedProposal;

    // NEW: To distinguish between BookingOrder projects and WorkerInput proposals
    private boolean isProposal; // true if from WorkerInput, false if from BookingOrder

    // Cost breakdown for proposals
    private Double materialsCost;
    private Double laborCost;
    private Double miscCost;

    private Double vat;

    private Double totalcostwVat;

    public WorkerProjectModel() {
        // Required empty constructor for Firestore
        this.isProposal = false; // Default to project
    }

    public WorkerProjectModel( String proposalId, String projectId, String userId, String clientName,
                              String clientPhone, String clientEmail, String workerId,
                              String workerName, String workDescription, String location,
                              String status, double vat, double totalcostwVat, double totalCost, String createdAt) {
        this.proposalId = proposalId;
        this.projectId = projectId;
        this.userId = userId;
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
        this.vat = vat;
        this.totalcostwVat = totalcostwVat;
        this.isProposal = false;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public double getGrandTotalWithVat() {
        return totalcostwVat;
    }

    public void setGrandTotalWithVat(double totalcostwVat) {
        this.totalcostwVat = totalcostwVat;
    }
    public String getProposalId() {

        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
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

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCreatedAtTimestamp() {
        return createdAtTimestamp;
    }

    public void setCreatedAtTimestamp(Timestamp createdAtTimestamp) {
        this.createdAtTimestamp = createdAtTimestamp;
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

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
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

    // NEW: Methods to distinguish between proposals and projects
    public boolean isProposal() {
        return isProposal;
    }

    public void setProposal(boolean proposal) {
        isProposal = proposal;
    }

    // For adapter view type determination
    public boolean isForWorker() {
        return isProposal; // Proposals use worker layout (shows cost breakdown)
    }

    // Cost breakdown getters/setters
    public Double getMaterialsCost() {
        return materialsCost != null ? materialsCost : 0.0;
    }

    public void setMaterialsCost(Double materialsCost) {
        this.materialsCost = materialsCost;
    }

    public Double getLaborCost() {
        return laborCost != null ? laborCost : 0.0;
    }

    public void setLaborCost(Double laborCost) {
        this.laborCost = laborCost;
    }

    public Double getMiscCost() {
        return miscCost != null ? miscCost : 0.0;
    }

    public void setMiscCost(Double miscCost) {
        this.miscCost = miscCost;
    }
}