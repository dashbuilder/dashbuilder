package com.ait.lienzo.charts.client.xy.bar.event;

import com.google.gwt.event.shared.EventHandler;

public interface ValueSelectedHandler extends EventHandler
{
    void onValueSelected(ValueSelectedEvent event);
}