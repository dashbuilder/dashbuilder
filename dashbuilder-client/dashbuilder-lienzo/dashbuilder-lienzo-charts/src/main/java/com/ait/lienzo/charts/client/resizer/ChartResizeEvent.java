package com.ait.lienzo.charts.client.resizer;

import com.google.gwt.event.shared.GwtEvent;

public class ChartResizeEvent extends GwtEvent<ChartResizeEventHandler> {

    public static Type<ChartResizeEventHandler> TYPE = new Type<ChartResizeEventHandler>();

    private double width;
    private double height;
    private boolean apply;

    public ChartResizeEvent(double width, double height, boolean apply) {
        this.width = width;
        this.height = height;
        this.apply = apply;
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

    public boolean isApply() {
        return apply;
    }
}