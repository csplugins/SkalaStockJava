import yahoofinance.YahooFinance;
import yahoofinance.Stock;
import yahoofinance.histquotes.*;
import java.math.*;
import java.util.logging.LogManager;
import java.util.*;
import java.util.List;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.nio.file.*;
import org.apache.commons.io.IOUtils;

public class Main{
  public static void main(String[] args) throws java.io.IOException{
    LogManager.getLogManager().reset();

    final JFrame frame = new JFrame();
    frame.setTitle("SkalaStock");
    JPanel mainPanel = new JPanel(new BorderLayout(4,4));
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    JButton run = new JButton("Run report");
    JPanel labels = new JPanel(new GridLayout(0,1));
    labels.add(new JLabel("Market cap"));
    labels.add(new JLabel("Dividend"));
    labels.add(new JLabel("Percent of low"));
    mainPanel.add(labels, BorderLayout.WEST);
    JPanel fields = new JPanel(new GridLayout(0,1));
    final JTextField mktCapField = new JTextField("1,000,000,000");
    final JTextField dividendField = new JTextField("2.5");
    final JTextField lowPercentField = new JTextField("20");
    fields.add(mktCapField);
    fields.add(dividendField);
    fields.add(lowPercentField);
    mainPanel.add(fields, BorderLayout.CENTER);
    mainPanel.add(run, BorderLayout.SOUTH);
    frame.add(mainPanel);
    ActionListener listener;
    class ChoiceListener implements ActionListener{
      @Override
      public void actionPerformed(ActionEvent e) {
        List<String[]> letterList = new ArrayList<String[]>();
        for(int letter = 0; letter < 26; ++letter){
          List<String> symbols = new ArrayList<String>();
          String urlString =
            "http://www.nasdaq.com/screening/companies-by-name.aspx?letter="
            + (char)(letter+65) + "&render=download";
          InputStream in = null;
          try{
            in = new URL(urlString).openStream();
          }catch(IOException ex){
            ex.printStackTrace();
          }
          String str = "";
          try{
            str = IOUtils.toString(in, "UTF-8");
          }catch(IOException ex){
            ex.printStackTrace();
          }
          String[] str2 = str.split("\n\"");
          for(int i = 1; i < str2.length; ++i){
            String cap = str2[i].split("\"")[6];
            //if(!cap.contains("B")){
              //continue;
            //}
            symbols.add(str2[i].split("\"")[0].replaceAll("[\\s+~]",""));
          }
          String[] symbols2 = new String[symbols.size()];
          symbols2 = symbols.toArray(symbols2);
          letterList.add(symbols2);
        }

        System.out.println("-----------SKALASTOCK-----------");

        PrintWriter writer = null;
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
         try{
           path = URLDecoder.decode(path, "UTF-8");
           path = path.split("Main.jar")[0];
           path = path.replaceFirst("^/(.:/)", "$1");
           System.out.println("\"" + path + "\"");
           writer = new PrintWriter(path + "PeRatiosSorted.html", "UTF-8");
         }catch(IOException ex){
           ex.printStackTrace();
         }
         String content = "";
         try{
           System.out.println(Paths.get(path + "htmlHelper/htmlStart.txt"));
           content = new String(Files.readAllBytes(Paths.get(path + "htmlHelper/htmlStart.txt")));
         }catch(IOException ex){
           ex.printStackTrace();
         }
         writer.println(content);

        for(String[] symbols2 : letterList){
          Map<String, Stock> stocks = null;
          try{
            stocks = YahooFinance.get(symbols2);
          }catch(IOException ex){
            ex.printStackTrace();
          }
          for(String s : symbols2){
            if(stocks.get(s).getStats().getPe() == null){
              continue;
            }
            if(stocks.get(s).getDividend().getAnnualYieldPercent() == null
              || stocks.get(s).getDividend().getAnnualYieldPercent().compareTo(new BigDecimal(dividendField.getText())) == -1){
              continue;
            }
            if(stocks.get(s).getStats().getMarketCap().compareTo(new BigDecimal(mktCapField.getText().replaceAll(",", ""))) == -1){
              continue;
            }
            BigDecimal yearLow = stocks.get(s).getQuote().getYearLow();
            BigDecimal yearHigh = stocks.get(s).getQuote().getYearHigh();
            BigDecimal percent = new BigDecimal(lowPercentField.getText()).multiply(new BigDecimal(0.01));
            BigDecimal tenPercentLow = yearHigh.subtract(yearLow).multiply(percent).add(yearLow);
            if(stocks.get(s).getQuote().getPreviousClose().compareTo(tenPercentLow) == 1){
              continue;
            }

            writer.println("  <tr>");
            writer.println("    <td><a target=\"_blank\" href=\"http://finance.yahoo.com/quote/" + s + "?p=" + s + "\">" + s + "</a></td>");
            writer.println("    <td>" + stocks.get(s).getStats().getPe() + "</td>");
            writer.println("    <td>" + stocks.get(s).getDividend().getAnnualYieldPercent() + "</td>");
            writer.println("    <td>" + stocks.get(s).getStats().getMarketCap() + "</td>");
            writer.println("    <td>" + stocks.get(s).getQuote().getYearHigh() + "</td>");
            writer.println("    <td>" + stocks.get(s).getQuote().getYearLow() + "</td>");
            writer.println("  </tr>");

          }
        }

        try{
          content = new String(Files.readAllBytes(Paths.get(path + "htmlHelper/htmlEnd.txt")));
        }catch(IOException ex){
          ex.printStackTrace();
        }
        writer.println(content);
        writer.close();
        File htmlFile = new File(path + "PeRatiosSorted.html");
        try{
          Desktop.getDesktop().browse(htmlFile.toURI());
        }catch(IOException ex){
          ex.printStackTrace();
        }
        frame.dispose();
      }
    }
    listener = new ChoiceListener();
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
    //stock.print();
    //Stock stock2 = YahooFinance.get("TSLA");
  }
}
