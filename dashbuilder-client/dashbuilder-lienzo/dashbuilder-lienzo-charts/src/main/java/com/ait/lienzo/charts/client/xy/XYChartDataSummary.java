package com.ait.lienzo.charts.client.xy;

import com.ait.lienzo.charts.client.model.DataTable;
import com.ait.lienzo.charts.client.model.DataTableColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class XYChartDataSummary {
    private XYChartData data;

    private double maxNumericValue;
    private Date maxDateValue;
    private double minNumericValue;
    private Date minDateValue;
    private int numSeries;
    private List<String> addedSeries = new ArrayList();
    private List<String> removedSeries = new ArrayList();

    public XYChartDataSummary(XYChartData data) {
        this.data = data;
        build();
    }

    public XYChartDataSummary(XYChartData data, Collection<String> currentSerieNames) {
        // Build the summary.
        this(data);

        if (data != null && currentSerieNames != null) {
            // Chech added or removed series from last chart data.
            XYChartSerie[] newSeries = data.getSeries();
            for (XYChartSerie newSerie : newSeries) {
                if (!currentSerieNames.contains(newSerie.getName())) addedSeries.add(newSerie.getName());
            }
            for (String oldSerieName : currentSerieNames) {
                if (isSerieRemoved(newSeries, oldSerieName)) removedSeries.add(oldSerieName);
            }
        }
    }

    private boolean isSerieRemoved(XYChartSerie[] series, String serieName) {
        if (serieName == null || series == null) return false;

        for (XYChartSerie _serie : series) {
            if (_serie.getName().equals(serieName)) return false;
        }

        return true;
    }

    public void build() {
        if (data == null) return;

        final XYChartSerie[] series = data.getSeries();
        this.numSeries = series.length;
        for (int i = 0; i < series.length; i++)
        {
            XYChartSerie serie = series[i];
            String categoryAxisProperty = data.getCategoryAxisProperty();
            String valuesAxisProperty = serie.getValuesAxisProperty();

            DataTable dataTable = data.getDataTable();
            DataTableColumn categoryColumn = dataTable.getColumn(categoryAxisProperty);
            DataTableColumn valuesColumn = dataTable.getColumn(valuesAxisProperty);
            DataTableColumn.DataTableColumnType valuesColumnType = valuesColumn.getType();

            Object[] values = null;
            switch (valuesColumnType) {

                case NUMBER:
                    values = valuesColumn.getNumericValues();
                    if (values != null && values.length > 0) {
                        for (int j = 0; j < values.length; j++) {
                            Double value = (Double) values[j];
                            if (value >= maxNumericValue) maxNumericValue = value;
                            if (value <= minNumericValue) minNumericValue = value;
                        }
                    }
                    break;
                case DATE:
                    values = valuesColumn.getDateValues();
                    if (values != null && values.length > 0) {
                        for (int j = 0; j < values.length; j++) {
                            Date value = (Date) values[j];
                            if (value.after(maxDateValue)) maxDateValue = value;
                            if (value.before(minDateValue)) minDateValue = value;
                        }
                    }
                    break;
            }


        }
    }

    public double getMaxNumericValue() {
        return maxNumericValue;
    }

    public Date getMaxDateValue() {
        return maxDateValue;
    }

    public double getMinNumericValue() {
        return minNumericValue;
    }

    public Date getMinDateValue() {
        return minDateValue;
    }

    public XYChartData getData() {
        return data;
    }

    public int getNumSeries() {
        return numSeries;
    }

    public List<String> getAddedSeries() {
        return addedSeries;
    }

    public List<String> getRemovedSeries() {
        return removedSeries;
    }
}