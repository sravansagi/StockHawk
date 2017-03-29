package com.udacity.stockhawk.util;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

/**
 * Created by Sravan on 3/29/2017.StockCheckerTask will check if the new stock entered by the user is valid or not
 * The task connects to Yahoo finance API in doInBackground method and retrieve the stock details. If the stock is a valid, then the stock is added
 * stock is added to the data base.
 */

public class StockCheckerTask extends AsyncTask<String,Void,Integer> {
    Context context;
    RefreshSwipe refreshSwipe;
    private final int EMPTYINPUT = 1; // This corresponds to the empty string returned from doInBackground
    private final int STOCKNOTVALID = 2; // This corresponds to the Invalid returned from doInBackground
    private final int STOCKADDED = 3; // This corresponds to the stock added to DB in doInBackground
    private final int CONNECTIONPROBLEM = 4; // This corresponds to problem connecting to Yahoo API in doInBackground

    public StockCheckerTask(Context context, RefreshSwipe refreshSwipe) {
        this.context = context;
        this.refreshSwipe = refreshSwipe;
    }

    @Override
    protected Integer doInBackground(String... params) {
        if (params[0] == null || params[0].length() == 0){
            return EMPTYINPUT;
        }
        String stockName = params[0];
        try {
            Stock stock = YahooFinance.get(stockName);
            if(stock != null && stock.getName() != null){
                int YEARS_OF_HISTORY = 2;
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();
                from.add(Calendar.YEAR, -YEARS_OF_HISTORY);
                StockQuote quote = stock.getQuote();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();

                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                StringBuilder historyBuilder = new StringBuilder();

                for (HistoricalQuote it : history) {
                    historyBuilder.append(it.getDate().getTimeInMillis());
                    historyBuilder.append(", ");
                    historyBuilder.append(it.getClose());
                    historyBuilder.append("\n");
                }
                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, stockName);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                context.getContentResolver()
                        .insert(Contract.Quote.URI,quoteCV);
                PrefUtils.addStock(context, stockName);
                return STOCKADDED;
            }
            else {
                return STOCKNOTVALID;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return CONNECTIONPROBLEM;
        }
    }


    public interface RefreshSwipe{
        void refreshSwipe(boolean status);
    }

    @Override
    protected void onPostExecute(Integer resultInt) {
        refreshSwipe.refreshSwipe(false);
        switch (resultInt){
            case EMPTYINPUT:
                Toast.makeText(context,"No Input was provided.",Toast.LENGTH_SHORT).show();
                break;
            case STOCKNOTVALID:
                Toast.makeText(context,"The stock is not valid. Please enter a valid stock",Toast.LENGTH_SHORT).show();
                break;
            case CONNECTIONPROBLEM:
                Toast.makeText(context,"There was a problem connecting to servers. Please try again",Toast.LENGTH_SHORT).show();
                break;
            case STOCKADDED:
                break;
        }
        /*
        if (resultString)
        Toast.makeText(context,"Add method clicked",Toast.LENGTH_SHORT).show();
        super.onPostExecute(resultString);*/
    }
}
