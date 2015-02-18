package com.ait.lienzo.charts.client.axis;

import java.util.Date;

public class DateAxis extends Axis {

    private static final AxisType AXIS_TYPE = AxisType.NUMBER;

    public DateAxis(String title) {
        super(title, AXIS_TYPE);
    }

    public DateAxis(String title, String format) {
        super(title, format, AXIS_TYPE);
    }

    public DateAxis(String title, Date minValue, Date maxValue) {
        super(title, AXIS_TYPE);
        setMinValue(minValue);
        setMaxValue(maxValue);
    }

    public DateAxis(String title, String format, Date minValue, Date maxValue) {
        super(title, format, AXIS_TYPE);
        setMinValue(minValue);
        setMaxValue(maxValue);
    }

    public DateAxis(DateAxisJSO m_jso) {
        super(m_jso);
        this.m_jso = m_jso;
    }

    public DateAxisJSO getJSO() {
        return (DateAxisJSO) m_jso;
    }

    public void setMaxValue(Date value) {
        ((DateAxisJSO)this.m_jso).setMaxValue(value);
    }

    public void setMinValue(Date value) {
        ((DateAxisJSO)this.m_jso).setMinValue(value);
    }
    
    public Date getMaxValue() {
        return ((DateAxisJSO)this.m_jso).getMaxValue();
    }

    public Date getMinValue() {
        return ((DateAxisJSO)this.m_jso).getMinValue();
    }

    public static class DateAxisJSO extends AxisJSO {
        protected DateAxisJSO() {
        }

        public final native void setMaxValue(Date maxValue) /*-{
            this.maxValue = maxValue;
        }-*/;

        public final native void setMinValue(Date minValue) /*-{
            this.minValue = minValue;
        }-*/;

        public final native Date getMinValue() /*-{
            if (!this.minValue) return null;
            return this.minValue;
        }-*/;

        public final native Date getMaxValue() /*-{
            if (!this.maxValue) return null;
            return this.maxValue;
        }-*/;

    }
}
