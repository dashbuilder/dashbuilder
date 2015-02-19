package com.ait.lienzo.charts.client;

import com.ait.lienzo.charts.client.xy.axis.AxisBuilder;
import com.ait.lienzo.charts.shared.core.types.*;
import com.ait.lienzo.client.core.Attribute;
import com.ait.lienzo.client.core.animation.*;
import com.ait.lienzo.client.core.event.*;
import com.ait.lienzo.client.core.shape.*;
import com.ait.lienzo.client.core.shape.json.IFactory;
import com.ait.lienzo.client.core.shape.json.validators.ValidationContext;
import com.ait.lienzo.client.core.shape.json.validators.ValidationException;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;

import java.util.List;

/**
 *  <p>Base chart implementation class.</p>
 *  <p>It provides:</p>
 *  <ul>
 *      <li>Five areas (group nodes):</li>
 *      <ul>
 *          <li>Chart area: Group node that contains all the chart shapes that represent the values.</li>          
 *          <li>Bottom area: Group node can usually contains axis labels or the chart legend.</li>
 *          <li>Top area: Group node usually contains the chart title or the chart legend.</li>
 *          <li>Left area: Group node usually contains axis labels or the chart legend.</li>
 *          <li>Right area: Group node usually contains axis labels or the chart legend.</li>
 *      </ul>      
 *      <li>Chart title.</li>
 *      <li>Chart legend.</li>
 *      <li>Chart resizer.</li>
 *  </ul>
 *  <p>It listens the <code>AttributesChangedEvent</code> for attributes <code>X</code>, <code>Y</code>, <code>WIDTH</code> and <code>HEIGHT</code>.</p>  
 *  
 */
public abstract class AbstractChart<T extends AbstractChart> extends Group {

    // Default animation duration (2sec).
    protected static final double ANIMATION_DURATION = 2000;
    protected static final int AREA_PADDING = 50;
    protected static final String AXIS_LABEL_DEFAULT_FONT_NAME = "Verdana";
    protected static final String AXIS_LABEL_DEFAULT_FONT_STYLE = "bold";
    protected static final IColor AXIS_LABEL_COLOR = ColorName.SILVER;
    protected static final int AXIS_LABEL_DEFAULT_FONT_SIZE = 10;

    // The available areas: chart, top, bottom, left and right. 
    protected final Group chartArea = new Group();
    protected final Group topArea = new Group();
    protected final Group bottomArea = new Group();
    protected final Group rightArea = new Group();
    protected final Group leftArea = new Group();

    protected AbstractChart() {
    }

    public AbstractChart(JSONObject node, ValidationContext ctx) throws ValidationException {
        super(node, ctx);
    }

    @Override
    public IFactory<Group> getFactory() {
        return new ChartFactory();
    }

    public static class ChartFactory extends GroupFactory {
        public ChartFactory() {
            addAttribute(ChartAttribute.X, true);
            addAttribute(ChartAttribute.Y, true);
            addAttribute(ChartAttribute.WIDTH, true);
            addAttribute(ChartAttribute.HEIGHT, true);
            addAttribute(ChartAttribute.NAME, true);
            addAttribute(ChartAttribute.ALIGN, false);
            addAttribute(ChartAttribute.DIRECTION, false);
            addAttribute(ChartAttribute.ORIENTATION, false);
            addAttribute(ChartAttribute.SHOW_TITLE, false);
            addAttribute(ChartAttribute.FONT_FAMILY, false);
            addAttribute(ChartAttribute.FONT_STYLE, false);
            addAttribute(ChartAttribute.FONT_SIZE, false);
            addAttribute(ChartAttribute.LEGEND_POSITION, false);
            addAttribute(ChartAttribute.LEGEND_ALIGN, false);
            addAttribute(ChartAttribute.RESIZABLE, false);
        }

        @Override
        public boolean addNodeForContainer(IContainer<?, ?> container, Node<?> node, ValidationContext ctx) {
            return false;
        }
    }

