package coinpaymentsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.Map;

public class PaymentUI extends JFrame {

    private CashRegister register;
    private PaymentProcessor processor;

    private final JTextField defaultCountField = new JTextField("3");
    private final JButton initializeButton = new JButton("Kasayı Başlat");

    private final Map<Coin, JTextField> customInitFields = new EnumMap<>(Coin.class);

    private final JTextField priceField = new JTextField(10);
    private final Map<Coin, JTextField> coinFields = new EnumMap<>(Coin.class);
    private final JButton payButton = new JButton("Ödeme Yap");

    private final JTextArea outputArea = new JTextArea(10, 40);

    private final Map<Coin, JLabel> coinCountLabels = new EnumMap<>(Coin.class);
    private final Map<Coin, JLabel> coinTotalLabels = new EnumMap<>(Coin.class);
    private final JLabel totalLabel = new JLabel("Toplam: 0.00 TL");

    public PaymentUI() {
        setTitle("Para Ödeme Sistemi");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(900, 850));

        JPanel setupPanel = new JPanel();
        setupPanel.setLayout(new BoxLayout(setupPanel, BoxLayout.Y_AXIS));
        setupPanel.setBorder(BorderFactory.createTitledBorder("Kasa Başlatma"));

        JPanel defaultInitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        defaultInitPanel.add(new JLabel("Tüm para türleri için başlangıç adedi:"));
        defaultInitPanel.add(defaultCountField);
        defaultInitPanel.add(initializeButton);

        setupPanel.add(defaultInitPanel);

        JPanel customGroupPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JPanel kurusPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        JPanel tlPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        for (Coin coin : Coin.values()) {
            JTextField field = new JTextField("0");
            customInitFields.put(coin, field);
            if (coin.getValue() < 1.0) {
                kurusPanel.add(new JLabel(coin.name() + ":"));
                kurusPanel.add(field);
            } else {
                tlPanel.add(new JLabel(coin.name() + ":"));
                tlPanel.add(field);
            }
        }

        customGroupPanel.add(kurusPanel);
        customGroupPanel.add(tlPanel);

        setupPanel.add(new JLabel("Veya her bir para türü için başlangıç adedi girin:"));
        setupPanel.add(customGroupPanel);

        initializeButton.addActionListener(this::initializeCashRegister);

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Ödeme Bilgileri"));
        inputPanel.add(new JLabel("Ürün Fiyatı (TL):"));
        inputPanel.add(priceField);

        for (Coin coin : Coin.values()) {
            JTextField field = new JTextField("0");
            coinFields.put(coin, field);
            inputPanel.add(new JLabel(coin.name() + " adedi:"));
            inputPanel.add(field);
        }

        payButton.addActionListener(this::handlePayment);
        payButton.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Kasa Durumu"));

        for (Coin coin : Coin.values()) {
            JLabel countLabel = new JLabel();
            JLabel totalCoinLabel = new JLabel();
            coinCountLabels.put(coin, countLabel);
            coinTotalLabels.put(coin, totalCoinLabel);

            JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            line.add(new JLabel(coin.name() + ": "));
            line.add(countLabel);
            line.add(new JLabel(" | Toplam: "));
            line.add(totalCoinLabel);
            inventoryPanel.add(line);
        }

        inventoryPanel.add(Box.createVerticalStrut(10));
        inventoryPanel.add(totalLabel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(inputPanel, BorderLayout.CENTER);
        centerPanel.add(inventoryPanel, BorderLayout.EAST);

        add(setupPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(payButton, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeCashRegister(ActionEvent e) {
        register = new CashRegister();

        boolean customUsed = false;
        Map<Coin, Integer> initMap = new EnumMap<>(Coin.class);

        for (Coin coin : Coin.values()) {
            String text = customInitFields.get(coin).getText().trim();
            int value = 0;
            if (!text.isEmpty()) {
                try {
                    value = Integer.parseInt(text);
                    if (value < 0) throw new NumberFormatException();
                    if (value > 0) customUsed = true;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, coin.name() + " için geçersiz giriş!", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            initMap.put(coin, value);
        }

        if (customUsed) {
            register.addCoins(initMap);
            outputArea.setText("Kasa elle belirtilen adetlerle başlatıldı.\n");
        } else {
            try {
                int defaultCount = Integer.parseInt(defaultCountField.getText().trim());
                if (defaultCount < 0) throw new NumberFormatException();
                register.initializeDefaultCoins(defaultCount);
                outputArea.setText("Kasa tüm para türleri için " + defaultCount + " adet ile başlatıldı.\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Geçerli bir varsayılan adet girin!", "Hatalı Giriş", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        processor = new PaymentProcessor(register);
        payButton.setEnabled(true);
        initializeButton.setEnabled(false);
        defaultCountField.setEditable(false);
        for (JTextField field : customInitFields.values()) {
            field.setEditable(false);
        }

        updateInventoryDisplay();
    }

    private void handlePayment(ActionEvent e) {
        try {
            double price = Double.parseDouble(priceField.getText().replace(",", "."));
            Map<Coin, Integer> payment = new EnumMap<>(Coin.class);

            for (Coin coin : Coin.values()) {
                String text = coinFields.get(coin).getText().trim();
                int count = text.isEmpty() ? 0 : Integer.parseInt(text);
                if (count > 0) {
                    payment.put(coin, count);
                }
            }

            outputArea.setText("");
            ConsoleCapturer capturer = new ConsoleCapturer(outputArea);
            capturer.capture(() -> processor.processPayment(price, payment));

            updateInventoryDisplay();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen geçerli bir ürün fiyatı ve adet bilgisi girin!",
                    "Giriş Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateInventoryDisplay() {
        double total = 0.0;

        for (Coin coin : Coin.values()) {
            int count = register.getCoinCount(coin);
            double subtotal = count * coin.getValue();

            coinCountLabels.get(coin).setText(count + " adet");
            coinTotalLabels.get(coin).setText(String.format("%.2f TL", subtotal));
            total += subtotal;
        }

        totalLabel.setText(String.format("Toplam: %.2f TL", total));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PaymentUI ui = new PaymentUI();
            ui.setVisible(true);
        });
    }
}
