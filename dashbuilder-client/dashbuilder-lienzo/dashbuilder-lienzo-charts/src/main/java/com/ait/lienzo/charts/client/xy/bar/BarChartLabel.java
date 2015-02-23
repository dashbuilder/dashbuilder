package com.ait.lienzo.charts.client.xy.bar;

import com.ait.lienzo.charts.client.xy.axis.AxisBuilder;
import com.ait.lienzo.client.core.animation.AnimationProperties;
import com.ait.lienzo.client.core.animation.AnimationProperty;
import com.ait.lienzo.client.core.animation.AnimationTweener;
import com.ait.lienzo.client.core.shape.*;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;

public class BarChartLabel {
    private static final double ANIMATION_DURATION = 500;
    private static final String FONT_FAMILY = "Verdana";
    private static final String FONT_STYLE = "";
    private static final int FONT_SIZE = 6;
    private static final IColor LABEL_COLOR = ColorName.BLACK;

    private AxisBuilder.AxisLabel axisLabel;
    private Text label;
    private Rectangle labelContainer;

    public BarChartLabel(AxisBuilder.AxisLabel axisLabel) {
        this.axisLabel = axisLabel;
    }

    public IPrimitive build() {
        label = new Text(axisLabel.getText(), FONT_FAMILY, FONT_STYLE, FONT_SIZE).setFillColor(LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.TOP);
        label.setID("label" + axisLabel.getIndex());
        labelContainer = new Rectangle(1,1);
        Group labelGroup = new Group();
        labelGroup.add(label);
        labelGroup.add(labelContainer);
        labelContainer.setAlpha(0.01);
        labelContainer.moveToTop();
        return labelGroup;
    }

    public void setAttributes(Double x, Double y, Double width, Double height, boolean animate) {
        String text = axisLabel.getText();
        label.setText(text);
        setShapeAttributes(label, x, y, width, height, animate);
        setShapeAttributes(labelContainer, x, y, width, height, animate);
    }
    
    public void clear() {
        // Create the animation properties.
        AnimationProperties animationProperties = new AnimationProperties();
        animationProperties.push(AnimationProperty.Properties.ALPHA(0d));

        // Apply animation to axis titles.
        label.animate(AnimationTweener.LINEAR, animationProperties, BarChart.CLEAR_ANIMATION_DURATION);
    }

    public Text getLabel() {
        return label;
    }

    public Rectangle getLabelContainer() {
        return labelContainer;
    }

    public AxisBuilder.AxisLabel getAxisLabel() {
        return axisLabel;
    }

    public double getLabelWidth() {
        return label.getBoundingBox().getWidth();        
    }

    public double getLabelHeight() {
        return label.getBoundingBox().getHeight();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        try {
            BarChartLabel _label = (BarChartLabel) obj;
            return label.getID().equals(_label.getLabel().getID());
        } catch (ClassCastException e) {
            return false;
        }
    }

    private void setShapeAttributes(Shape shape, Double x, Double y, Double width, Double height, boolean animate) {
        AnimationProperties animationProperties = new AnimationProperties();
        if (width != null) animationProperties.push(AnimationProperty.Properties.WIDTH(width));
        if (height != null) animationProperties.push(AnimationProperty.Properties.HEIGHT(height));
        if (x != null) animationProperties.push(AnimationProperty.Properties.X(x));
        if (y != null) animationProperties.push(AnimationProperty.Properties.Y(y));
        shape.animate(AnimationTweener.LINEAR, animationProperties, ANIMATION_DURATION);
    }

}
