package clients.works;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

/**
 * Model for WorkerInput collection (worker proposals to clients)
 * Based on actual Firebase structure
 */
public class ClientProjectModel {

    // IDs
    private String projectId;          // Document ID
    private String userId;             // Client ID
    private String workerId;           // Worker ID
    private String proposalId;         // Proposal ID

    // Client info
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String clientAddress;

    // Worker info
    private String workerName;
    private String workerEmail;
    private String workerPhone;
    private String workerAddress;
    private String workerDescription;  // Work description from worker

    // Project details
    private String status;             // pending, active, completed, cancelled

    // Cost breakdown
    private Double totalCost;          // Subtotal
    private Double totalLabor;         // Total labor cost
    private Double totalMaterials;     // Total materials cost (field name: totalMaterials, not materialsCost)
    private Double totalMisc;          // Total misc cost
    private Double vat;                // VAT amount
    private Double grandTotal;         // Total + VAT
    private Double grandTotalWithVat;

    // Arrays
    private List<Map<String, Object>> labor;          // Labor items
    private List<Map<String, Object>> materials;      // Material items
    private List<Map<String, Object>> miscellaneous;  // Misc items

    // Timestamps
    private String createdAt;

    public ClientProjectModel() {
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

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }


    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    // Client info
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    // Worker info
    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerEmail() {
        return workerEmail;
    }

    public void setWorkerEmail(String workerEmail) {
        this.workerEmail = workerEmail;
    }

    public String getWorkerPhone() {
        return workerPhone;
    }

    public void setWorkerPhone(String workerPhone) {
        this.workerPhone = workerPhone;
    }

    public String getWorkerAddress() {
        return workerAddress;
    }

    public void setWorkerAddress(String workerAddress) {
        this.workerAddress = workerAddress;
    }

    public String getWorkerDescription() {
        return workerDescription;
    }

    public void setWorkerDescription(String workerDescription) {
        this.workerDescription = workerDescription;
    }

    // For compatibility with adapter (uses getWorkDescription)
    public String getWorkDescription() {
        return workerDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workerDescription = workDescription;
    }

    // Status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Costs - safe getters return 0 if null
    public double getTotalCost() {
        return totalCost != null ? totalCost : 0.0;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalLabor() {
        return totalLabor != null ? totalLabor : 0.0;
    }

    public void setTotalLabor(Double totalLabor) {
        this.totalLabor = totalLabor;
    }

    // Note: Firebase field is "totalMaterials", not "materialsCost"
    public double getTotalMaterials() {
        return totalMaterials != null ? totalMaterials : 0.0;
    }

    public void setTotalMaterials(Double totalMaterials) {
        this.totalMaterials = totalMaterials;
    }

    // For compatibility with adapter (uses getMaterialsCost)
    public double getMaterialsCost() {
        return getTotalMaterials();
    }

    public void setMaterialsCost(Double materialsCost) {
        this.totalMaterials = materialsCost;
    }

    // For compatibility with adapter (uses getLaborCost)
    public double getLaborCost() {
        return getTotalLabor();
    }

    public void setLaborCost(Double laborCost) {
        this.totalLabor = laborCost;
    }

    public double getTotalMisc() {
        return totalMisc != null ? totalMisc : 0.0;
    }

    public void setTotalMisc(Double totalMisc) {
        this.totalMisc = totalMisc;
    }

    // For compatibility with adapter (uses getMiscCost)
    public double getMiscCost() {
        return getTotalMisc();
    }

    public void setMiscCost(Double miscCost) {
        this.totalMisc = miscCost;
    }

    public double getVat() {
        return vat != null ? vat : 0.0;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public double getGrandTotal() {
        return grandTotal != null ? grandTotal : 0.0;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    // Arrays
    public List<Map<String, Object>> getLabor() {
        return labor;
    }

    public void setLabor(List<Map<String, Object>> labor) {
        this.labor = labor;
    }

    public List<Map<String, Object>> getMaterials() {
        return materials;
    }

    public void setMaterials(List<Map<String, Object>> materials) {
        this.materials = materials;
    }

    public List<Map<String, Object>> getMiscellaneous() {
        return miscellaneous;
    }

    public void setMiscellaneous(List<Map<String, Object>> miscellaneous) {
        this.miscellaneous = miscellaneous;
    }

    // Timestamp
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public double getGrandTotalWithVat() {
        return grandTotalWithVat != null ? grandTotalWithVat : 0.0;
    }

    public void setGrandTotalWithVat(Double grandTotalWithVat) {
        this.grandTotalWithVat = grandTotalWithVat;
    }
}