package workers.works.invoice;

public class MiscItem {
    private String description;
    private double amount;
    
    public MiscItem() {
    }
    
    public MiscItem(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }
    
    // Getters and Setters
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
