package ui;

import javax.swing.*;
import java.awt.*;

public class ProgressDialog extends JDialog {
    public JProgressBar pb;

    public ProgressDialog(String text) {
        super(MainForm.frame, text, true);

        pb = new JProgressBar(0, 500);
        add(BorderLayout.CENTER, pb);
        add(BorderLayout.NORTH, new JLabel("Progress..."));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setSize(300, 75);
        setLocationRelativeTo(MainForm.frame);
    }
}
