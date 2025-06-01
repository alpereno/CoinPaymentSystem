package coinpaymentsystem;

public enum Coin {
    
    KURUS_05(0.05),
    KURUS_10(0.10),
    KURUS_25(0.25),
    KURUS_50(0.50),
    TL_1(1.0);
    
    private final double value;

    Coin(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
    
}
