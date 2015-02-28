package com.ait.lienzo.charts.client.xy.bar;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.*;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;

public class BarChartTooltip extends Group {

    public static final double TRIANGLE_SIZE = 10;
    private static final double TOOLTIP_PADDING_WIDTH = 25;
    private static final double TOOLTIP_PADDING_HEIGHT = 25;
    private static final IColor TOOLTIP_COLOR = ColorName.WHITESMOKE;
    private static final String FONT_FAMILY = "Verdana";
    private static final String CATEGORIES_FONT_STYLE = "";
    private static final String VALUES_FONT_STYLE = "bold";
    private static final int FONT_SIZE = 10;
    private static final IColor LABEL_COLOR = ColorName.BLACK;
    
    private Rectangle rectangle;
    private Triangle triangle;
    private Text categoriesText;
    private Text valuesText;

    public BarChartTooltip() {
        build();
    }

    protected IPrimitive build() {
        rectangle = new Rectangle(1,1).setFillColor(TOOLTIP_COLOR).setCornerRadius(30);
        triangle = new Triangle(new Point2D(1,1),new Point2D(1,1),new Point2D(1,1)).setFillColor(TOOLTIP_COLOR);
        categoriesText = new Text("", FONT_FAMILY, CATEGORIES_FONT_STYLE, FONT_SIZE).setFillColor(LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.TOP);
        valuesText = new Text("", FONT_FAMILY, VALUES_FONT_STYLE, FONT_SIZE).setFillColor(LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.TOP);
        add(rectangle);
        add(triangle);
        add(categoriesText);
        add(valuesText);
        categoriesText.moveToTop();
        valuesText.moveToTop();
        setVisible(false);
        return this;
        
    }
    
    public void show(String categoriesText, String valuesText) {
        setVisible(true);
        this.categoriesText.setText(categoriesText);
        double ctw = this.categoriesText.getBoundingBox().getWidth();
        double cth = this.categoriesText.getBoundingBox().getHeight();
        this.valuesText.setText(valuesText);
        double vtw = this.valuesText.getBoundingBox().getWidth();
        double vth = this.valuesText.getBoundingBox().getHeight();
        double rw = ctw > vtw ? ctw : vtw;
        rw += TOOLTIP_PADDING_WIDTH;
        double rh = cth + vth;
        rh += TOOLTIP_PADDING_HEIGHT;
        rectangle.setWidth(rw).setHeight(rh);
        double rx = rectangle.getX();
        double ry = rectangle.getY();
        triangle.setPoints(new Point2D(rx + rw/2 - TRIANGLE_SIZE, ry + rh),new Point2D(rx + rw/2, rh + TRIANGLE_SIZE),new Point2D(rx + rw/2 + TRIANGLE_SIZE, ry + rh));
        double vtx = rw/2 - vtw/2;
        double ctx = rw/2 - ctw/2;
        double vty = rh /2 - vth/2;
        double cty = vty + cth + 1;
        this.categoriesText.setX(ctx).setY(cty);
        this.valuesText.setX(vtx).setY(vty);
        setX(getX() - rw / 2);
        setY(getY() - rh);
        moveToTop();
        redraw();
    }
    
    public void hide() {
        setVisible(false);
        redraw();
    }
    
    private void redraw() {
        LayerRedrawManager.get().schedule(getLayer());
    }

    public void clear() {
        removeAll();
    }
}
