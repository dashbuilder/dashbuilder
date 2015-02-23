package com.ait.lienzo.charts.client.pie.event;

import com.google.gwt.event.shared.GwtEvent;

public class ValueSelectedEvent extends GwtEvent<ValueSelectedHandler> {

    public static Type<ValueSelectedHandler> TYPE = new Type<ValueSelectedHandler>();
    private String column;
    private int row;

    public ValueSelectedEvent(String column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public String getColumn() {
        return column;
    }

    @Override
    public Type<ValueSelectedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ValueSelectedHandler handler) {
        handler.onValueSelected(this);
    }
}