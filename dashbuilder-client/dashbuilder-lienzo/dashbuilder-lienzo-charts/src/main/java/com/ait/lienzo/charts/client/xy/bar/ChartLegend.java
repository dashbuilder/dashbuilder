package com.ait.lienzo.charts.client.xy.bar;

import com.ait.lienzo.charts.client.xy.XYChartSerie;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextBaseLine;

import java.util.LinkedList;
import java.util.List;

public class ChartLegend extends Group {
    private static final int LEGEND_RECTANGLE_WIDTH = 15;
    private static final int LEGEND_RECTANGLE_HEIGHT = 15;
    private static final int LEGEND_HEIGHT_SEPARATION = 10;
    private static final String FONT_FAMILY = "Verdana";
    private static final String FONT_STYLE = "";
    private static final int FONT_SIZE = 6;


    protected List<XYChartSerie> series;

    public ChartLegend() {
        series = new LinkedList<XYChartSerie>();
    }

    public ChartLegend(XYChartSerie[] _series) {
        series = new LinkedList<XYChartSerie>();
        if (_series != null) {
            for (XYChartSerie serie : _series) {
                series.add(serie);
            }
        }
    }

    public boolean add(XYChartSerie serie) {
        boolean added = series.add(serie);
        if (added) build();
        return added;
    }

    public void remove(String serieName) {
        int pos = getPos(serieName);
        if (pos > -1) {
            series.remove(pos);
            build();
        }
    }

    protected int getPos(String serieName) {
        for (int x = 0; x < series.size(); x++) {
            XYChartSerie serie = series.get(x);
            if (serie.getName().equals(serieName)) return x;
        }
        return -1;
    }

    public Group build() {
        removeAll();
        if (!series.isEmpty()) {
            for (int x = 0; x < series.size(); x++) {
                final XYChartSerie serie = series.get(x);
                final double legendY = 30 + (LEGEND_HEIGHT_SEPARATION * x ) + (LEGEND_RECTANGLE_HEIGHT * x);
                final Rectangle rectangle = new Rectangle(LEGEND_RECTANGLE_WIDTH,LEGEND_RECTANGLE_HEIGHT).setX(0).setY(legendY).setFillColor(serie.getColor());
                final Text text = new Text(serie.getName(), FONT_FAMILY, FONT_STYLE, FONT_SIZE).setFillColor(ColorName.BLACK).setX(LEGEND_RECTANGLE_WIDTH + 10).setY(legendY + (LEGEND_HEIGHT_SEPARATION / 2)).setTextBaseLine(TextBaseLine.MIDDLE);
                add(rectangle);
                add(text);
            }
        }
        return this;
    }

    public void clear() {
        removeAll();
    }
}
