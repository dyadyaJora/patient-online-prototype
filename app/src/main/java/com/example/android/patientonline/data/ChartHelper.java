package com.example.android.patientonline.data;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

public class ChartHelper implements OnChartValueSelectedListener {
    private LineChart mChart;
    private Context mContext;

    private ArrayList<String> xValues;
    public ChartHelper(LineChart mChart, Context context) {
        this.mChart = mChart;
        this.mContext = context;
        this.xValues = new ArrayList<>();
    }

    public void initialize() {
        mChart.getDescription().setEnabled(false);
        mChart.setScaleEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);
        mChart.setDrawGridBackground(false);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.getLegend().setEnabled(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.setData(new LineData());
        mChart.setOnChartValueSelectedListener(this);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setEnabled(false);

        YAxis yAxisLeft = mChart.getAxisLeft();
        yAxisLeft.setTextColor(Color.WHITE);
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setDrawAxisLine(false);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Y");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setFillColor(Color.RED);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setLineWidth(2f);
        set.setFillAlpha(0);
        set.setDrawFilled(true);

        return set;
    }

    public void addEntry(float yValue) {
        ILineDataSet set = null;
        xValues.add("x");

        LineData data = mChart.getData();

        if (data == null ) {
            data = new LineData();
        }
        set = data.getDataSetByIndex(0);

        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }

        data.addEntry(new Entry(data.getDataSetByIndex(0).getEntryCount(), yValue), 0);
        //data.notifyDataChanged();
        mChart.setData(data);
        mChart.notifyDataSetChanged();
        mChart.setVisibleXRangeMaximum(400);
        mChart.moveViewToX(data.getEntryCount());
    }

    public void clearChart() {
        mChart.clear();
    }
}
