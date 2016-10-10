package stockticker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class YahooStockPriceFetcherTest {

    private YahooStockPriceFetcher yahooStockPriceFetcher;

    @Before
    public void setUp()
    {
        yahooStockPriceFetcher = new YahooStockPriceFetcher();
    }

    @Test
    public void getPrice()
    {
        assertTrue(yahooStockPriceFetcher.getPrice("AAPL") > 0);
    }

    @Test
    public void compareTwoDifferentStocksPrice()
    {
        int googleStockPrice;
        googleStockPrice = yahooStockPriceFetcher.getPrice("GOOG");
        int appleStockPrice = yahooStockPriceFetcher.getPrice("AAPL");

        assertNotEquals(googleStockPrice, appleStockPrice);
    }

    @Test
    public void getStockPriceForInvalidShare()
    {
        try
        {
            yahooStockPriceFetcher.getPrice("Invalid");
        }
        catch (RuntimeException ex)
        {
            assertEquals("Invalid ticker symbol", ex.getMessage());
        }
    }

    @Test
    public void checkUrlfailure() throws MalformedURLException {

        YahooStockPriceFetcher yahooStockPriceFetcher = Mockito.mock(YahooStockPriceFetcher.class);
        when(yahooStockPriceFetcher.getUrl(anyString())).thenThrow(new MalformedURLException("Invalid hostname"));
        when(yahooStockPriceFetcher.getPrice(anyString())).thenCallRealMethod();

        try
        {
            yahooStockPriceFetcher.getPrice("GOOG");
            fail("Expected Exception for url format error");
        }
        catch (RuntimeException ex)
        {
            assertEquals("Invalid hostname", ex.getMessage());
        }
    }

    @Test
    public void checkOpenConnectionFailure()
    {
        YahooStockPriceFetcher yahooStockPriceFetcher = new YahooStockPriceFetcher() {

            @Override
            void openUrlConnection(URL yahooApiUrl) throws IOException
            {
                throw new IOException("Unable to open connection");
            }
        };

        try
        {
            yahooStockPriceFetcher.getPrice("AAPL");
            fail("Expected Exception for network connection");

        }
        catch(RuntimeException ex)
        {
            assertEquals("Unable to open connection", ex.getMessage());
        }
    }

    @Test
    public void checkScannerFailure()
    {
        YahooStockPriceFetcher yahooStockPriceFetcher = new YahooStockPriceFetcher() {

            @Override
            Scanner getScanner(URL yahooApiUrl) throws IOException
            {
                throw new IOException("unable to read the input stream");
            }
        };

        try
        {
            yahooStockPriceFetcher.getPrice("Invalid");
            fail("Expected Exception for scan error");

        }
        catch(RuntimeException ex)
        {
            assertEquals("Invalid ticker symbol", ex.getMessage());
        }
    }

    @Test
    public void checkGetStockPriceAsIntFailure()
    {
        YahooStockPriceFetcher yahooStockPriceFetcher = new YahooStockPriceFetcher()
        {
            @Override
            int getStockPriceAsInt(String[] tokens) throws NumberFormatException
            {
                throw new NumberFormatException("parsing failed");
            }

        };

        try
        {
            yahooStockPriceFetcher.getPrice("AAPL");
        }
        catch (NumberFormatException ex)
        {
            assertEquals("number format error", ex.getMessage());
        }
    }

}