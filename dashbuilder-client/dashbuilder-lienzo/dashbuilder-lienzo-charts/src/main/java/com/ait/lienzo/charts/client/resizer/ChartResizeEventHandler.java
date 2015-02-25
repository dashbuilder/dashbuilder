package com.ait.lienzo.charts.client.resizer;

import com.google.gwt.event.shared.EventHandler;

public interface ChartResizeEventHandler extends EventHandler
{
    void onChartResize(ChartResizeEvent event);
}