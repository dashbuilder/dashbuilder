package com.ait.lienzo.charts.client.xy.bar;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;

public class BarChartTooltip {
    
    private static final double TOOLTIP_PADDING_WIDTH = 10;
    private static final double TOOLTIP_PADDING_HEIGHT = 10;
    private static final IColor TOOLTIP_COLOR = ColorName.LIGHTGREY;
    private static final String FONT_FAMILY = "Verdana";
    private static final String FONT_STYLE = "";
    private static final int FONT_SIZE = 6;
    private static final IColor LABEL_COLOR = ColorName.BLACK;
    
    private Group mainGroup;
    private Rectangle rectangle;
    private Text categoriesText;
    private Text valuesText;
    
    public IPrimitive build() {
        mainGroup = new Group();
        rectangle = new Rectangle(1,1).setFillColor(TOOLTIP_COLOR);
        categoriesText = new Text("", FONT_FAMILY, FONT_STYLE, FONT_SIZE).setFillColor(LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.TOP);
        valuesText = new Text("", FONT_FAMILY, FONT_STYLE, FONT_SIZE).setFillColor(LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.TOP);
        mainGroup.add(rectangle);
        mainGroup.add(categoriesText);
        mainGroup.add(valuesText);
        categoriesText.moveToTop();
        valuesText.moveToTop();
        mainGroup.setVisible(false);
        return mainGroup;
        
    }
    
    public void show(double x, double y, String categoriesText, String valuesText) {
        mainGroup.setX(x).setY(y).setVisible(true);
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
        double ctx = rw/2 - ctw/2;
        double vtx = ctx;
        double cty = rh /2 - cth/2;
        double vty = cty + cth;
        this.categoriesText.setX(ctx).setY(cty);
        this.valuesText.setX(vtx).setY(vty);
        mainGroup.moveToTop();
        redraw();
    }
    
    public void hide() {
        mainGroup.setVisible(false);
        redraw();
    }
    
    private void redraw() {
        LayerRedrawManager.get().schedule(mainGroup.getLayer());
    }
}
