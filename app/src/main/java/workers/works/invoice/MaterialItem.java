package workers.works.invoice;

public class MaterialItem {
    private int quantity;
    private String materialName;
    private double rate;
    private double total;
    
    public MaterialItem() {
    }
    
    public MaterialItem(int quantity, String materialName, double rate) {
        this.quantity = quantity;
        this.materialName = materialName;
        this.rate = rate;
        this.total = quantity * rate;
    }
    
    // Getters and Setters
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotal();
    }
    
    public String getMaterialName() {
        return materialName;
    }
    
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }
    
    public double getRate() {
        return rate;
    }
    
    public void setRate(double rate) {
        this.rate = rate;
        calculateTotal();
    }
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    private void calculateTotal() {
        this.total = this.quantity * this.rate;
    }
}