    public AbstractChart build() {


        // Add the area node containers.
        add(chartArea); // Area for drawing the chart.
        add(topArea); // Area for top padding.
        add(bottomArea); // Area for bottom padding.
        add(leftArea); // Area for left padding.
        add(rightArea); // Area for right padding.

        // Position the areas.
        double currentx = chartArea.getX();
        double currenty = chartArea.getY();
        moveAreas(currentx, currenty);

        // Chart title.
        final Text chartTitle = new Text(getName(), getFontFamily(), getFontStyle(), getFontSize()).setFillColor(ColorName.BLACK).setTextAlign(TextAlign.CENTER).setTextBaseLine(TextBaseLine.MIDDLE);
        setShapeAttributes(chartTitle,getChartWidth() / 2, 10d, null, null, true);
        topArea.add(chartTitle);

        // Call parent build implementation.
        doBuild();
        
        // Add the resizer.
        final ChartResizer resizer = new ChartResizer();
        if (isResizable()) {
            resizer.build();
            moveResizerToTop(resizer);
        }

        // Attribute change handlers.
        this.addAttributesChangedHandler(Attribute.X, new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                GWT.log("AbstractChart - X attribute changed.");
                moveAreas(getX(), null);
            }
        });

        this.addAttributesChangedHandler(Attribute.Y, new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                GWT.log("AbstractChart - Y attribute changed.");
                moveAreas(null, getY());
                moveResizerToTop(resizer);
            }
        });

        this.addAttributesChangedHandler(Attribute.WIDTH, new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                GWT.log("AbstractChart - WIDTH attribute changed -> " + getOriginalWidth());
                setGroupAttributes(bottomArea, null, topArea.getY() + getChartHeight() + AREA_PADDING, false);
                setGroupAttributes(rightArea, topArea.getX() + getChartWidth() + AREA_PADDING, null, false);
                setShapeAttributes(chartTitle, getOriginalWidth() / 2, null, null, null, false);
                moveResizerToTop(resizer);
            }
        });

        this.addAttributesChangedHandler(Attribute.HEIGHT, new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                GWT.log("AbstractChart - HEIGHT attribute changed -> " + getOriginalHeight());
                setGroupAttributes(bottomArea, null, topArea.getY() + getChartHeight() + AREA_PADDING, false);
                setGroupAttributes(rightArea, topArea.getX() + getChartWidth() + AREA_PADDING, null, false);
                setShapeAttributes(chartTitle, getOriginalWidth() / 2, null, null, null, false);
                moveResizerToTop(resizer);
            }
        });

        return (T) this;
    }

    protected void moveAreas(Double x, Double y) {
        if (x != null) {
            topArea.setX(x);
            chartArea.setX(x + AREA_PADDING);
            bottomArea.setX(x + AREA_PADDING);
            leftArea.setX(x);
            rightArea.setX(x + getChartWidth() + AREA_PADDING);
        }
        if (y != null) {
            topArea.setY(y);
            chartArea.setY(y + AREA_PADDING);
            bottomArea.setY(y + getChartHeight() + AREA_PADDING);
            leftArea.setY(y);
            rightArea.setY(y + AREA_PADDING);
        }
    }
    
    protected void moveResizerToTop(ChartResizer resizer) {
        if (isResizable()) {
            resizer.moveToTop();
        }
    }

    protected abstract void doBuild();

    protected T setGroupAttributes(Group group, Double x, Double y, boolean animate) {
        return setGroupAttributes(group, x, y, null, animate);
    }

    protected T setGroupAttributes(Group group, Double x, Double y, Double alpha, boolean animate) {
        if (x != null) group.setX(x);
        if (y != null) group.setY(y);
        if (alpha != null) group.setAlpha(alpha);
        return (T) this;
    }

    protected T setShapeAttributes(Shape shape, Double x, Double y, Double width, Double height, boolean animate) {
        return setShapeAttributes(shape, x, y, width, height, null, null, animate);
    }

    protected T setShapeAttributes(Shape shape, Double x, Double y, Double width, Double height, IColor color, boolean animate) {
        return setShapeAttributes(shape, x, y, width, height, color, null, animate);
    }

    protected T setShapeAttributes(Shape shape, Double x, Double y, Double width, Double height, IColor color, Double alpha, boolean animate) {
        if (animate) {
            AnimationProperties animationProperties = new AnimationProperties();
            if (width != null) animationProperties.push(AnimationProperty.Properties.WIDTH(width));
            if (height != null) animationProperties.push(AnimationProperty.Properties.HEIGHT(height));
            if (x != null) animationProperties.push(AnimationProperty.Properties.X(x));
            if (y != null) animationProperties.push(AnimationProperty.Properties.Y(y));
            if (color != null) animationProperties.push(AnimationProperty.Properties.FILL_COLOR(color));
            if (alpha != null) animationProperties.push(AnimationProperty.Properties.ALPHA(alpha));
            shape.animate(AnimationTweener.LINEAR, animationProperties, ANIMATION_DURATION);
        } else {
            if (x != null) shape.setX(x);
            if (y != null) shape.setY(y);
            if (width != null) shape.getAttributes().setWidth(width);
            if (height != null) shape.getAttributes().setHeight(height);
            if (color != null) shape.setFillColor(color);
            if (alpha != null) shape.setAlpha(alpha);
        }
        return (T) this;
    }


    /**
     * Build the shapes & mouse handlers for resizing the chart.
     */
    protected class ChartResizer {
        private static final int RECTANGLE_SIZE = 30;
        private static final double RECTANGLE_INITIA_ALPHA = 0.2d;
        private static final double RECTANGLE_ANIMATION_DURATION = 500;
        private int initialXPosition;
        private int initialYPosition;
        protected Rectangle resizeRectangleButton;
        protected Rectangle resizeRectangle;
        protected Arrow resizeArrow1;
        protected Arrow resizeArrow2;
        protected Arrow resizeArrow3;
        protected Arrow resizeArrow4;

        public ChartResizer() {
        }

        public void build() {
            if (resizeRectangleButton == null) {
                double rectangleXPos = getChartWidth() - RECTANGLE_SIZE;
                double rectangleYPos = getChartHeight() - RECTANGLE_SIZE;
                resizeRectangleButton = new Rectangle(RECTANGLE_SIZE, RECTANGLE_SIZE).setX(rectangleXPos).setY(rectangleYPos).setFillColor(ColorName.GREY).setDraggable(true).setAlpha(RECTANGLE_INITIA_ALPHA);
                resizeRectangle = new Rectangle(getChartWidth(), getChartHeight()).setX(0).setY(0).setFillColor(ColorName.GREY).setAlpha(0);
                resizeArrow1 = new Arrow(new Point2D(getChartWidth() / 2, getChartHeight() / 2), new Point2D(getChartWidth(), getChartHeight() / 2), 0, 10, 10, 10, ArrowType.AT_END_TAPERED).setFillColor(ColorName.BLACK).setAlpha(0);
                resizeArrow2 = new Arrow(new Point2D(getChartWidth() / 2, getChartHeight() / 2), new Point2D(getChartWidth() / 2, getChartHeight()), 0, 10, 10, 10, ArrowType.AT_END_TAPERED).setFillColor(ColorName.BLACK).setAlpha(0);
                resizeArrow3 = new Arrow(new Point2D(getChartWidth() / 2, getChartHeight() / 2), new Point2D(0, getChartHeight() / 2), 0, 10, 10, 10, ArrowType.AT_END_TAPERED).setFillColor(ColorName.BLACK).setAlpha(0);
                resizeArrow4 = new Arrow(new Point2D(getChartWidth() / 2, getChartHeight() / 2), new Point2D(getChartWidth() / 2, 0), 0, 10, 10, 10, ArrowType.AT_END_TAPERED).setFillColor(ColorName.BLACK).setAlpha(0);

                resizeRectangleButton.addNodeMouseEnterHandler(new NodeMouseEnterHandler() {
                    @Override
                    public void onNodeMouseEnter(NodeMouseEnterEvent event) {
                        // Apply alphas.
                        AnimationProperties animationProperties = new AnimationProperties();
                        animationProperties.push(AnimationProperty.Properties.ALPHA(0.5));
                        resizeRectangleButton.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                        resizeRectangle.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                        resizeArrow1.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                        resizeArrow2.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                        resizeArrow3.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                        resizeArrow4.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                        AnimationProperties animationProperties2 = new AnimationProperties();
                        animationProperties2.push(AnimationProperty.Properties.ALPHA(0));
                        rightArea.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                        leftArea.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                        bottomArea.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                        topArea.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);

                    }
                });

                resizeRectangleButton.addNodeMouseExitHandler(new NodeMouseExitHandler() {
                    @Override
                    public void onNodeMouseExit(NodeMouseExitEvent event) {
                        // Apply alphas.
                        AnimationProperties animationProperties = new AnimationProperties();
                        animationProperties.push(AnimationProperty.Properties.ALPHA(RECTANGLE_INITIA_ALPHA));
                        resizeRectangleButton.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);

                        // Apply alphas.
                        AnimationProperties animationProperties2 = new AnimationProperties();
                        animationProperties2.push(AnimationProperty.Properties.ALPHA(0));
                        resizeRectangle.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                        resizeArrow1.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                        resizeArrow2.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                        resizeArrow3.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                        resizeArrow4.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);

                        AnimationProperties animationProperties3 = new AnimationProperties();
                        animationProperties3.push(AnimationProperty.Properties.ALPHA(1));
                        rightArea.animate(AnimationTweener.LINEAR, animationProperties3, RECTANGLE_ANIMATION_DURATION);
                        leftArea.animate(AnimationTweener.LINEAR, animationProperties3, RECTANGLE_ANIMATION_DURATION);
                        bottomArea.animate(AnimationTweener.LINEAR, animationProperties3, RECTANGLE_ANIMATION_DURATION);
                        topArea.animate(AnimationTweener.LINEAR, animationProperties3, RECTANGLE_ANIMATION_DURATION);
                    }
                });

                resizeRectangleButton.addNodeDragStartHandler(new NodeDragStartHandler() {
                    @Override
                    public void onNodeDragStart(NodeDragStartEvent event) {
                        initialXPosition = event.getX();
                        initialYPosition = event.getY();
                    }
                });

                resizeRectangleButton.addNodeDragEndHandler(new NodeDragEndHandler() {
                    @Override
                    public void onNodeDragEnd(NodeDragEndEvent event) {
                        int currentX = event.getX();
                        int currentY = event.getY();
                        int incrementX = currentX - initialXPosition;
                        int incrementY = currentY - initialYPosition;
                        initialXPosition = currentX;
                        initialYPosition = currentY;
                        double finalWidth = getOriginalWidth() + incrementX;
                        double finalHeight = getOriginalHeight() + incrementY;
                        Double chartWidth = getChartWidth(finalWidth);
                        Double chartHeight = getChartHeight(finalHeight);

                        // Apply scale to chart area.
                        AnimationProperties animationProperties = new AnimationProperties();
                        animationProperties.push(AnimationProperty.Properties.WIDTH(finalWidth));
                        animationProperties.push(AnimationProperty.Properties.HEIGHT(finalHeight));
                        IAnimationHandle chartAnimationHandle = AbstractChart.this.animate(AnimationTweener.LINEAR, animationProperties, ANIMATION_DURATION, new IAnimationCallback() {
                            @Override
                            public void onStart(IAnimation animation, IAnimationHandle handle) {
                            }

                            @Override
                            public void onFrame(IAnimation animation, IAnimationHandle handle) {

                            }

                            @Override
                            public void onClose(IAnimation animation, IAnimationHandle handle) {

                            }
                        });

                        // Animate the resize rectangle to its final position.
                        AnimationProperties rectangleAnimationProperties = new AnimationProperties();
                        rectangleAnimationProperties.push(AnimationProperty.Properties.X(chartWidth - RECTANGLE_SIZE));
                        rectangleAnimationProperties.push(AnimationProperty.Properties.Y(chartHeight - RECTANGLE_SIZE));
                        IAnimationHandle rectangleAnimationHandle = resizeRectangleButton.animate(AnimationTweener.LINEAR, rectangleAnimationProperties, ANIMATION_DURATION);
                    }
                });

                resizeRectangleButton.addNodeDragMoveHandler(new NodeDragMoveHandler() {
                    @Override
                    public void onNodeDragMove(NodeDragMoveEvent event) {
                        int currentX = event.getX();
                        int currentY = event.getY();
                        int incrementX = currentX - initialXPosition;
                        int incrementY = currentY - initialYPosition;
                        double finalWidth = getOriginalWidth() + incrementX;
                        double finalHeight = getOriginalHeight() + incrementY;
                        Double chartWidth = getChartWidth(finalWidth);
                        Double chartHeight = getChartHeight(finalHeight);
                        resizeRectangle.setWidth(chartWidth).setHeight(chartHeight);
                        Point2D start = new Point2D(chartWidth / 2, chartHeight / 2);
                        resizeArrow1.setStart(start).setEnd(new Point2D(chartWidth, chartHeight / 2));
                        resizeArrow2.setStart(start).setEnd(new Point2D(chartWidth / 2, chartHeight));
                        resizeArrow3.setStart(start).setEnd(new Point2D(0, chartHeight / 2));
                        resizeArrow4.setStart(start).setEnd(new Point2D(chartWidth / 2, 0));
                        LayerRedrawManager.get().schedule(getLayer());
                    }
                });

            }

            chartArea.add(resizeRectangle);
            chartArea.add(resizeArrow1);
            chartArea.add(resizeArrow2);
            chartArea.add(resizeArrow3);
            chartArea.add(resizeArrow4);
            chartArea.add(resizeRectangleButton);
        }

        public void moveToTop() {
            resizeRectangle.moveToTop();
            resizeRectangleButton.moveToTop();
        }
    }

    public double getChartHeight(double originalHeight) {
        return originalHeight - (AREA_PADDING * 2);
    }

    public double getChartWidth(double originalWidth) {
        return originalWidth - (AREA_PADDING * 2);
    }

    public double getChartHeight() {
        return getChartHeight(getOriginalHeight());
    }

    public double getChartWidth() {
        return getChartWidth(getOriginalWidth());
    }

    protected double getOriginalHeight() {
        return getAttributes().getHeight();
    }

    protected double getOriginalWidth() {
        return getAttributes().getWidth();
    }

    public String getFontFamily() {
        return getAttributes().getFontFamily();
    }

    public String getFontStyle() {
        return getAttributes().getFontStyle();
    }

    public double getFontSize() {
        return getAttributes().getFontSize();
    }

    public AbstractChart setWidth(double width) {
        getAttributes().setWidth(width);
        return this;
    }

    public AbstractChart setHeight(double height) {
        getAttributes().setHeight(height);
        return this;
    }

    public AbstractChart setFontFamily(String f) {
        getAttributes().setFontFamily(f);
        return this;
    }

    public AbstractChart setFontSize(int size) {
        getAttributes().setFontSize(size);
        return this;
    }

    public AbstractChart setFontStyle(String f) {
        getAttributes().setFontStyle(f);
        return this;
    }

    public T setAlignment(ChartAlign chartAlignment)
    {
        if (null != chartAlignment)
        {
            getAttributes().put(ChartAttribute.ALIGN.getProperty(), chartAlignment.getValue());
        }
        else
        {
            getAttributes().delete(ChartAttribute.ALIGN.getProperty());
        }
        return (T) this;
    }

    public final ChartAlign getAlignment()
    {
        return ChartAlign.lookup(getAttributes().getString(ChartAttribute.ALIGN.getProperty()));
    }

    public T setOrientation(ChartOrientation chartOrientation)
    {
        if (null != chartOrientation)
        {
            getAttributes().put(ChartAttribute.ORIENTATION.getProperty(), chartOrientation.getValue());
        }
        else
        {
            getAttributes().delete(ChartAttribute.ORIENTATION.getProperty());
        }
        return (T) this;
    }

    public final ChartOrientation getOrientation()
    {
        return ChartOrientation.lookup(getAttributes().getString(ChartAttribute.ORIENTATION.getProperty()));
    }

    public T setDirection(ChartDirection chartDirection)
    {
        if (null != chartDirection)
        {
            getAttributes().put(ChartAttribute.DIRECTION.getProperty(), chartDirection.getValue());
        }
        else
        {
            getAttributes().delete(ChartAttribute.DIRECTION.getProperty());
        }
        return (T) this;
    }

    public final ChartDirection getDirection()
    {
        return ChartDirection.lookup(getAttributes().getString(ChartAttribute.DIRECTION.getProperty()));
    }
    
    
    public T setLegendPosition(LegendPosition legendPosition)
    {
        if (null != legendPosition)
        {
            getAttributes().put(ChartAttribute.LEGEND_POSITION.getProperty(), legendPosition.getValue());
        }
        else
        {
            getAttributes().delete(ChartAttribute.LEGEND_POSITION.getProperty());
        }
        return (T) this;
    }

    public final LegendPosition getLegendPosition()
    {
        return LegendPosition.lookup(getAttributes().getString(ChartAttribute.LEGEND_POSITION.getProperty()));
    }

    public T setLegendAlignment(LegendAlign legendAlign)
    {
        if (null != legendAlign)
        {
            getAttributes().put(ChartAttribute.LEGEND_ALIGN.getProperty(), legendAlign.getValue());
        }
        else
        {
            getAttributes().delete(ChartAttribute.LEGEND_ALIGN.getProperty());
        }
        return (T) this;
    }

    public final LegendAlign getLegendAlignment()
    {
        return LegendAlign.lookup(getAttributes().getString(ChartAttribute.LEGEND_ALIGN.getProperty()));
    }

    public final T setShowTitle(boolean showTitle)
    {
        getAttributes().put(ChartAttribute.SHOW_TITLE.getProperty(), showTitle);
        return (T) this;
    }

    public final boolean isShowTitle()
    {
        if (getAttributes().isDefined(ChartAttribute.SHOW_TITLE))
        {
            return getAttributes().getBoolean(ChartAttribute.SHOW_TITLE.getProperty());
        }
        return true;
    }

    public final T setResizable(boolean resizable)
    {
        getAttributes().put(ChartAttribute.RESIZABLE.getProperty(), resizable);
        return (T) this;
    }

    public final boolean isResizable()
    {
        if (getAttributes().isDefined(ChartAttribute.RESIZABLE))
        {
            return getAttributes().getBoolean(ChartAttribute.RESIZABLE.getProperty());
        }
        return true;
    }
    
    protected class LabelRendererFormatter {
        private List<AxisBuilder.AxisLabel> labels;
        private Text[] labelTexts;

        public LabelRendererFormatter(List<AxisBuilder.AxisLabel> labels, Text[] labelTexts) {
            this.labels = labels;
            this.labelTexts = labelTexts;
        }

        /**
         * Formats the label Text shapes in the given axis using the <code>visibility</code> attribute.
         */
        public void visibility(int index, double width, boolean animate) {
            if (labels != null && !labels.isEmpty()) {
                    AxisBuilder.AxisLabel lastVisibleLabel = null;
                    Text lastVisibleText = null;
                    if (index > 0)  {
                        int last = 1;
                        lastVisibleText = labelTexts[index - last];
                        while (lastVisibleText != null && !lastVisibleText.isVisible()) {
                            lastVisibleText = labelTexts[index - ++last];
                        }
                        lastVisibleLabel = labels.get(index - last);

                    }
                    AxisBuilder.AxisLabel label = labels.get(index);
                    double position = label.getPosition();
                    String text = label.getText();
                    Text intervalText = labelTexts[index];
                    final double lastTextWidth = lastVisibleText != null ? lastVisibleText.getBoundingBox().getWidth() : 0;
                    final double textWidth = intervalText.getBoundingBox().getWidth();
                    intervalText.setText(text);
                    // If labels are overlapped, do not show it.
                    if (lastVisibleLabel != null && lastVisibleLabel.getPosition() + lastTextWidth > label.getPosition()) {
                        intervalText.setVisible(false);
                    } else {
                        intervalText.setVisible(true);
                        double xPos = (index>0 && index < (labels.size() -1) ) ? position - textWidth/2 : position;
                        setShapeAttributes(intervalText, xPos, 10d, null, width, animate);
                    }
            }
        }

        /**
         * Formats the label Text shapes in the given axis by cutting text value.
         */
        public void cut(Text label, double width) {
            cut(label, width, 1);
        }

        private void cut(Text label, double width, int iteration) {
            String text = label.getText();
            if (text != null && label.getBoundingBox().getWidth() > width) {
                int cutLength = text.length() - iteration;
                if (cutLength < 0) cutLength = 1;
                String cuttedText = text.substring(0, cutLength);
                label.setText(cuttedText + "..");
                if (cuttedText.length() > 1) cut(label, width, ++iteration);
            }
        }
        
    }

}
