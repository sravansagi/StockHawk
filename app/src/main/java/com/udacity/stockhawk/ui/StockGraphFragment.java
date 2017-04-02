package com.udacity.stockhawk.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.List;


import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_HISTORY;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockGraphFragment extends Fragment {

    private Uri stockUri;
    private String stockName;
    public StockGraphFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock_graph, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getActivity().getIntent();
        if ( intent!= null && intent.hasExtra(Intent.EXTRA_TEXT)){

            LineChart lineChart = (LineChart) rootView.findViewById(R.id.lineChart);
            stockName = intent.getStringExtra(Intent.EXTRA_TEXT);
            stockUri = Contract.Quote.makeUriForStock(stockName);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(stockName + " Historical data");
            ContentResolver contentResolver = getActivity().getContentResolver();
            Cursor cursor = contentResolver.query(stockUri,
                    new String[]{COLUMN_HISTORY},
                    null,
                    null,
                    null);
            if (cursor == null){
                lineChart.setNoDataText("An error has been encountered while getting the data. Please try again");
            } else if(cursor.getCount() == 0 ){
                lineChart.setNoDataText("There is not data available for the given stock");
                cursor.close();
            }else {
                cursor.moveToFirst();
                String historicData = cursor.getString(0);
                String[] historicDataDateWise = historicData.split("\n");
                int historicDataSamplesLen = historicDataDateWise.length;
                final String[] dates = new String[historicDataSamplesLen];
                float[] values = new float[historicDataSamplesLen];
                for (int i = 0; i < historicDataDateWise.length ; i++) {
                    String[] tempDataStr = historicDataDateWise[i].split("::");
                    dates[i] = tempDataStr[0];
                    values[i] = Float.parseFloat(tempDataStr[1]);
                }
                List<Entry> entryList = new ArrayList<>();
                for(int i=0; i<values.length; i++)
                {
                    entryList.add(new Entry(((float) i), values[values.length-1-i]));
                }
                LineDataSet dataSet = new LineDataSet(entryList, stockName);
                dataSet.setColor(Color.parseColor("#BBDEFB"));
                dataSet.setCircleColor(Color.parseColor("#FF4081"));
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                lineChart.setContentDescription("The graph displaying historic data of "+ stockName + "stock");
                Description description = new Description();
                description.setText(stockName + " History");
                lineChart.setDescription(description);
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return dates[dates.length - (int)value - 1];
                    }
                });
                YAxis yAxisLeft = lineChart.getAxis(YAxis.AxisDependency.LEFT);
                yAxisLeft.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return value + "$";
                    }
                });
                YAxis yAxisRight = lineChart.getAxis(YAxis.AxisDependency.RIGHT);
                yAxisRight.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return value + "$";
                    }
                });
                lineChart.animateX(1000);
                lineChart.invalidate();
                cursor.close();
            }
        }
        return rootView;
    }
}
