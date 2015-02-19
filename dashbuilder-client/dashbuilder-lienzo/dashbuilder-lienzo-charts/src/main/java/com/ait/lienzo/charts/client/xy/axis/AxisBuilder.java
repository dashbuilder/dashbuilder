package com.ait.lienzo.charts.client.xy.axis;

import com.ait.lienzo.charts.client.xy.XYChartData;
import com.ait.lienzo.charts.client.xy.XYChartDataSummary;

import java.util.Collection;
import java.util.List;

public abstract class AxisBuilder<T> {
    protected XYChartDataSummary dataSummary;
    protected double chartSizeAttribute;
    protected AxisDirection axisDirection;

    public AxisBuilder(XYChartData data, double chartSizeAttribute) {
        this.dataSummary = new XYChartDataSummary(data);
        this.chartSizeAttribute = chartSizeAttribute;
        this.axisDirection = AxisDirection.DESC;
    }

    public AxisBuilder(XYChartData data, double chartSizeAttribute, AxisDirection axisDirection) {
        this(data, chartSizeAttribute);
        this.axisDirection = axisDirection;
    }

    /**
     * Get axis labels for all series
     * TODO: Cache.
     * @return All serie axis labels.
     */
    public abstract List<AxisLabel> getLabels();

    /**
     * Get axis values for a given property in the datatable model..
     * TODO: Cache.
     * @parm modelProperty The property in the datatable model.. 
     * @return Serie's axis values.
     */
    public abstract List<AxisValue<T>> getValues(String modelProperty);

    public void reload(XYChartData data, Collection<String> currentSeries, double chartSizeAttribute) {
        // Rebuild data summary as columns, series and values can have been modified.
        this.dataSummary = new XYChartDataSummary(data, currentSeries);
        this.chartSizeAttribute = chartSizeAttribute;
    }

    public String format(T value) {
        return value.toString();
    }

    public XYChartDataSummary getDataSummary() {
        return this.dataSummary;
    }

    public class AxisLabel {
        protected String text;
        protected double position;

        protected AxisLabel(String text, double position) {
            this.text = text;
            this.position = position;
        }

        public String getText() {
            return text;
        }

        public double getPosition() {
            return position;
        }
    }

    public class AxisValue<T> {
        protected T value;
        protected double position;

        protected AxisValue(T value, double position) {
            this.value = value;
            this.position = position;
        }

        public double getPosition() {
            return position;
        }

        public void setPosition(double position) {
            this.position = position;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

    }
    
    public enum AxisDirection {
        ASC, DESC;
    }
}