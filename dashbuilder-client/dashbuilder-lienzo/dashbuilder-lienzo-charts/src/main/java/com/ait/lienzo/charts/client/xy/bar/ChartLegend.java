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
    private static final int LEGEND_WIDTH_SEPARATION = 10;
    private static final int MAX_ENTRIES_ROW = 5;
    private static final String FONT_FAMILY = "Verdana";
    private static final String FONT_STYLE = "";
    private static final int FONT_SIZE = 6;

    public enum ChartLegendOrientation {
        HORIZONTAL, VERTICAL;
    }
    
    private ChartLegendOrientation orientation = ChartLegendOrientation.VERTICAL;
    
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

    public ChartLegend add(ChartLegendEntry serie) {
        entries.add(serie);
        return this;
    }

    public ChartLegend remove(String serieName) {
        int pos = getPos(serieName);
        if (pos > -1) {
            entries.remove(pos);
        }
        return this;
    }
    
    public ChartLegend setOrientation(ChartLegendOrientation orientation) {
        this.orientation = orientation;
        return this;
    }

    protected int getPos(String serieName) {
        for (int x = 0; x < entries.size(); x++) {
            ChartLegendEntry serie = entries.get(x);
            if (serie.getName().equals(serieName)) return x;
        }
        return -1;
    }

    public ChartLegend build() {
        removeAll();
        if (!entries.isEmpty()) {
            switch (orientation) {
                case HORIZONTAL:
                    buildHorizontal();
                    break;
                default:
                    buildVertical();
                    break;
            }
        }
        return this;
    }

    private ChartLegend buildVertical() {
        
        // Build legend entries.
        for (int x = 0; x < entries.size(); x++) {
            final ChartLegendEntry serie = entries.get(x);
            final double legendX = 0;
            final double legendY = 30 + (LEGEND_HEIGHT_SEPARATION * x ) + (LEGEND_RECTANGLE_HEIGHT * x);
            final Rectangle rectangle = new Rectangle(LEGEND_RECTANGLE_WIDTH,LEGEND_RECTANGLE_HEIGHT).setX(legendX).setY(legendY).setFillColor(serie.getColor());
            final Text text = new Text(serie.getName(), FONT_FAMILY, FONT_STYLE, FONT_SIZE).setFillColor(ColorName.BLACK).setX(legendX + LEGEND_RECTANGLE_WIDTH + 10).setY(legendY + (LEGEND_HEIGHT_SEPARATION / 2)).setTextBaseLine(TextBaseLine.MIDDLE);
            add(rectangle);
            add(text);
        }
        return this;
    }

    private ChartLegend buildHorizontal() {
        Text firstText = null;
        Text lastText = null;
        Text lastTextOnSameRow = null;
        
        // Build legend entries.
        final int size = entries.size();
        int column = 1;
        for (int x = 0; x < size; x++) {
            final ChartLegendEntry serie = entries.get(x);
            final double lastEntryX = lastText != null ? lastText.getX() : 0;
            final double lastEntryWidth = getLegendEntryWidth(lastText);
            final double legendX = lastEntryX + lastEntryWidth;
            final double legendY = LEGEND_RECTANGLE_HEIGHT * column;
            final Rectangle rectangle = new Rectangle(LEGEND_RECTANGLE_WIDTH,LEGEND_RECTANGLE_HEIGHT).setX(legendX).setY(legendY).setFillColor(serie.getColor());
            final Text text = new Text(serie.getName(), FONT_FAMILY, FONT_STYLE, FONT_SIZE).setFillColor(ColorName.BLACK).setX(legendX + LEGEND_RECTANGLE_WIDTH).setY(legendY + LEGEND_RECTANGLE_HEIGHT/2).setTextBaseLine(TextBaseLine.MIDDLE);
            add(rectangle);
            add(text);
            if ( x == 0 ) firstText = text;
            if (x + 1 < (MAX_ENTRIES_ROW  * column)) {
                lastText = text;
            }
            else {
                lastTextOnSameRow = lastText;
                lastText = null;
                column++;
            }
            if (lastTextOnSameRow == null) lastTextOnSameRow = lastText;
        }
        
        // Center the legend area.
        if (lastTextOnSameRow != null) {
            final double lastEntryX = lastTextOnSameRow.getX();
            final double lastEntryWidth = getLegendEntryWidth(lastTextOnSameRow);
            final double firstX = firstText.getX();
            final double w = firstX - lastEntryX + lastEntryWidth;
            setX(w > 0 ? getX() - w : getX() + w);
        }
        return this;
    }
    
    protected double getLegendEntryWidth(Text entry) {
        if (entry != null) return entry.getBoundingBox().getWidth() + LEGEND_RECTANGLE_WIDTH + LEGEND_WIDTH_SEPARATION;
        return 0;
    }

    public ChartLegend clear() {
        removeAll();
        return this;
    }
}
