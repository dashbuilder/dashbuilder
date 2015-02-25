package com.ait.lienzo.charts.client;

import com.ait.lienzo.charts.client.resizer.ChartResizeEvent;
import com.ait.lienzo.charts.client.resizer.ChartResizeEventHandler;
import com.ait.lienzo.charts.client.resizer.ChartResizer;
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
import com.google.gwt.json.client.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

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
    protected static final double ANIMATION_DURATION = 1000;
    // Default animation duration when clearing chart.
    public static final double CLEAR_ANIMATION_DURATION = 500;

    public static final double DEFAULT_MARGIN  = 50;

    // The available areas: chart, top, bottom, left and right. 
    protected final Group chartArea = new Group();
    protected final Group topArea = new Group();
    protected final Group bottomArea = new Group();
    protected final Group rightArea = new Group();
    protected final Group leftArea = new Group();
    protected ChartResizer resizer = null;
    protected Text chartTitle = null;
    protected final Boolean[] isReloading = new Boolean[1];

    protected IAttributesChangedBatcher attributesChangedBatcher = new AnimationFrameAttributesChangedBatcher();

    protected AbstractChart() {
        isReloading[0] = false;
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
            addAttribute(ChartAttribute.MARGIN_LEFT, false);
            addAttribute(ChartAttribute.MARGIN_RIGHT, false);
            addAttribute(ChartAttribute.MARGIN_BOTTOM, false);
            addAttribute(ChartAttribute.MARGIN_TOP, false);
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
            addAttribute(ChartAttribute.ANIMATED, true);
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
        moveAreas(0d, 0d);
        
        // Chart title.
        buildTitle();

        // Call parent build implementation.
        doBuild();
        
        // Add the resizer.
        buildResizer();

        this.setAttributesChangedBatcher(attributesChangedBatcher);

        AttributesChangedHandler xyhandler = new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                if (!isReloading[0]) {
                    moveAreas(getX(), getY());
                }
            }
        };
        
        // Attribute change handlers.
        this.addAttributesChangedHandler(Attribute.X, xyhandler);
        this.addAttributesChangedHandler(Attribute.Y, xyhandler);

        AttributesChangedHandler whhandler = new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                if (!isReloading[0]) {
                    redraw(getChartWidth(), getChartHeight(), false);
                }
            }
        };
        
        
        this.addAttributesChangedHandler(Attribute.WIDTH, whhandler);
        this.addAttributesChangedHandler(Attribute.HEIGHT, whhandler);

        return (T) this;
    }

    protected T redraw(final Double chartWidth, final Double chartHeight, final boolean animate) {
        // Move areas using new width and height.
        setGroupAttributes(bottomArea, null, topArea.getY() + getChartHeight() + getMarginTop(), animate);
        setGroupAttributes(rightArea, getX() + getChartWidth() + getMarginLeft(), null, animate);
        
        // Chart title.
        buildTitle();

        // Add the resizer.
        buildResizer();
        
        return (T) this;
    }
    
    protected void buildResizer() {
        if (isResizable()) {
            resizer = new ChartResizer(getWidth(), getHeight()).moveToTop();
            resizer.addChartResizeEventHandler(new ChartResizeEventHandler() {
                @Override
                public void onChartResize(ChartResizeEvent event) {
                    final double w = event.getWidth();
                    final double h = event.getHeight();
                    // Apply scale to chart area.
                    AnimationProperties animationProperties = new AnimationProperties();
                    animationProperties.push(AnimationProperty.Properties.WIDTH(w));
                    animationProperties.push(AnimationProperty.Properties.HEIGHT(h));
                    AbstractChart.this.animate(AnimationTweener.LINEAR, animationProperties, ANIMATION_DURATION);
                }
            });
            
            chartArea.add(resizer);
        }
    }
    
    protected void buildTitle() {
        if (isShowTitle()) {
            chartTitle = new Text(getName(), getFontFamily(), getFontStyle(), getFontSize()).setFillColor(ColorName.BLACK).setTextAlign(TextAlign.CENTER).setTextBaseLine(TextBaseLine.MIDDLE);
            setShapeAttributes(chartTitle,getChartWidth() / 2, 10d, null, null, true);
            topArea.add(chartTitle);
        }
    }

    protected void moveAreas(Double x, Double y) {
        if (x != null) {
            double marginLeft = getMarginLeft();
            leftArea.setX(x);
            topArea.setX(x + marginLeft);
            chartArea.setX(x + marginLeft);
            bottomArea.setX(x + marginLeft);
            rightArea.setX(x + getChartWidth() + marginLeft);
        }
        if (y != null) {
            double marginTop = getMarginTop();
            topArea.setY(y);
            chartArea.setY(y + marginTop);
            leftArea.setY(y + marginTop);
            rightArea.setY(y + marginTop);
            bottomArea.setY(y + getChartHeight() + marginTop);
        }
        chartArea.moveToTop();
    }
    
    protected abstract void doBuild();

    protected void clearAreas() {
        topArea.removeAll();
        bottomArea.removeAll();
        leftArea.removeAll();
        rightArea.removeAll();
        chartArea.removeAll();
    }
    
    protected T setGroupAttributes(Group group, Double x, Double y, boolean animate) {
        return setGroupAttributes(group, x, y, null, animate);
    }

    protected T setGroupAttributes(Group group, Double x, Double y, Double alpha, boolean animate) {
        if (x != null) group.setX(x);
        if (y != null) group.setY(y);
        if (alpha != null) group.setAlpha(alpha);
        return (T) this;
    }

    protected void setShapeAttributes(Shape shape, Double x, Double y, Double width, Double height, boolean animate) {
        Map<Attribute, Object> attributes = new LinkedHashMap<Attribute, Object>(4);
        if (x != null) attributes.put(Attribute.X, x);
        if (y != null) attributes.put(Attribute.Y, y);
        if (width != null) attributes.put(Attribute.WIDTH, width);
        if (height != null) attributes.put(Attribute.HEIGHT, height);
        setShapeAttributes(shape, attributes, animate);
    }

    protected void setShapeAttributes(Shape shape, Double alpha, boolean animate) {
        Map<Attribute, Object> attributes = new LinkedHashMap<Attribute, Object>(1);
        if (alpha != null) attributes.put(Attribute.ALPHA, alpha);
        setShapeAttributes(shape, attributes, animate);
    }

    protected void setShapeAttributes(Shape shape, Double radius, Double startAngle, Double endAngle, boolean animate) {
        Map<Attribute, Object> attributes = new LinkedHashMap<Attribute, Object>(4);
        if (radius != null) attributes.put(Attribute.RADIUS, radius);
        if (startAngle != null) attributes.put(Attribute.START_ANGLE, startAngle);
        if (endAngle != null) attributes.put(Attribute.END_ANGLE, endAngle);
        setShapeAttributes(shape, attributes, animate);
    }

    protected void setShapeAttributes(Shape shape, Double x, Double y, Double width, Double height, IColor color, Double alpha, boolean animate) {
        Map<Attribute, Object> attributes = new LinkedHashMap<Attribute, Object>(6);
        if (x != null) attributes.put(Attribute.X, x);
        if (y != null) attributes.put(Attribute.Y, y);
        if (width != null) attributes.put(Attribute.WIDTH, width);
        if (height != null) attributes.put(Attribute.HEIGHT, height);
        if (color != null) attributes.put(Attribute.FILL, color);
        if (alpha != null) attributes.put(Attribute.ALPHA, alpha);
        setShapeAttributes(shape, attributes, animate);
    }

    protected void setShapeAttributes(Shape shape, Double x, Double y, Double width, Double height, IColor color, Double alpha, boolean animate, double duration, IAnimationCallback callback) {
        Map<Attribute, Object> attributes = new LinkedHashMap<Attribute, Object>(6);
        if (x != null) attributes.put(Attribute.X, x);
        if (y != null) attributes.put(Attribute.Y, y);
        if (width != null) attributes.put(Attribute.WIDTH, width);
        if (height != null) attributes.put(Attribute.HEIGHT, height);
        if (color != null) attributes.put(Attribute.FILL, color);
        if (alpha != null) attributes.put(Attribute.ALPHA, alpha);
        setShapeAttributes(shape, attributes, animate, duration, callback);
    }

    protected void setShapeAttributes(Shape shape, Point2D scale, boolean animate) {
        Map<Attribute, Object> attributes = new LinkedHashMap<Attribute, Object>(1);
        if (scale != null) attributes.put(Attribute.SCALE, scale);
        setShapeAttributes(shape, attributes, animate);
    }

    protected void setShapeAttributes(Shape shape, Map<Attribute, Object> attributes, boolean animate) {
        setShapeAttributes(shape, attributes, animate, 0, null);
    }
    
    protected void setShapeAttributes(Shape shape, Map<Attribute, Object> attributes, boolean animate, double duration, IAnimationCallback callback) {
        
        if (attributes != null && !attributes.isEmpty()) {
            AnimationProperties animationProperties = new AnimationProperties();
            for (Map.Entry<Attribute, Object> entry : attributes.entrySet()) {
                Attribute attribute = entry.getKey();
                String property = attribute.getProperty();
                Object value = entry.getValue();
                
                if (Attribute.WIDTH.getProperty().equals(property)) {
                    if (animate) animationProperties.push(AnimationProperty.Properties.WIDTH((Double) value));
                    else shape.getAttributes().setWidth((Double) value);
                }
                else if (Attribute.HEIGHT.getProperty().equals(property)) {
                    if (animate) animationProperties.push(AnimationProperty.Properties.HEIGHT((Double) value));
                    else shape.getAttributes().setHeight((Double) value);
                }
                else if (Attribute.X.getProperty().equals(property)) {
                    if (animate) animationProperties.push(AnimationProperty.Properties.X((Double) value));
                    else shape.setX((Double) value);
                }
                else if (Attribute.Y.getProperty().equals(property)) {
                    if (animate) animationProperties.push(AnimationProperty.Properties.Y((Double) value));
                    else shape.setY((Double) value);
                }
                else if (Attribute.FILL.getProperty().equals(property)) {
                    if (animate) animationProperties.push(AnimationProperty.Properties.FILL_COLOR((IColor) value));
                    else shape.setFillColor((IColor) value);
                }
                else if (Attribute.ALPHA.getProperty().equals(property)) {
                    if (animate) animationProperties.push(AnimationProperty.Properties.ALPHA((Double) value));
                    else shape.setAlpha((Double) value);
                }
                else if (Attribute.RADIUS.getProperty().equals(property)) {
                    if (animate) animationProperties.push(AnimationProperty.Properties.RADIUS((Double) value));
                    else shape.getAttributes().setRadius((Double) value);
                }
                else if (Attribute.START_ANGLE.getProperty().equals(property)) {
                    //if (animate) animationProperties.push(AnimationProperty.Properties.START_ANGLE((Double) value)); else 
                    shape.getAttributes().setStartAngle((Double) value);
                }
                else if (Attribute.END_ANGLE.getProperty().equals(property)) {
                    // if (animate) animationProperties.push(AnimationProperty.Properties.END_ANGLE((Double) value)); else 
                    shape.getAttributes().setEndAngle((Double) value);
                }
                else if (Attribute.SCALE.getProperty().equals(property)) {
                    if (animate) animationProperties.push(AnimationProperty.Properties.SCALE((Point2D) value)); 
                    else shape.getAttributes().setScale((Double) value);
                }
            }

            if (animate && callback == null) shape.animate(AnimationTweener.LINEAR, animationProperties, ANIMATION_DURATION);
            if (animate && callback != null) shape.animate(AnimationTweener.LINEAR, animationProperties, duration, callback);

        }
    }

    protected double getChartHeight(double originalHeight) {
        return originalHeight - (getMarginTop() + getMarginBottom());
    }

    protected double getChartWidth(double originalWidth) {
        return originalWidth - (getMarginLeft() + getMarginRight());
    }

    public double getChartHeight() {
        return getAttributes().getHeight();
    }

    public double getChartWidth() {
        return getAttributes().getWidth();
    }

    public double getHeight() {
        return getAttributes().getHeight() +  getMarginTop() + getMarginBottom();
    }

    public double getWidth() {
        return getAttributes().getWidth() + getMarginLeft() + getMarginRight();
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

    public final T setAnimated(boolean animated)
    {
        getAttributes().put(ChartAttribute.ANIMATED.getProperty(), animated);
        return (T) this;
    }

    public final boolean isAnimated()
    {
        if (getAttributes().isDefined(ChartAttribute.ANIMATED))
        {
            return getAttributes().getBoolean(ChartAttribute.ANIMATED.getProperty());
        }
        return true;
    }

    public final double getMarginLeft()
    {
        if (getAttributes().isDefined(ChartAttribute.MARGIN_LEFT))
        {
            return getAttributes().getDouble(ChartAttribute.MARGIN_LEFT.getProperty());
        }
        return DEFAULT_MARGIN;
    }

    public final T setMarginLeft(final double margin)
    {
        getAttributes().put(ChartAttribute.MARGIN_LEFT.getProperty(), margin);
        return (T) this;
    }

    public final double getMarginTop()
    {
        if (getAttributes().isDefined(ChartAttribute.MARGIN_TOP))
        {
            return getAttributes().getDouble(ChartAttribute.MARGIN_TOP.getProperty());
        }
        return DEFAULT_MARGIN;
    }

    public final T setMarginTop(final double margin)
    {
        getAttributes().put(ChartAttribute.MARGIN_TOP.getProperty(), margin);
        return (T) this;
    }

    public final double getMarginRight()
    {
        if (getAttributes().isDefined(ChartAttribute.MARGIN_RIGHT))
        {
            return getAttributes().getDouble(ChartAttribute.MARGIN_RIGHT.getProperty());
        }
        return DEFAULT_MARGIN;
    }

    public final T setMarginRight(final double margin)
    {
        getAttributes().put(ChartAttribute.MARGIN_RIGHT.getProperty(), margin);
        return (T) this;
    }

    public final double getMarginBottom()
    {
        if (getAttributes().isDefined(ChartAttribute.MARGIN_BOTTOM))
        {
            return getAttributes().getDouble(ChartAttribute.MARGIN_BOTTOM.getProperty());
        }
        return DEFAULT_MARGIN;
    }

    public final T setMarginBotom(final double margin)
    {
        getAttributes().put(ChartAttribute.MARGIN_BOTTOM.getProperty(), margin);
        return (T) this;
    }
}
