package com.ait.lienzo.charts.client.xy.axis;

import com.ait.lienzo.charts.client.axis.Axis;
import com.ait.lienzo.charts.client.axis.CategoryAxis;
import com.ait.lienzo.charts.client.model.DataTableColumn;
import com.ait.lienzo.charts.client.xy.XYChartData;
import com.ait.lienzo.charts.client.xy.XYChartSerie;

import java.util.LinkedList;
import java.util.List;

public final class CategoryAxisBuilder extends AxisBuilder<String> {
    protected CategoryAxis axis;


    public CategoryAxisBuilder(XYChartData data, double chartSizeAttribute, Axis.AxisJSO jso) {
        super(data, chartSizeAttribute);
        buildAxis(jso);
    }

    public CategoryAxisBuilder(XYChartData data, double chartSizeAttribute, AxisDirection axisDirection, Axis.AxisJSO jso) {
        super(data, chartSizeAttribute, axisDirection);
        buildAxis(jso);
    }

    protected void buildAxis(Axis.AxisJSO jso) {
        if (jso.getType().equals(Axis.AxisType.CATEGORY)) {
            this.axis = new CategoryAxis((CategoryAxis.CategoryAxisJSO) jso);
        } else {
            throw new RuntimeException("You cannot build a CategoryAxisBuilder using a non CategoryAxis");
        }
    }

    @Override
    public List<AxisLabel> getLabels() {
        List<AxisLabel> result = new LinkedList();
        XYChartSerie[] series = dataSummary.getData().getSeries();
        DataTableColumn dataTableLabelsColumn = dataSummary.getData().getDataTable().getColumn(dataSummary.getData().getCategoryAxisProperty());
        String[] labelValues = dataTableLabelsColumn.getStringValues();
        int labelsCount = labelValues.length;
        int seriesCount = dataSummary.getNumSeries();
        double labelSize = chartSizeAttribute / ( seriesCount * labelsCount);
        for (int i = 0, j = labelsCount; i < labelsCount; i++, j--) {
            String text = labelValues[i];
            int axisDivisions = axis.getSegments();
            double position = (axisDirection.equals(AxisDirection.DESC)) ? labelSize * i : labelSize * j;
            result.add(new AxisLabel(text, i, position * seriesCount));
        }
        return result;
    }

    @Override
    public List<AxisValue<String>> getValues(String modelProperty) {
        String[] values = dataSummary.getData().getDataTable().getStringValues(modelProperty);
        int segments = axis.getSegments();
        int valuesCount = values.length;
        int seriesCount = dataSummary.getNumSeries();
        
        List<AxisValue<String>> result = new LinkedList();
        if (values != null) {
            for (int i = 0, j = valuesCount - 1; i < valuesCount; i++, j--) {
                String value = (axisDirection.equals(AxisDirection.DESC)) ? values[i] : values[j];
                int axisDivisions = axis.getSegments();
                double barSize =  (chartSizeAttribute - (axisDivisions * (valuesCount+1) ) ) / valuesCount / seriesCount;
                double position = (barSize * seriesCount * i) + (segments * (i +1));
                result.add(new AxisValue<String>(value, position));
            }
            return result;
        }
        return result;
    }

}