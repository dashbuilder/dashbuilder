package com.ait.lienzo.charts.client.axis;

public class CategoryAxis extends Axis {
    
    private static final AxisType AXIS_TYPE = AxisType.CATEGORY;

    public CategoryAxis(String title) {
        super(title, AXIS_TYPE);
    }

    public CategoryAxis(String title, String format) {
        super(title, format, AXIS_TYPE);
    }

    public CategoryAxis(String title, String format, int size) {
        super(title, format, AXIS_TYPE);
    }

    public CategoryAxis(CategoryAxisJSO m_jso) {
        super(m_jso);
    }

    public CategoryAxisJSO getJSO() {
        return (CategoryAxisJSO) m_jso;
    }

    public static class CategoryAxisJSO extends AxisJSO {
        protected CategoryAxisJSO() {
        }

    }
    
}
