package com.ait.lienzo.charts.client.axis;

public class NumericAxis extends Axis {

    private static final AxisType AXIS_TYPE = AxisType.NUMBER;

    public NumericAxis(String title) {
        super(title, AXIS_TYPE);
    }

    public NumericAxis(String title, String format) {
        super(title, format, AXIS_TYPE);
    }

    public NumericAxis(String title, double minValue, double maxValue) {
        super(title, AXIS_TYPE);
        setMinValue(minValue);
        setMaxValue(maxValue);
    }

    public NumericAxis(String title, String format, double minValue, double maxValue) {
        super(title, format, AXIS_TYPE);
        setMinValue(minValue);
        setMaxValue(maxValue);
    }

    public NumericAxis(NumericAxisJSO m_jso) {
        super(m_jso);
        this.m_jso = m_jso;
    }

    public NumericAxisJSO getJSO() {
        return (NumericAxisJSO) m_jso;
    }

    public void setMaxValue(double value) {
        getJSO().setMaxValue(value);
    }

    public void setMinValue(double value) {
        getJSO().setMinValue(value);
    }

    public Double getMaxValue() {
        return getJSO().getMaxValue();
    }

    public Double getMinValue() {
        return getJSO().getMinValue();
    }
    
    public static class NumericAxisJSO extends AxisJSO {
        protected NumericAxisJSO() {
        }

        public final native void setMaxValue(Double maxValue) /*-{
            this.maxValue = maxValue;
        }-*/;

        public final native void setMinValue(Double minValue) /*-{
            this.minValue = minValue;
        }-*/;
        
        public final native Double getMinValue() /*-{
            return this.minValue;
        }-*/;

        public final native Double getMaxValue() /*-{
            return this.maxValue;
        }-*/;
        
    }
}
