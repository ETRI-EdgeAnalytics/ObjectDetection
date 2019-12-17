package com.etri.lightnetwork;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReportActivity extends AppCompatActivity {
    @BindView(R.id.chart_compare)
    BarChart chartCompare;

    @BindView(R.id.chart_mobilenet)
    BarChart mobilenetCompare;

    @BindView(R.id.tv_mobilenet_perf)
    TextView mobilenetTv;

    @BindView(R.id.chart_inceptionv3)
    BarChart inceptionCompare;

    @BindView(R.id.tv_inception_perf)
    TextView inceptionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);

        //ArrayList<Integer> mobilePerformance = SystemInfo.getInstance(getApplicationContext()).getPrformanceResult(0);
        //ArrayList<Integer> inceptionPerformance = SystemInfo.getInstance(getApplicationContext()).getPrformanceResult(1);


        initCompareChart( );
        initBarChart(mobilenetCompare, 0, mobilenetTv);
        initBarChart(inceptionCompare, 1, inceptionTv);
    }

    @OnClick(R.id.btn_back) void onClickBack() {
        finish();
    }
    void initCompareChart() {
        String[] labels = {"MobileNet", "InceptionV3"};
        chartCompare.setDrawBarShadow(false);
        chartCompare.setDrawValueAboveBar(true);
        chartCompare.getDescription().setEnabled(false);
        chartCompare.setMaxVisibleValueCount(60);
        chartCompare.setPinchZoom(false);
        chartCompare.setDrawGridBackground(false);
        chartCompare.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartCompare.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartCompare.getAxisRight().setDrawAxisLine(false);
        chartCompare.getAxisRight().setDrawGridLines(false);
        chartCompare.getXAxis().setGranularityEnabled(true);
        chartCompare.getXAxis().setDrawAxisLine(false);
        chartCompare.getXAxis().setDrawGridLines(false);
        chartCompare.getAxisLeft().setAxisMinimum(0);
        setComapreData(2);
    }

    private void setComapreData(int modelCount) {

        int tmpTotal = 0;
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for(int i = 0; i<modelCount; i++) {
            ArrayList<Integer> performance = SystemInfo.getInstance(getApplicationContext()).getPrformanceResult(i);
            if(performance.size() != 0) {
                for(int temp:performance) {
                    tmpTotal += temp;
                }

                BarEntry entry = new BarEntry(i, (float)tmpTotal/performance.size());
                yVals1.add(entry);
            }
        }

        BarDataSet set1;

        if (chartCompare.getData() != null &&
                chartCompare.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chartCompare.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            chartCompare.getData().notifyDataChanged();
            chartCompare.notifyDataSetChanged();
        } else {

            if(yVals1.size() > 0) {
                set1 = new BarDataSet(yVals1, "Millisecond");

                set1.setDrawIcons(false);

                set1.setColors (new int[]{0xff0066a0, 0xfff15922});
                ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
                dataSets.add(set1);

                BarData data = new BarData(dataSets);

                data.setValueTextSize(10f);
                data.setBarWidth(0.5f);

                chartCompare.setData(data);
            }

        }
    }

    void initBarChart(BarChart chart, int modelIdx, TextView outputView) {

        int tmpTotal = 0;
        int min = 10000;
        int max = 0;
        int i = 0;

        //String[] labels = {"MobileNet", "InceptionV3"};
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setMaxVisibleValueCount(60);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        //chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);

        //chart.getXAxis().setAxisMinimum(-0.5f);


        chart.getXAxis().setDrawAxisLine(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setAxisMinimum(0);

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<Integer> performance = SystemInfo.getInstance(getApplicationContext()).getPrformanceResult(modelIdx);

        String[] labels = new String[performance.size()];
        for(int temp:performance) {
            tmpTotal += temp;
            if(min > temp) {
                min = temp;
            }

            if(max < temp) {
                max = temp;
            }

            labels[i] = String.format("%d", i);
            BarEntry entry = new BarEntry(i, temp);
            yVals1.add(entry);

            i++;

        }
        chart.getXAxis().setLabelCount(labels.length);
        if(performance.size()>0) {
            outputView.setText(String.format("Min:%d Max:%d Avg:%d", min, max, (int)tmpTotal/performance.size()));
        } else {
            outputView.setText("");
        }

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        BarDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {

            if(yVals1.size() > 0) {
                set1 = new BarDataSet(yVals1, "Millisecond");

                set1.setDrawIcons(false);

                set1.setColor (0xff0066a0);
                ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
                dataSets.add(set1);

                BarData data = new BarData(dataSets);

                data.setValueTextSize(10f);
                data.setBarWidth(0.5f);

                chart.setData(data);
            }

        }
    }

   
}
