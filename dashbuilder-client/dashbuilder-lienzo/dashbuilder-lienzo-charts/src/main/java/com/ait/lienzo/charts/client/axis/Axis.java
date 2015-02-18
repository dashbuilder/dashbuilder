package com.ait.lienzo.charts.client.axis;

import com.google.gwt.core.client.JavaScriptObject;

public abstract class Axis {

    public static enum AxisType {
        CATEGORY, NUMBER, DATE;
    }
    
    protected AxisJSO m_jso;

    public Axis(String title, AxisType type) {
        this(AxisJSO.make(title, type));
    }

    public Axis(String title, String format, AxisType type) {
        this(AxisJSO.make(title, format, type));
    }

    public Axis(AxisJSO m_jso) {
        this.m_jso = m_jso;
        this.m_jso.setSegments(5);
    }

    public AxisJSO getJSO() {
        return m_jso;
    }

    public void setFormat(String format) {
        this.m_jso.setFormat(format);
    }

    public void setTitle(String title) {
        this.m_jso.setTitle(title);
    }

    public void setSegments(int segments) {
        this.m_jso.setSegments(segments);
    }
    
    public String getTitle() {
        return this.m_jso.getTitle();
    }

    public String getFormat() {
        return this.m_jso.getFormat();
    }

    public int getSegments() {
        return this.m_jso.getSegments();
    }

    public AxisType getType() {
        return this.m_jso.getType();
    }
    
    public static class AxisJSO extends JavaScriptObject {
        protected AxisJSO() {
        }

        public static AxisJSO make(AxisType type) {
            AxisJSO axisJSO = createObject().cast();
            axisJSO.setType(type);
            return axisJSO;
        }

        public static AxisJSO make(String title, AxisType type) {
            AxisJSO axisJSO = make(type);
            axisJSO.setTitle(title);
            return axisJSO;
        }

        public static AxisJSO make(String title, String format, AxisType type) {
            AxisJSO axisJSO = make(title, type);
            axisJSO.setFormat(format);
            return axisJSO;
        }

        public final native void setFormat(String format) /*-{
            this.format = format;
        }-*/;

        public final native void setTitle(String title) /*-{
            this.title = title;
        }-*/;

        public final native void setType(AxisType type) /*-{
            this.type = type;
        }-*/;

        public final native void setSegments(int segments) /*-{
            this.segments = segments;
        }-*/;
        
        public final native AxisType getType() /*-{
            return this.type;
        }-*/;

        public final native String getFormat() /*-{
            return this.format;
        }-*/;

        public final native String getTitle() /*-{
            return this.title;
        }-*/;

        public final native int getSegments() /*-{
            return this.segments;
        }-*/;
    }
    
    

}
