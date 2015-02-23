package com.ait.lienzo.charts.client.xy.bar.event;

import com.google.gwt.event.shared.GwtEvent;

public class SelectEvent extends GwtEvent<SelectEventHandler> {

    public static Type<SelectEventHandler> TYPE = new Type<SelectEventHandler>();
    private String serie;
    private String column;
    private int row;

    public SelectEvent(String serie, String column, int row) {
        this.serie = serie;
        this.column = column;
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public String getColumn() {
        return column;
    }

    public String getSerie() {
        return serie;
    }

    @Override
    public Type<SelectEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SelectEventHandler handler) {
        handler.onSelect(this);
    }
}