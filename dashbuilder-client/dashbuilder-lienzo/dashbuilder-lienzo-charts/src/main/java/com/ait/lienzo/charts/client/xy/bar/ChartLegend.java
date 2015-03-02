package com.ait.lienzo.charts.client.xy.bar;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
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


    public static class ChartLegendEntry {
        private String name;
        private IColor color;

        public ChartLegendEntry(String name, IColor color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public IColor getColor() {
            return color;
        }

        public void setColor(IColor color) {
            this.color = color;
        }
    }
    protected List<ChartLegendEntry> entries;

    public ChartLegend() {
        entries = new LinkedList<ChartLegendEntry>();
    }

    public ChartLegend(ChartLegendEntry[] _series) {
        entries = new LinkedList<ChartLegendEntry>();
        if (_series != null) {
            for (ChartLegendEntry serie : _series) {
                entries.add(serie);
            }
        }
    }

    public boolean add(ChartLegendEntry serie) {
        boolean added = entries.add(serie);
        if (added) build();
        return added;
    }

    public void remove(String serieName) {
        int pos = getPos(serieName);
        if (pos > -1) {
            entries.remove(pos);
            build();
        }
    }

    protected int getPos(String serieName) {
        for (int x = 0; x < entries.size(); x++) {
            ChartLegendEntry serie = entries.get(x);
            if (serie.getName().equals(serieName)) return x;
        }
        return -1;
    }

    public Group build() {
        removeAll();
        if (!entries.isEmpty()) {
            for (int x = 0; x < entries.size(); x++) {
                final ChartLegendEntry serie = entries.get(x);
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
