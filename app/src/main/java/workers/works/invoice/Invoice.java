package workers.works.invoice;

import java.util.List;

public class Invoice {
    // Company Information
    private String workerName;
    private String workerAddress;
    private String workerPhone;
    private String workerEmail;
    private String userId;

    
    // Client Information
    private String clientName;
    private String clientAddress;
    private String clientPhone;
    private String clientEmail;
    
    // Work Description
    private String workDescription;
    
    // Line Items
    private List<MaterialItem> materials;
    private List<LaborItem> labor;
    private List<MiscItem> miscellaneous;

    private double vat;
    private double grandTotalWithVat;
    

    // Constructors
    public Invoice() {
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public double getGrandTotalWithVat() {
        return grandTotalWithVat;
    }

    public void setGrandTotalWithVat(double grandTotalWithVat) {
        this.grandTotalWithVat = grandTotalWithVat;
    }
    // Getters and Setters
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


    
    // Calculate totals
    public double getTotalMaterials() {
        if (materials == null) return 0;
        double total = 0;
        for (MaterialItem item : materials) {
            total += item.getTotal();
        }
        return total;
    }
    
    public double getTotalLabor() {
        if (labor == null) return 0;
        double total = 0;
        for (LaborItem item : labor) {
            total += item.getAmount();
        }
        return total;
    }
    
    public double getTotalMiscellaneous() {
        if (miscellaneous == null) return 0;
        double total = 0;
        for (MiscItem item : miscellaneous) {
            total += item.getAmount();
        }
        return total;
    }
    
    public double getSubtotal() {
        return getTotalMaterials() + getTotalLabor() + getTotalMiscellaneous();
    }



    public double getGrandTotal() {
        return getSubtotal();
    }


    public String getUserId() {
return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
