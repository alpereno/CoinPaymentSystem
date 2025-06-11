package coinpaymentsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.Map;

public class PaymentUI extends JFrame {

    private final CashRegister register;
    private final PaymentProcessor processor;

    private final JTextField priceField = new JTextField(10);
    private final Map<Coin, JTextField> coinFields = new EnumMap<>(Coin.class);
    private final JTextArea outputArea = new JTextArea(10, 40);

    public PaymentUI() {
        register = new CashRegister();
        register.initializeDefaultCoins(3);
        processor = new PaymentProcessor(register);

        setTitle("Bozuk Para Ödeme Sistemi");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Ödeme Bilgileri"));

        inputPanel.add(new JLabel("Ürün Fiyatı (TL):"));
        inputPanel.add(priceField);

        for (Coin coin : Coin.values()) {
            JTextField coinField = new JTextField("0");
            coinFields.put(coin, coinField);
            inputPanel.add(new JLabel(coin.name() + " adedi:"));
            inputPanel.add(coinField);
        }

        JButton payButton = new JButton("Ödeme Yap");
        payButton.addActionListener(this::handlePayment);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        add(inputPanel, BorderLayout.NORTH);
        add(payButton, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
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

            // Konsol yerine outputArea’ya yaz.
            outputArea.setText(""); // temizle
            ConsoleCapturer capturer = new ConsoleCapturer(outputArea);
            capturer.capture(() -> processor.processPayment(price, payment));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı girin!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PaymentUI::new);
    }
}
