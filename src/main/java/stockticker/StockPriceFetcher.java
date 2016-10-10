package stockticker;

import java.net.ConnectException;

interface StockPriceFetcher {
    int getPrice(String ticker) throws ConnectException;
}
