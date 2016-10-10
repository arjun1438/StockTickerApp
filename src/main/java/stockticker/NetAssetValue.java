package stockticker;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetAssetValue {

    private StockPriceFetcher stockPriceFetcher;

    public NetAssetValue(StockPriceFetcher stockPriceFetcher) {
        this.stockPriceFetcher = stockPriceFetcher;
    }

    int computeNetAssetValueInCents(int price, int quantity) {
        if(quantity < 0) return 0;

        return price * quantity;
    }

    int getStockPrice(String ticker) throws ConnectException{
       return stockPriceFetcher.getPrice(ticker);
    }

    public Map<String, Object> computerShareValues(Map<String, Integer> tickersAndNumberOfShares)
    {
        int stockPrice;
        int totalNetAssetValue = 0;
        int netStockValue;
        String ticker = " ";
        List<String> invalidTickerList = new ArrayList<>();
        List<String> NetworkFailureTickerList = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        Map.Entry<String, Integer> stockMap;

        for (Map.Entry<String, Integer> stringIntegerEntry : tickersAndNumberOfShares.entrySet()) {
            try {
                stockMap = stringIntegerEntry;
                ticker = stockMap.getKey();
                stockPrice = getStockPrice(stockMap.getKey());   
                
                netStockValue = computeNetAssetValueInCents(stockPrice, stockMap.getValue());
                totalNetAssetValue += netStockValue;
                result.put(stockMap.getKey(), netStockValue);
            }
            catch (RuntimeException ex)
            {
                invalidTickerList.add(ticker);
            }
            catch (ConnectException ex)
            {
                NetworkFailureTickerList.add(ticker);
            }
        }

        result.put("TotalNetAssetValue", totalNetAssetValue);
        result.put("Tickers With Network Failure", NetworkFailureTickerList);
        result.put("Invalid Ticker Symbols", invalidTickerList);
        return result;
    }
}