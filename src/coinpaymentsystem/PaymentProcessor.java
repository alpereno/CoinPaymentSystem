package coinpaymentsystem;
import java.util.*;

public class PaymentProcessor {
    private final CashRegister cashRegister;

    public PaymentProcessor(CashRegister cashRegister) {
        this.cashRegister = cashRegister;
    }

    public void processPayment(double price, Map<Coin, Integer> paymentCoins) {
        double paidAmount = paymentCoins.entrySet().stream()
                .mapToDouble(e -> e.getKey().getValue() * e.getValue()).sum();

        System.out.printf("Odeme: %.2f TL | Fiyat: %.2f TL\n", paidAmount, price);

        if (paidAmount < price) {
            System.out.println("Yetersiz odeme! Lütfen daha fazla para verin.");
            return;
        }

        double changeAmount = paidAmount - price;

        // Kasaya ekle
        cashRegister.addCoins(paymentCoins);

        if (changeAmount == 0) {
            System.out.println("Tam ödeme alindi, para üstü yok.");
        } else {
            Map<Coin, Integer> change = cashRegister.calculateChange(changeAmount);
            if (change.isEmpty()) {
                System.out.println("Para ustu verilemiyor!");

                Optional<String> suggestion = cashRegister.suggestAlternative(changeAmount);
                suggestion.ifPresent(System.out::println);
            } else {
                System.out.println("Para ustu:");
                for (Map.Entry<Coin, Integer> entry : change.entrySet()) {
                    System.out.printf("- %s: %d adet\n", entry.getKey().name(), entry.getValue());
                }
                cashRegister.giveChange(change);
            }
        }
        cashRegister.printInventory();
    }
}
