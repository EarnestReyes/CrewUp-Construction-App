package workers.works.invoice;

public class LaborItem {
    private String laborType;
    private int hours;
    private double rate;
    private double amount;
    
    public LaborItem() {
    }
    
    public LaborItem(String laborType, int hours, double rate) {
        this.laborType = laborType;
        this.hours = hours;
        this.rate = rate;
        this.amount = hours * rate;
    }
    
    // Getters and Setters
    public String getLaborType() {
        return laborType;
    }
    
    public void setLaborType(String laborType) {
        this.laborType = laborType;
    }
    
    public int getHours() {
        return hours;
    }
    
    public void setHours(int hours) {
        this.hours = hours;
        calculateAmount();
    }
    
    public double getRate() {
        return rate;
    }
    
    public void setRate(double rate) {
        this.rate = rate;
        calculateAmount();
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    private void calculateAmount() {
        this.amount = this.hours * this.rate;
    }
}
