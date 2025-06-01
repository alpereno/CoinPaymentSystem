package coinpaymentsystem;

import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

public class CoinPaymentSystem {

    public static void main(String[] args) {
        // TODO code application logic here

        CashRegister register = new CashRegister();
        register.initializeDefaultCoins(3);
        register.printInventory(); // Bakiyeyi göster

        PaymentProcessor processor = new PaymentProcessor(register);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Urun fiyatini girin (cikmak icin -1): ");
            double price = scanner.nextDouble();
            if (price == -1) {
                break;
            }

            Map<Coin, Integer> payment = new EnumMap<>(Coin.class);
            for (Coin coin : Coin.values()) {
                System.out.printf("%s kac adet verdiniz?: ", coin.name());
                int count = scanner.nextInt();
                if (count > 0) {
                    payment.put(coin, count);
                }
            }

            processor.processPayment(price, payment);
        }

        System.out.println("Uygulama sonlandi.");

    }
}
