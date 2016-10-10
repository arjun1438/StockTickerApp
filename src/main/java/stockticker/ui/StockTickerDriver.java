package stockticker.ui;

import stockticker.NetAssetValue;
import stockticker.YahooStockPriceFetcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

public class StockTickerDriver {

    private Map<String, Integer> tickersAndNumOfShares;
    private NetAssetValue netAssetValue;

    private StockTickerDriver()
    {
        tickersAndNumOfShares = new HashMap<>();
        YahooStockPriceFetcher yahooStockPriceFetcher = new YahooStockPriceFetcher();
        netAssetValue = new NetAssetValue(yahooStockPriceFetcher);
    }

    private String getInputFileName() throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("Enter the path to input file containing tickers and symbols");
        if(scanner.hasNext()) {
            return scanner.nextLine();
        }
        else
        {
            URL url = this.getClass().getClassLoader().getResource("text.txt");

            if(url != null)
                return url.getPath();
            else
                throw new FileNotFoundException("File not found");
        }
    }

    private Map<String, Integer> readStockDetailsFromFile(String filePath) {
                                                        
        try(Stream<String> lines = Files.lines(Paths.get(filePath)))
        {
            lines.forEach( line -> tickersAndNumOfShares.put(line.split(",")[0], Integer.parseInt(line.split(",")[1] ) ) );
        }
        catch(IOException ex)
        {
          throw new RuntimeException("Error in reading the file");
        }

        return tickersAndNumOfShares;
    }

    private void printResult(Map<String, Integer> tickersAndSymbols) {

        Map<String, Object> result = netAssetValue.computerShareValues(tickersAndSymbols);

        int quantity;
        int pricePerShare;
        int netAssetValueOfOneTicker;
        Object invalidTickerList = null;
        Object networkFailureTickers = null;
        int totalNetAssetValue = 0;

        System.out.format("%n%27s Symbol %1s Quantity %1s Price (USD) %4s NetValue (USD) %n%n", " "," "," "," ");
        for(Map.Entry<String, Object> entry : result.entrySet())
        {
            String tickerSymbol  = entry.getKey();

            if((!Objects.equals(tickerSymbol, "TotalNetAssetValue")) &&
                    (!Objects.equals(tickerSymbol, "Invalid Ticker Symbols")) &&
                    (!Objects.equals(tickerSymbol, "Tickers With Network Failure")))
            {
                quantity = tickersAndSymbols.get(entry.getKey());
                netAssetValueOfOneTicker = (Integer) entry.getValue();
                pricePerShare = (netAssetValueOfOneTicker / quantity);
                System.out.format("%32s %10d %12.2f %20.2f %n", tickerSymbol, quantity, ( pricePerShare / 100.0 ), ( netAssetValueOfOneTicker/ 100.0 ));
            }
            else if(Objects.equals(tickerSymbol, "TotalNetAssetValue"))
            {
                totalNetAssetValue = (Integer) entry.getValue();
            }
            else if(Objects.equals(tickerSymbol, "Invalid Ticker Symbols"))
            {
                invalidTickerList = entry.getValue();
            }
            else if(Objects.equals(tickerSymbol, "Tickers With Network Failure"))
            {
                networkFailureTickers = entry.getValue();
            }

        }

        System.out.format("%n %26s Total Net Asset Value (USD) %21.2f %n"," ", ( totalNetAssetValue / 100.0 ));
        System.out.format("%n %26s List of Invalid Tickers %29s %n"," ", invalidTickerList);
        System.out.format("%27s Tickers with Network Failure %20s"," ", networkFailureTickers);
    }

    public static void main(String args[]) throws IOException
    {
        StockTickerDriver stockTickerDriver = new StockTickerDriver();
        String filePath = stockTickerDriver.getInputFileName();
        System.out.println("File Path entered by user " + filePath);
        Map<String, Integer> tickerandShareQty = stockTickerDriver.readStockDetailsFromFile(filePath);
        stockTickerDriver.printResult(tickerandShareQty);


    }

}
