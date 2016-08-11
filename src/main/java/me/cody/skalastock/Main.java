package me.cody.skalastock;

import org.apache.commons.io.IOUtils;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        labels.add(new JLabel("Dividend"));
        labels.add(new JLabel("Percent of low"));
        mainPanel.add(labels, BorderLayout.WEST);
        JPanel fields = new JPanel(new GridLayout(0, 1));
        final JTextField mktCapField = new JTextField("1,000,000,000");
        final JTextField dividendField = new JTextField("2.5");
        final JTextField lowPercentField = new JTextField("20");
        fields.add(mktCapField);
        fields.add(dividendField);
        fields.add(lowPercentField);
        mainPanel.add(fields, BorderLayout.CENTER);
        mainPanel.add(run, BorderLayout.SOUTH);
        frame.add(mainPanel);
        ActionListener listener = new ChoiceListener(frame, mktCapField, dividendField, lowPercentField);
        run.addActionListener(listener);
        frame.setSize(500, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Stock stock = YahooFinance.get("MMM");
        BigDecimal yearLow = stock.getQuote().getYearLow();
        BigDecimal yearHigh = stock.getQuote().getYearHigh();
        BigDecimal price = stock.getQuote().getPrice();
        BigDecimal change = stock.getQuote().getChangeInPercent();
        BigDecimal peg = stock.getStats().getPeg();
        BigDecimal dividend = stock.getDividend().getAnnualYieldPercent();
    }
}
