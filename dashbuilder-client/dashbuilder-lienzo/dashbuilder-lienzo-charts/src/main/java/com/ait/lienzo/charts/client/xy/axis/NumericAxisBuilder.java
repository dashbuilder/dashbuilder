package com.ait.lienzo.charts.client.xy.axis;

import com.ait.lienzo.charts.client.axis.Axis;
import com.ait.lienzo.charts.client.axis.NumericAxis;
import com.ait.lienzo.charts.client.xy.XYChartData;
import com.google.gwt.i18n.client.NumberFormat;

import java.util.LinkedList;
import java.util.List;

public final class NumericAxisBuilder extends AxisBuilder<Double> {

    private static final String NULL_VALUE = "0";
    // private static final NumberFormat numberFormat = NumberFormat.getFormat("#0.00");
    private static final NumberFormat numberFormat = NumberFormat.getDecimalFormat();
    private NumericAxis axis;
    

    public NumericAxisBuilder(XYChartData data, double chartSizeAttribute, Axis.AxisJSO jso) {
        super(data, chartSizeAttribute);
        buildAxis(jso);
    }

    public NumericAxisBuilder(XYChartData data, double chartSizeAttribute, AxisDirection axisDirection, Axis.AxisJSO jso) {
        super(data, chartSizeAttribute, axisDirection);
        buildAxis(jso);
    }
    
    protected void buildAxis(Axis.AxisJSO jso) {
        if (jso.getType().equals(Axis.AxisType.NUMBER)) {
            this.axis = new NumericAxis((NumericAxis.NumericAxisJSO) jso);
        } else {
            throw new RuntimeException("You cannot build a NumericAxisBuilder using a non NumericAxis");
        }
    }

    @Override
    public List<AxisLabel> getLabels() {
        String modelProperty = dataSummary.getData().getCategoryAxisProperty();
        Double[] values = dataSummary.getData().getDataTable().getNumericValues(modelProperty);
        int segments = axis.getSegments();
        Double maxValue = roundUp(getMaxValue());
        Double minValue = roundDown(getMinValue());


        double sizeAttributeIncrement = chartSizeAttribute / segments;
        double valueIncrement = (maxValue - minValue) / segments;

        List<AxisLabel> result = new LinkedList();
        for (int x = 0; x <= segments; x++) {
            double currentchartSizeAttribute = (axisDirection.equals(AxisDirection.DESC)) ? chartSizeAttribute - (sizeAttributeIncrement * x) : sizeAttributeIncrement * x; 
            double currentValue = valueIncrement * x;
            String formattedValue = format(currentValue);
            result.add(new AxisLabel(formattedValue, currentchartSizeAttribute));
        }
        return result;
    }

    @Override
    public List<AxisValue<Double>> getValues(String modelProperty) {
        Double[] values = dataSummary.getData().getDataTable().getNumericValues(modelProperty);
        int segments = axis.getSegments();
        Double maxValue = getMaxValue();
        Double minValue = getMinValue();

        List<AxisValue<Double>> result = new LinkedList();
        if (values != null && values.length > 0) {
            for (int i = 0, j = values.length - 1; i < values.length; i++, j--) {
                Double value = (axisDirection.equals(AxisDirection.DESC)) ? values[i] : values[j];
                // Obtain width and height values for the shape.
                double shapeSize = getSizeForShape(chartSizeAttribute, value, maxValue);
                result.add(new AxisValue<Double>(value, shapeSize));
            }
        }
        return result;
    }

    protected Double getMinValue() {
        Double minValue = axis.getMinValue();
        if (minValue == null) return dataSummary.getMinNumericValue();
        return minValue;
    }

    protected Double getMaxValue() {
        Double maxValue = axis.getMaxValue();
        if (maxValue == null) return dataSummary.getMaxNumericValue();
        return maxValue;
    }

    protected double getSizeForShape(double chartSizeAttribute, double value, double maxValue) {
        return ( chartSizeAttribute * value ) / maxValue;
    }

    @Override
    public String format(Double value) {
        if (value != null) return getNumberFormat().format(value);
        return NULL_VALUE;
    }
    
    private NumberFormat getNumberFormat() {
        if (axis.getFormat() != null && axis.getFormat().trim().length() > 0) return NumberFormat.getFormat(axis.getFormat());
        else return numberFormat;
    }
    
    private Double roundUp(Double value) {
        return Math.ceil(value);        
    }

    private Double roundDown(Double value) {
        return Math.floor(value);
    }
}