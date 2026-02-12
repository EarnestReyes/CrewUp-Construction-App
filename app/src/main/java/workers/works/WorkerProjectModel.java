package workers.works;

import com.google.firebase.Timestamp;
import java.util.List;

public class WorkerProjectModel {

    private String proposalId;
    private String projectId;
    private String userId; // Map
    private String clientId;
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private String clientAddress;

    private String workerId;
    private String workerName;

    private String serviceType;
    private String workDescription;
    private String location;
    private String status;

    private double totalCost;
    private String createdAt;
    private Timestamp createdAtTimestamp;
    private Timestamp startDate;
    private Timestamp completionDate;

    private List<String> photos;
    private String notes;
    private boolean hasAcceptedProposal;

    // BUG FIX: Added actual backing field instead of infinite self-call
    private boolean isProposal; // true = WorkerInput proposal, false = BookingOrder project

    // Cost breakdown for proposals
    private Double materialsCost;
    private Double laborCost;
    private Double miscCost;


    private Double vat;

    private Double totalcostwVat;
    public WorkerProjectModel() {}

    public WorkerProjectModel( String proposalId, String projectId, String userId, String clientId, String clientName,
                              String clientPhone, String clientEmail, String workerId,
                              String workerName, String workDescription, String location,
                              String status, double totalCost, String createdAt) {

        this.proposalId = proposalId;
        this.projectId = projectId;
        this.clientId = clientId;
        this.userId = userId;
        this.clientName = clientName;
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

    // ----------------------------------------------------------
    //  BUG FIX: was `return isForWorker();` — infinite recursion
    //  Now returns the actual backing field `isProposal`
    // ----------------------------------------------------------
    public boolean isForWorker() {
        return isProposal;
    }

    public boolean isProposal() {
        return isProposal;
    }

    public void setProposal(boolean proposal) {
        isProposal = proposal;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    // getters / setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public String getClientAddress() { return clientAddress; }
    public void setClientAddress(String clientAddress) { this.clientAddress = clientAddress; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getWorkDescription() { return workDescription; }
    public void setWorkDescription(String workDescription) { this.workDescription = workDescription; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Timestamp getCreatedAtTimestamp() { return createdAtTimestamp; }
    public void setCreatedAtTimestamp(Timestamp t) { this.createdAtTimestamp = t; }

    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getCompletionDate() { return completionDate; }
    public void setCompletionDate(Timestamp completionDate) { this.completionDate = completionDate; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isHasAcceptedProposal() { return hasAcceptedProposal; }
    public void setHasAcceptedProposal(boolean v) { this.hasAcceptedProposal = v; }

    public double getMaterialsCost() { return materialsCost != null ? materialsCost : 0; }
    public void setMaterialsCost(Double v) { this.materialsCost = v; }

    public double getLaborCost() { return laborCost != null ? laborCost : 0; }
    public void setLaborCost(Double v) { this.laborCost = v; }

    public double getMiscCost() { return miscCost != null ? miscCost : 0; }
    public void setMiscCost(Double v) { this.miscCost = v; }
}