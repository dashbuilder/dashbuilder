package com.ait.lienzo.charts.client.resizer;

import com.ait.lienzo.charts.client.xy.bar.BarChart;
import com.ait.lienzo.charts.client.xy.bar.event.DataReloadedEventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ChartResizeEvent extends GwtEvent<ChartResizeEventHandler> {

    public static Type<ChartResizeEventHandler> TYPE = new Type<ChartResizeEventHandler>();

    private double width;
    private double height;

    public ChartResizeEvent(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Type<ChartResizeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ChartResizeEventHandler handler) {
        handler.onChartResize(this);
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }
}