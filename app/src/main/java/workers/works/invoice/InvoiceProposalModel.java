package workers.works.invoice;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Model representing an invoice proposal sent from Worker to Client
 * FIXED: Added vat and grandTotalWithVat getters/setters
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
    private String workerDescription; // Add this for compatibility

    // Cost breakdown
    private List<MaterialItem> materials;
    private List<LaborItem> labor;
    private List<MiscItem> miscellaneous;

    // Totals
    private double totalMaterials;
    private double totalLabor;
    private double totalMisc;
    private double totalCost;  // Subtotal (same as grandTotal for compatibility)

    // 🔥 VAT FIELDS - THESE WERE MISSING GETTERS/SETTERS!
    private double vat;
    private double grandTotalWithVat;  // This is actually grandTotalWithVat

    // Status tracking
    private String status; // "pending", "accepted", "declined"
    private String createdAt;
    private Timestamp respondedAt;
    private String responseMessage;

    public InvoiceProposalModel() {
        this.status = "pending";
        this.createdAt = Timestamp.now().toString();
    }

    @SuppressLint("RestrictedApi")
    public InvoiceProposalModel(Invoice invoice, String workerId) {
        this();
        this.workerId = workerId;
        this.userId = invoice.getUserId();
        Log.d(TAG, String.format("Creating UserId - dito " + userId));

        // Copy invoice data
        this.workerName = invoice.getWorkerName();
        this.workerAddress = invoice.getWorkerAddress();
        this.workerPhone = invoice.getWorkerPhone();
        this.workerEmail = invoice.getWorkerEmail();

        this.clientName = invoice.getClientName();
        this.clientAddress = invoice.getClientAddress();
        this.clientPhone = invoice.getClientPhone();
        this.clientEmail = invoice.getClientEmail();

        this.workDescription = invoice.getWorkDescription();
        this.workerDescription = invoice.getWorkDescription(); // Duplicate for compatibility

        this.materials = invoice.getMaterials();
        this.labor = invoice.getLabor();
        this.miscellaneous = invoice.getMiscellaneous();

        // Calculate totals
        this.totalMaterials = calculateMaterialsTotal();
        this.totalLabor = calculateLaborTotal();
        this.totalMisc = calculateMiscTotal();

        // Subtotal
        this.totalCost = totalMaterials + totalLabor + totalMisc;

        // VAT and Grand Total
        this.vat = invoice.getVat();
        this.grandTotalWithVat = invoice.getGrandTotalWithVat();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getWorkerDescription() {
        return workerDescription;
    }

    public void setWorkerDescription(String workerDescription) {
        this.workerDescription = workerDescription;
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

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    // 🔥 VAT GETTERS/SETTERS - THESE WERE MISSING!
    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }




    // Compatibility getter/setter for grandTotalWithVat
    public double getGrandTotalWithVat() {
        return grandTotalWithVat;
    }

    public void setGrandTotalWithVat(double grandTotalWithVat) {
        this.grandTotalWithVat = grandTotalWithVat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
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