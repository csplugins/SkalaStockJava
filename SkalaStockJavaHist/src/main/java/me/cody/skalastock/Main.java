package me.cody.skalastock;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.logging.LogManager;

public class Main {
    public static void main(String[] args) throws java.io.IOException {
        LogManager.getLogManager().reset();

        final JFrame frame = new JFrame();
        frame.setTitle("SkalaStock");
        JPanel mainPanel = new JPanel(new BorderLayout(4, 4));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JButton run = new JButton("Run report");
        JPanel labels = new JPanel(new GridLayout(0, 1));
        labels.add(new JLabel("Market cap"));
        labels.add(new JLabel("Year"));
        labels.add(new JLabel("Percent of low"));
        mainPanel.add(labels, BorderLayout.WEST);
        JPanel fields = new JPanel(new GridLayout(0, 1));
        final JTextField mktCapField = new JTextField("1,000,000,000");
        final JTextField yearField = new JTextField("2010");
        final JTextField lowPercentField = new JTextField("20");
        fields.add(mktCapField);
        fields.add(yearField);
        fields.add(lowPercentField);
        mainPanel.add(fields, BorderLayout.CENTER);
        mainPanel.add(run, BorderLayout.SOUTH);
        frame.add(mainPanel);
        ActionListener listener = new ChoiceListener(frame, mktCapField, yearField, lowPercentField);
        run.addActionListener(listener);
        frame.setSize(500, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
