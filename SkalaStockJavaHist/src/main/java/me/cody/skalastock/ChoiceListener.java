package me.cody.skalastock;

import com.sun.org.apache.xpath.internal.SourceTree;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
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
    private final JTextField yearField;
    private final JTextField lowPercentField;


    ChoiceListener(JFrame parent, JTextField mktCapField, JTextField yearField, JTextField lowPercentField) {
        this.frame = parent;
        this.mktCapField = mktCapField;
        this.yearField = yearField;
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
        String currentYear = new SimpleDateFormat("yyyy").format(date);
        int targetYear = Integer.parseInt(yearField.getText());

        try {
            File initDir = new File("SkalaStock");
            if(!initDir.exists()){
                new File("SkalaStock").mkdir();
                File f = new File("SkalaStock/sorttable.js");
                FileUtils.copyInputStreamToFile(ClassLoader.getSystemClassLoader().getResourceAsStream("htmlHelper/sorttable.js"), f);
            }
            File historyFolder = new File("SkalaStock/History");
            if(!historyFolder.exists()){
                new File("SkalaStock/History").mkdir();
            }
            writer = new PrintWriter("SkalaStock/History/" + targetYear + ".html", "UTF-8");

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

            writer.println(targetYear);

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
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, targetYear - 1);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Calendar cal2 = Calendar.getInstance();
            cal2.set(Calendar.YEAR, targetYear);
            cal2.set(Calendar.MONTH, Calendar.JANUARY);
            cal2.set(Calendar.DAY_OF_MONTH, 1);
            for (int i = 0; i < symbolsArray.length; i = i + batchSize) {
                String[] batch = Arrays.copyOfRange(symbolsArray, i, Math.min(i + batchSize, symbolsArray.length));
                Map<String, Stock> stocks = null;
                try {
                    stocks = YahooFinance.get(batch);
                } catch (IOException ex) {
                    continue;
                    //ex.printStackTrace();
                }
                for (String s : batch) {
                    //System.out.println(s);
                    if (stocks.get(s) == null) {
                        System.out.println("Problem loading stock " + s);
                        continue;
                    }
                    if(stocks.get(s).getStats().getMarketCap() == null){
                        continue;
                    }
                    if (stocks.get(s).getStats().getMarketCap().compareTo(new BigDecimal(mktCapField.getText().replaceAll(",", ""))) == -1) {
                        continue;
                    }

                    List<HistoricalQuote> histQuotes = null;
                    try{
                        histQuotes = stocks.get(s).getHistory(cal, cal2, Interval.DAILY);
                    }catch(FileNotFoundException e1){
                        continue;
                    }catch(SocketTimeoutException e1){
                        System.out.println("Error fetching history for " + s + ". SocketTimeoutException");
                        continue;
                    }
                    if(histQuotes.isEmpty()){
                        continue;
                    }
                    if(histQuotes.get(0).getClose() == null){
                        continue;
                    }
                    BigDecimal yearLow = histQuotes.get(0).getLow();
                    BigDecimal yearHigh = histQuotes.get(0).getHigh();

                    for(int k = 1; k < histQuotes.size(); ++k){
                        if(yearLow == null || yearLow.compareTo(histQuotes.get(0).getLow()) == 1){
                            yearLow = histQuotes.get(0).getLow();
                        }
                        if(yearHigh == null || yearHigh.compareTo(histQuotes.get(0).getHigh()) == -1){
                            yearLow = histQuotes.get(0).getLow();
                        }
                    }
                    if(yearHigh == null || yearLow == null){
                        continue;
                    }
                    BigDecimal percent = new BigDecimal(lowPercentField.getText()).multiply(new BigDecimal(0.01));
                    BigDecimal tenPercentLow = yearHigh.subtract(yearLow).multiply(percent).add(yearLow);
                    if (histQuotes.get(0).getClose().compareTo(tenPercentLow) == 1) {
                        continue;
                    }

                    Document doc = null;
                    try {
                        //System.out.println(s);
                        doc = Jsoup.connect("http://financials.morningstar.com/valuate/valuation-history.action?&t=" + s + "&region=usa&culture=en-US&cur=&type=price-earnings").get();
                    } catch (IOException e1) {
                        //e1.printStackTrace();
                        continue;
                    }
                    Elements yearsFound = doc.select("#valuation_history_table tr:nth-child(2) td");
                    if(Integer.parseInt(currentYear) - targetYear + 2 > yearsFound.size()){
                        continue;
                    }
                    String pe = yearsFound.get(yearsFound.size() - (Integer.parseInt(currentYear) - targetYear) - 2).text();
                    //System.out.println(pe);
                    if(pe.equals("—")){
                        continue;
                    }

                    BigDecimal price = histQuotes.get(0).getClose();
                    BigDecimal calculatedPercent = yearHigh.subtract(yearLow);
                    if(calculatedPercent.compareTo(new BigDecimal(0)) == 0){
                        continue;
                    }
                    calculatedPercent = (price.subtract(yearLow)).divide(calculatedPercent, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2);
                    String mktCapCommas = stocks.get(s).getStats().getMarketCap().toString();
                    for(int k = mktCapCommas.length() - 6; k > 0; k = k - 3){
                        mktCapCommas = mktCapCommas.subSequence(0, k).toString() + "," + mktCapCommas.subSequence(k, mktCapCommas.length());
                    }

                    writer.println("  <tr>");
                    writer.println("    <td><a target=\"_blank\" href=\"http://finance.yahoo.com/quote/" + s + "?p=" + s + "\">" + s + "</a></td>");
                    writer.println("    <td>" + price + "</td>");
                    writer.println("    <td>" + calculatedPercent + "</td>");
                    writer.println("    <td>" + pe/*stocks.get(s).getStats().getPe()*/ + "</td>");
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

            File htmlFile = new File("SkalaStock/History/" + targetYear + ".html");
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