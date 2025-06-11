package coinpaymentsystem;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ConsoleCapturer {
    private final JTextArea textArea;

    public ConsoleCapturer(JTextArea textArea) {
        this.textArea = textArea;
    }

    public void capture(Runnable task) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(baos);

        System.setOut(newOut);
        task.run();
        System.out.flush();
        System.setOut(originalOut);

        textArea.append(baos.toString());
    }
}
