package stockticker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class YahooStockPriceFetcher implements StockPriceFetcher{

    @Override
    public int getPrice(String ticker){

        String url = "http://ichart.finance.yahoo.com/table.csv?s=" + ticker;
        URL  yahooApiUrl = tryURL(url);
        tryOpenUrlConnection(yahooApiUrl);

        Scanner scanner = tryScanner(yahooApiUrl);
        scanner.nextLine();
        String[] tokens = scanner.nextLine().split(",");
        return tryGetStockPriceAsInt(tokens);
    }

    private int tryGetStockPriceAsInt(String[] tokens) throws NumberFormatException
    {
        try {
            return getStockPriceAsInt(tokens);
        }
        catch (NumberFormatException ex)
        {
            throw new NumberFormatException("number format error");
        }
    }

    int getStockPriceAsInt(String[] tokens) throws NumberFormatException
    {
            Double price = Double.parseDouble(tokens[6]) * 100;
            return price.intValue();

    }


    URL getUrl(String yahooApiUrl) throws MalformedURLException
    {
        return new URL(yahooApiUrl);
    }

    private URL tryURL(String yahooApiUrl) throws RuntimeException
    {
        try
        {
            return getUrl(yahooApiUrl);
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }

    void openUrlConnection(URL yahooApiUrl) throws IOException
    {
        yahooApiUrl.openConnection();
    }

    private void tryOpenUrlConnection(URL yahooApiUrl) throws RuntimeException
    {
        try
        {
            openUrlConnection(yahooApiUrl);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }

    Scanner getScanner(URL yahooApiUrl) throws IOException
    {
        return new Scanner(new InputStreamReader(yahooApiUrl.openStream(), StandardCharsets.UTF_8));
    }

    private Scanner tryScanner(URL yahooApiUrl) throws RuntimeException
    {
        try {
            return getScanner(yahooApiUrl);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Invalid ticker symbol");
        }
    }


}
