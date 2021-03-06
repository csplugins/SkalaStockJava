package me.cody.skalastock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by cody on 8/11/16.
 */
class ChoiceListener implements ActionListener {

    private final JFrame frame;

    private final JTextField mktCapField;
    private final JTextField dividendField;
    private final JTextField lowPercentField;


    ChoiceListener(JFrame parent, JTextField mktCapField, JTextField dividendField, JTextField lowPercentField) {
        this.frame = parent;
        this.mktCapField = mktCapField;
        this.dividendField = dividendField;
        this.lowPercentField = lowPercentField;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<String> symbols = new ArrayList<String>();
        for (int letter = 0; letter < 26; ++letter) {
            String urlString =
                    "http://www.nasdaq.com/screening/companies-by-name.aspx?letter="
                            + (char) (letter + 65) + "&render=download";
            InputStream in = null;
            try {
                in = new URL(urlString).openStream();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            String str = "";
            try {
                str = IOUtils.toString(in, "UTF-8");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            String[] str2 = str.split("\n\"");
            for (int i = 1; i < str2.length; ++i) {
                symbols.add(str2[i].split("\"")[0].replaceAll("[\\s+~]", ""));
            }
        }
        String[] symbolsArray = new String[symbols.size()];
        symbolsArray = symbols.toArray(symbolsArray);

        System.out.println("-----------SKALASTOCK-----------");

        PrintWriter writer = null;
        Date date = Calendar.getInstance().getTime();
        String year = new SimpleDateFormat("yyyy").format(date);
        String month = new SimpleDateFormat("MMM").format(date);
        String day = new SimpleDateFormat("d").format(date);
        try {
            File initDir = new File("SkalaStock");
            if(!initDir.exists()){
                new File("SkalaStock").mkdir();
                File f = new File("SkalaStock/sorttable.js");
                FileUtils.copyInputStreamToFile(ClassLoader.getSystemClassLoader().getResourceAsStream("htmlHelper/sorttable.js"), f);
            }
            File yearFolder = new File("SkalaStock/" + year);
            if(!yearFolder.exists()){
                new File("SkalaStock/" + year).mkdir();
            }
            File monthFolder = new File("SkalaStock/" + year + "/" + month);
            if(!monthFolder.exists()){
                new File("SkalaStock/" + year + "/" + month).mkdir();
            }
            writer = new PrintWriter("SkalaStock/" + year + "/" + month + "/" + day + ".html", "UTF-8");

            String content = "";
            try {
                InputStream fis = ClassLoader.getSystemClassLoader().getResourceAsStream("htmlHelper/htmlStart.txt");
                StringBuilder builder = new StringBuilder();
                int ch;
                while ((ch = fis.read()) != -1) {
                    builder.append((char) ch);
                }
                content = builder.toString();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            writer.println(content);

            writer.println(month + " " + day + " " + year);

            String content2 = "";
            try {
                InputStream fis = ClassLoader.getSystemClassLoader().getResourceAsStream("htmlHelper/htmlMid.txt");
                StringBuilder builder = new StringBuilder();
                int ch;
                while ((ch = fis.read()) != -1) {
                    builder.append((char) ch);
                }
                content2 = builder.toString();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            writer.println(content2);

            int batchSize = 750;
            for (int i = 0; i < symbolsArray.length; i = i + batchSize) {
                String[] batch = Arrays.copyOfRange(symbolsArray, i, Math.min(i + batchSize, symbolsArray.length));
                Map<String, Stock> stocks = null;
                try {
                    stocks = YahooFinance.get(batch);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                for (String s : batch) {
                    //System.out.println(s);
                    if (stocks.get(s) == null) {
                        continue;
                    }
                    if (stocks.get(s).getStats().getPe() == null) {
                        continue;
                    }
                    if (stocks.get(s).getDividend().getAnnualYieldPercent() == null
                            || stocks.get(s).getDividend().getAnnualYieldPercent().compareTo(new BigDecimal(dividendField.getText())) == -1) {
                        continue;
                    }
                    if (stocks.get(s).getStats().getMarketCap().compareTo(new BigDecimal(mktCapField.getText().replaceAll(",", ""))) == -1) {
                        continue;
                    }
                    if (stocks.get(s).getQuote().getPreviousClose() == null) {
                        continue;
                    }
                    BigDecimal yearLow = stocks.get(s).getQuote().getYearLow();
                    BigDecimal yearHigh = stocks.get(s).getQuote().getYearHigh();
                    BigDecimal percent = new BigDecimal(lowPercentField.getText()).multiply(new BigDecimal(0.01));
                    BigDecimal tenPercentLow = yearHigh.subtract(yearLow).multiply(percent).add(yearLow);
                    if (stocks.get(s).getQuote().getPreviousClose().compareTo(tenPercentLow) == 1) {
                        continue;
                    }
                    BigDecimal price = stocks.get(s).getQuote().getPrice();
                    BigDecimal calculatedPercent = yearHigh.subtract(yearLow);
                    calculatedPercent = (price.subtract(yearLow)).divide(calculatedPercent, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2);
                    String mktCapCommas = stocks.get(s).getStats().getMarketCap().toString();
                    for(int k = mktCapCommas.length() - 6; k > 0; k = k - 3){
                        mktCapCommas = mktCapCommas.subSequence(0, k).toString() + "," + mktCapCommas.subSequence(k, mktCapCommas.length());
                    }

                    writer.println("  <tr>");
                    writer.println("    <td><a target=\"_blank\" href=\"http://finance.yahoo.com/quote/" + s + "?p=" + s + "\">" + s + "</a></td>");
                    writer.println("    <td>" + price + "</td>");
                    writer.println("    <td>" + calculatedPercent + "</td>");
                    writer.println("    <td>" + stocks.get(s).getStats().getPe() + "</td>");
                    writer.println("    <td>" + stocks.get(s).getDividend().getAnnualYieldPercent() + "</td>");
                    writer.println("    <td>" + mktCapCommas + "</td>");
                    writer.println("    <td>" + stocks.get(s).getQuote().getYearHigh() + "</td>");
                    writer.println("    <td>" + stocks.get(s).getQuote().getYearLow() + "</td>");
                    writer.println("  </tr>");
                }
            }

            try {
                InputStream fis = ClassLoader.getSystemClassLoader().getResourceAsStream("htmlHelper/htmlEnd.txt");
                StringBuilder builder = new StringBuilder();
                int ch;
                while ((ch = fis.read()) != -1) {
                    builder.append((char) ch);
                }
                writer.println(builder.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

            File htmlFile = new File("SkalaStock/" + year + "/" + month + "/" + day + ".html");
            try {
                Desktop.getDesktop().browse(htmlFile.toURI());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            frame.dispose();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}