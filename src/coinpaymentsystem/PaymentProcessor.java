package coinpaymentsystem;

import java.util.*;

public class PaymentProcessor {

    private final CashRegister cashRegister;

    public PaymentProcessor(CashRegister cashRegister) {
        this.cashRegister = cashRegister;
    }

    public void processPayment(double price, Map<Coin, Integer> paymentCoins) {
        double paidAmount = paymentCoins.entrySet().stream()
                .mapToDouble(e -> e.getKey().getValue() * e.getValue())
                .sum();

        System.out.printf("Ödeme: %.2f TL | Fiyat: %.2f TL\n", paidAmount, price);

        if (paidAmount < price) {
            System.out.println("Yetersiz ödeme! Lütfen daha fazla para verin.");
            return;
        }

        double changeAmount = Math.round((paidAmount - price) * 100.0) / 100.0;

        // 🔹 1. Önce geçici olarak ödeme kasaya eklenir
        cashRegister.addCoins(paymentCoins);

        // 🔹 2. Bu yeni durumda para üstü verilebiliyor mu kontrol edilir
        Map<Coin, Integer> change = cashRegister.calculateChange(changeAmount);

        if (changeAmount > 0 && change.isEmpty()) {
            // 🔸 3. Yeni durumda da para üstü verilemiyorsa → ödeme iptal
            System.out.println("Para üstü verilemiyor! Ödeme iptal edildi.");

            // 🔸 Tavsiye mesajı
            Optional<String> suggestion = cashRegister.suggestAlternative(changeAmount);
            suggestion.ifPresent(System.out::println);

            // 🔸 4. Ödeme kasadan geri alınır
            for (Map.Entry<Coin, Integer> entry : paymentCoins.entrySet()) {
                Coin coin = entry.getKey();
                int currentCount = cashRegister.getCoinInventory().get(coin);
                cashRegister.getCoinInventory().put(coin, currentCount - entry.getValue());
            }

            return;
        }

        // 🔹 4. Ödeme geçerli, para üstü varsa verilir
        if (changeAmount == 0) {
            System.out.println("Tam ödeme alındı, para üstü yok.");
        } else {
            System.out.println("Para üstü:");
            for (Map.Entry<Coin, Integer> entry : change.entrySet()) {
                System.out.printf("- %s: %d adet\n", entry.getKey().name(), entry.getValue());
            }

            cashRegister.giveChange(change);
        }

        // 🔹 5. Kasa durumu yazdırılır
        cashRegister.printInventory();
    }

}
