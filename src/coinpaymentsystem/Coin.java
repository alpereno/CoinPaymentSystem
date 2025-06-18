package coinpaymentsystem;

public enum Coin {
    // para türlerinin tanımlandığı class
    KURUS_05(0.05),
    KURUS_10(0.10),
    KURUS_25(0.25),
    KURUS_50(0.50),
    TL_1(1.0),
    TL_5(5.0),
    TL_10(10.0),
    TL_20(20.0),
    TL_50(50.0),
    TL_100(100.0),
    TL_200(200.0);

    private final double value;

    Coin(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}

