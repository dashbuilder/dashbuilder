package com.ait.lienzo.charts.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class DataReloadedEvent extends GwtEvent<DataReloadedEventHandler> {

    public static Type<DataReloadedEventHandler> TYPE = new Type<DataReloadedEventHandler>();

    public DataReloadedEvent() {
    }

    @Override
    public Type<DataReloadedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DataReloadedEventHandler handler) {
        handler.onDataReloaded(this);
    }
}