package workers.works.invoice;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Model representing an invoice proposal sent from Worker to Client
 */
public class InvoiceProposalModel {

    private String proposalId;
    private String workerId;
    private String userId;
    private String projectId; // Link to BookingOrder

    // Invoice details
    private String workerName;
    private String workerAddress;
    private String workerPhone;
    private String workerEmail;

    private String clientName;
    private String clientAddress;
    private String clientPhone;
    private String clientEmail;

    private String workDescription;

    // Cost breakdown
    private List<MaterialItem> materials;
    private List<LaborItem> labor;
    private List<MiscItem> miscellaneous;

    // Totals
    private double totalMaterials;
    private double totalLabor;
    private double totalMisc;
    private double grandTotal;

    // Status tracking
    private String status; // "pending", "accepted", "declined"
    private Timestamp createdAt;
    private Timestamp respondedAt;
    private String responseMessage;

    public InvoiceProposalModel() {
        this.status = "pending";
        this.createdAt = Timestamp.now();
    }

    public InvoiceProposalModel(Invoice invoice, String workerId, String userId) {
        this();
        this.workerId = workerId;
        this.userId = userId;

        // Copy invoice data
        this.workerName = invoice.getCompanyName();
        this.workerAddress = invoice.getCompanyAddress();
        this.workerPhone = invoice.getCompanyPhone();
        this.workerEmail = invoice.getCompanyEmail();

        this.clientName = invoice.getClientName();
        this.clientAddress = invoice.getClientAddress();
        this.clientPhone = invoice.getClientPhone();
        this.clientEmail = invoice.getClientEmail();

        this.workDescription = invoice.getWorkDescription();

        this.materials = invoice.getMaterials();
        this.labor = invoice.getLabor();
        this.miscellaneous = invoice.getMiscellaneous();

        // Calculate totals
        this.totalMaterials = calculateMaterialsTotal();
        this.totalLabor = calculateLaborTotal();
        this.totalMisc = calculateMiscTotal();
        this.grandTotal = totalMaterials + totalLabor + totalMisc;
    }

    private double calculateMaterialsTotal() {
        double total = 0;
        if (materials != null) {
            for (MaterialItem item : materials) {
                total += item.getTotal();
            }
        }
        return total;
    }

    private double calculateLaborTotal() {
        double total = 0;
        if (labor != null) {
            for (LaborItem item : labor) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private double calculateMiscTotal() {
        double total = 0;
        if (miscellaneous != null) {
            for (MiscItem item : miscellaneous) {
                total += item.getAmount();
            }
        }
        return total;
    }

    // Getters and Setters
    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getClientId() {
        return userId;
    }

    public void setClientId(String clientId) {
        this.userId= clientId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerAddress() {
        return workerAddress;
    }

    public void setWorkerAddress(String workerAddress) {
        this.workerAddress = workerAddress;
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
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

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public List<MaterialItem> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialItem> materials) {
        this.materials = materials;
    }

    public List<LaborItem> getLabor() {
        return labor;
    }

    public void setLabor(List<LaborItem> labor) {
        this.labor = labor;
    }

    public List<MiscItem> getMiscellaneous() {
        return miscellaneous;
    }

    public void setMiscellaneous(List<MiscItem> miscellaneous) {
        this.miscellaneous = miscellaneous;
    }

    public double getTotalMaterials() {
        return totalMaterials;
    }

    public void setTotalMaterials(double totalMaterials) {
        this.totalMaterials = totalMaterials;
    }

    public double getTotalLabor() {
        return totalLabor;
    }

    public void setTotalLabor(double totalLabor) {
        this.totalLabor = totalLabor;
    }

    public double getTotalMisc() {
        return totalMisc;
    }

    public void setTotalMisc(double totalMisc) {
        this.totalMisc = totalMisc;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Timestamp respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}