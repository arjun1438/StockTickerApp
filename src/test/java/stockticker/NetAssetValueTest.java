package stockticker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class NetAssetValueTest {
    private NetAssetValue netAssetValue;
    private StockPriceFetcher stockPriceFetcher;
    private Map<String, Integer> tickersAndNumberOfShares;

    @Test
    public void canary()
    {
        assertTrue(true);
    }

    @Before
    public void setup(){
        stockPriceFetcher = Mockito.mock(StockPriceFetcher.class);

        netAssetValue = new NetAssetValue(stockPriceFetcher);

        tickersAndNumberOfShares = new HashMap<>();
    }

    @Test
    public void computeNetAssetValueForZeroQuantity()
    {
        assertEquals(0, netAssetValue.computeNetAssetValueInCents(10000000, 0));
    }

    @Test
    public void computeNetAssetValueForLowerQuantity()
    {
        assertEquals(100000000, netAssetValue.computeNetAssetValueInCents(10000, 10000));

    }

    @Test
    public void computeNetAssetValueForHigherQuantity()
    {
        assertEquals(100000000, netAssetValue.computeNetAssetValueInCents(10000, 10000));

    }

    @Test
    public void computerNetAssetValueForNegativeQuantity()
    {
        assertEquals(0, netAssetValue.computeNetAssetValueInCents(10000000, -1000000));
    }

    @Test
    public void computeNetAssetValueForSharePriceWithCents()
    {
        assertEquals(3125815, netAssetValue.computeNetAssetValueInCents(12355, 253));

    }

    @Test
    public void getStockPriceForGoogle() throws ConnectException
    {
            when(stockPriceFetcher.getPrice("GOOGL")).thenReturn(81496);
            assertEquals(81496, netAssetValue.getStockPrice("GOOGL"));
    }

    @Test
    public void getStockPriceForApple() throws ConnectException {
            when(stockPriceFetcher.getPrice("AAPL")).thenReturn(2048);
            assertEquals(2048, netAssetValue.getStockPrice("AAPL"));
    }

    @Test
    public void getStockPriceForInvalidTicker() throws ConnectException {
        {
            when(stockPriceFetcher.getPrice("INV")).thenThrow(new RuntimeException("Invalid ticker symbol"));

            try {
                netAssetValue.getStockPrice("INV");
                fail("Expected exception: Invalid ticker symbol");
            } catch (RuntimeException ex) {
                assertEquals("Invalid ticker symbol", ex.getMessage());
            }
        }
    }

    @Test
    public void getStockPriceNetworkfailure()
    {
        try
        {
            when(stockPriceFetcher.getPrice("AAPL")).thenThrow(new ConnectException("Network failure"));
            netAssetValue.getStockPrice("AAPL");
            fail("Expected exception: Network failure");
        }
        catch(ConnectException ex)
        {
            assertEquals("Network failure", ex.getMessage());
        }
    }

    @Test
    public void computeValuesOfSharesHeldByUserCallsGetStockPrice() throws ConnectException
    {


        tickersAndNumberOfShares.put("AAPL", 1000);
        when(netAssetValue.getStockPrice("AAPL")).thenReturn(10);

        Map<String,Object> result = netAssetValue.computerShareValues(tickersAndNumberOfShares);

        assertEquals(10000, result.get("AAPL"));
        assertEquals( 10000, result.get("TotalNetAssetValue"));
    }

    @Test
    public void computeValuesForTwoShares() throws ConnectException {


        tickersAndNumberOfShares.put("GOOGL", 2000);

        when(netAssetValue.getStockPrice("GOOGL")).thenReturn(4);

        Map<String, Object>  result = netAssetValue.computerShareValues(tickersAndNumberOfShares);

        assertEquals(8000, result.get("GOOGL"));

        assertEquals(8000, result.get("TotalNetAssetValue"));
    }

    @Test
    public void computerValueOfSharesHeldByUserWithOneInvalidTicker() throws ConnectException {

        List<String> expectedInvalidTickerList = Arrays.asList("INV", "INV1");

        tickersAndNumberOfShares.put("GOOGL", 2000);
        tickersAndNumberOfShares.put("AAPL", 1000);
        tickersAndNumberOfShares.put("INV", 1000);
        tickersAndNumberOfShares.put("INV1", 1000);

        when(netAssetValue.getStockPrice("GOOGL")).thenReturn(4);
        when(netAssetValue.getStockPrice("AAPL")).thenReturn(7);
        when(netAssetValue.getStockPrice("INV")).thenThrow(new RuntimeException("Imvalid ticker Symbol"));
        when(netAssetValue.getStockPrice("INV1")).thenThrow(new RuntimeException("Imvalid ticker Symbol"));
        Map<String, Object>  result = netAssetValue.computerShareValues(tickersAndNumberOfShares);

        assertEquals(8000, result.get("GOOGL"));
        assertEquals(7000, result.get("AAPL"));

        assertEquals(expectedInvalidTickerList, result.get("Invalid Ticker Symbols"));

        assertEquals(15000, result.get("TotalNetAssetValue"));
    }


    @Test
    public void computerValueOfSharesHeldByUserWithNetworkFailure() throws ConnectException {

        List<String> expectedNetworkFailureList = Arrays.asList("GOOGL", "AAPL");

        tickersAndNumberOfShares.put("GOOGL", 2000);
        tickersAndNumberOfShares.put("YHOO", 2000);
        tickersAndNumberOfShares.put("AAPL", 1000);

        when(netAssetValue.getStockPrice("GOOGL")).thenThrow(new ConnectException("Network Failure"));
        when(netAssetValue.getStockPrice("YHOO")).thenReturn(6);
        when(netAssetValue.getStockPrice("AAPL")).thenThrow(new ConnectException("Network Failure"));

        Map<String, Object> result = netAssetValue.computerShareValues(tickersAndNumberOfShares);

        assertEquals(12000, result.get("YHOO"));
        assertEquals(expectedNetworkFailureList, result.get("Tickers With Network Failure"));
    }
}