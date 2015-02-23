package com.ait.lienzo.charts.client.xy.bar.event;

import com.ait.lienzo.charts.client.xy.bar.BarChart;
import com.google.gwt.event.shared.GwtEvent;

public class DataReloadedEvent extends GwtEvent<DataReloadedEventHandler> {

    public static Type<DataReloadedEventHandler> TYPE = new Type<DataReloadedEventHandler>();

    private BarChart chart;

    public DataReloadedEvent(BarChart chart) {
        this.chart = chart;
    }

    @Override
    public Type<DataReloadedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DataReloadedEventHandler handler) {
        handler.onDataReloaded(this);
    }

    public BarChart getChart() {
        return chart;
    }
}