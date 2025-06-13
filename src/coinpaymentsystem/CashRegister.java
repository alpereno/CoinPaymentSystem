package coinpaymentsystem;

import java.util.*;

public class CashRegister {

    private final Map<Coin, Integer> coinInventory = new EnumMap<>(Coin.class);

    public CashRegister() {
        for (Coin coin : Coin.values()) {
            coinInventory.put(coin, 0);
        }
    }

    public void addCoins(Map<Coin, Integer> coins) {
        for (Map.Entry<Coin, Integer> entry : coins.entrySet()) {
            coinInventory.put(entry.getKey(), coinInventory.get(entry.getKey()) + entry.getValue());
        }
    }

    public void printInventory() {
        System.out.println("Kasadaki bozuk paralar:");
        double totalAmount = 0;

        for (Coin coin : Coin.values()) {
            int count = coinInventory.get(coin);
            double value = coin.getValue();
            System.out.printf("- %s: %d adet (%.2f TL)\n", coin.name(), count, count * value);
            totalAmount += count * value;
        }

        System.out.printf("Kasadaki toplam para: %.2f TL\n", totalAmount);
    }

    public boolean canGiveChange(double changeAmount) {
        return findChangeCombination(changeAmount) != null;
    }

    public Map<Coin, Integer> calculateChange(double amount) {
        Map<Coin, Integer> combination = findChangeCombination(amount);
        if (combination != null) {
            return combination;
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<Coin, Integer> findChangeCombination(double amount) {
        Map<Coin, Integer> result = new EnumMap<>(Coin.class);
        Map<Coin, Integer> inventoryCopy = new EnumMap<>(coinInventory);
        Coin[] coins = Coin.values();
        // oncelik en yakın ve en az bozukluk verme yönünde
        Arrays.sort(coins, Comparator.comparingDouble(Coin::getValue).reversed());

        if (searchCombination(amount, coins, 0, inventoryCopy, result)) {
            return result;
        } else {
            return null;
        }
    }

    private boolean searchCombination(double remaining, Coin[] coins, int index,
            Map<Coin, Integer> inventoryCopy, Map<Coin, Integer> result) {
        if (Math.abs(remaining) < 0.001) {
            return true;
        }
        if (index >= coins.length) {
            return false;
        }

        Coin coin = coins[index];
        int maxAvailable = inventoryCopy.getOrDefault(coin, 0);
        int maxUsable = (int) (remaining / coin.getValue());
        int useUpTo = Math.min(maxAvailable, maxUsable);

        for (int i = useUpTo; i >= 0; i--) {
            double newRemaining = remaining - (i * coin.getValue());
            newRemaining = Math.round(newRemaining * 100.0) / 100.0;

            if (i > 0) {
                result.put(coin, i);
            }
            if (searchCombination(newRemaining, coins, index + 1, inventoryCopy, result)) {
                return true;
            }
            if (i > 0) {
                result.remove(coin); // geri al
            }
        }

        return false;
    }

    public void giveChange(Map<Coin, Integer> change) {
        for (Map.Entry<Coin, Integer> entry : change.entrySet()) {
            Coin coin = entry.getKey();
            int newCount = coinInventory.get(coin) - entry.getValue();
            coinInventory.put(coin, newCount);
        }
    }

    public Map<Coin, Integer> getCoinInventory() {
        return coinInventory;
    }

    public Optional<String> suggestAlternative(double amount) {
        for (double delta = 0.05; delta <= 1.00; delta += 0.05) {
            double suggestedAmount = amount + delta;
            if (canGiveChange(suggestedAmount)) {
                return Optional.of(String.format("%.2f TL'niz var mıydı?", delta));
            }
        }
        return Optional.empty();
    }

    public void initializeDefaultCoins(int number) {
        for (Coin coin : Coin.values()) {
            coinInventory.put(coin, number);
        }
    }

    public int getCoinCount(Coin coin) {
        return coinInventory.getOrDefault(coin, 0);
    }
}
