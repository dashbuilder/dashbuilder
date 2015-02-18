/*
   Copyright (c) 2014 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.charts.client.xy;

import com.ait.lienzo.charts.client.AbstractChart;
import com.ait.lienzo.charts.client.ChartAttribute;
import com.ait.lienzo.charts.client.ChartNodeType;
import com.ait.lienzo.charts.client.axis.Axis;
import com.ait.lienzo.charts.client.axis.CategoryAxis;
import com.ait.lienzo.charts.client.xy.axis.AxisBuilder;
import com.ait.lienzo.charts.client.xy.axis.CategoryAxisBuilder;
import com.ait.lienzo.charts.client.xy.axis.NumericAxisBuilder;
import com.ait.lienzo.charts.shared.core.types.ChartDirection;
import com.ait.lienzo.charts.shared.core.types.ChartOrientation;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.event.*;
import com.ait.lienzo.client.core.shape.*;
import com.ait.lienzo.client.core.shape.json.IFactory;
import com.ait.lienzo.client.core.shape.json.validators.ValidationContext;
import com.ait.lienzo.client.core.shape.json.validators.ValidationException;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>XY chart implementation using rectangles as shapes for values.</p>
 * <p>It can be drawn as a bar chart or a column chart depending on the <code>CHART_ORIENTATION</code> attribute.</p>
 *  
 * <p>Attributes:</p>
 * <ul>
 *     <li><code>X</code>: The chart X position.</li>
 *     <li><code>Y</code>: The chart Y position.</li>
 *     <li><code>WIDTH</code>: The chart width.</li>
 *     <li><code>HEIGHT</code>: The chart height.</li>
 *     <li><code>NAME</code>: The chart name, used as title.</li>
 *     <li><code>SHOW_TITLE</code>: Flag for title visibility.</li>
 *     <li><code>FONT_FAMILY</code>: The chart font family.</li>
 *     <li><code>FONT_STYLE</code>: The chart font style.</li>
 *     <li><code>FONT_SIZE</code>: The chart font size.</li>
 *     <li><code>RESIZABLE</code>: Add or avoid the use of the chart resizer.</li>
 *     <li><code>LEGEND_POSITION</code>: The chart legend position.</li>
 *     <li><code>LEGEND_ALIGN</code>: The chart legend alignment.</li>
 *     <li><code>XY_CHART_DATA</code>: The chart data.</li>
 *     <li><code>CATEGORY_AXIS</code>: The chart category axis.</li>
 *     <li><code>VALUES_AXIS</code>: The chart values axis.</li>
 *     <li><code>ALIGN</code>: The chart alignment.</li>
 *     <li><code>DIRECTION</code>: The chart direction.</li>
 *     <li><code>ORIENTATION</code>: The chart orientation (Bar or Column).</li>
 * </ul>
 * 
 * <p>It listens the <code>AttributesChangedEvent</code> for attribute <code>XY_CHART_DATA</code>.</p> 
 */
public class BarChart extends AbstractChart<BarChart>
{
    // Default separation size between bars.
    protected static final double BAR_SEPARATION = 2;

    protected BarChart(JSONObject node, ValidationContext ctx) throws ValidationException
    {
        super(node, ctx);

        setNodeType(ChartNodeType.BAR_CHART);
    }

    public BarChart(XYChartData data)
    {
        setNodeType(ChartNodeType.BAR_CHART);

        setData(data);

        getMetaData().put("creator", "Roger Martinez");
    }

    public final BarChart setCategoriesAxis(CategoryAxis xAxis)
    {
        if (null != xAxis)
        {
            getAttributes().put(ChartAttribute.CATEGORIES_AXIS.getProperty(), xAxis.getJSO());
        }
        else
        {
            getAttributes().delete(ChartAttribute.CATEGORIES_AXIS.getProperty());
        }
        return this;
    }

    protected final Axis.AxisJSO getCategoriesAxis()
    {
        return getAttributes().getObject(ChartAttribute.CATEGORIES_AXIS.getProperty()).cast();
    }

    public final BarChart setValuesAxis(Axis yAxis)
    {
        if (null != yAxis)
        {
            getAttributes().put(ChartAttribute.VALUES_AXIS.getProperty(), yAxis.getJSO());
        }
        else
        {
            getAttributes().delete(ChartAttribute.VALUES_AXIS.getProperty());
        }
        return this;
    }

    protected final Axis.AxisJSO getValuesAxis()
    {
        return getAttributes().getObject(ChartAttribute.VALUES_AXIS.getProperty()).cast();
    }

    public final BarChart setData(XYChartData data)
    {
        if (null != data)
        {
            getAttributes().put(ChartAttribute.XY_CHART_DATA.getProperty(), data.getJSO());
        }
        else
        {
            getAttributes().delete(ChartAttribute.XY_CHART_DATA.getProperty());
        }
        return this;
    }

    public final XYChartData getData()
    {
        XYChartData.XYChartDataJSO jso = getAttributes().getArrayOfJSO(ChartAttribute.XY_CHART_DATA.getProperty()).cast();

        return new XYChartData(jso);
    }
   
    
    @Override
    public JSONObject toJSONObject()
    {
        JSONObject object = new JSONObject();

        object.put("type", new JSONString(getNodeType().getValue()));

        if (!getMetaData().isEmpty())
        {
            object.put("meta", new JSONObject(getMetaData().getJSO()));
        }
        object.put("attributes", new JSONObject(getAttributes().getJSO()));

        return object;
    }

    @Override
    public IFactory<Group> getFactory()
    {
        return new BarChartFactory();
    }

    public static class BarChartFactory extends ChartFactory
    {
        public BarChartFactory()
        {
            super();
            setTypeName(ChartNodeType.BAR_CHART.getValue());
            addAttribute(ChartAttribute.XY_CHART_DATA, true);
            addAttribute(ChartAttribute.CATEGORIES_AXIS, true);
            addAttribute(ChartAttribute.VALUES_AXIS, true);
        }

        @Override
        public boolean addNodeForContainer(IContainer<?, ?> container, Node<?> node, ValidationContext ctx)
        {
            return false;
        }

        @Override
        public BarChart create(JSONObject node, ValidationContext ctx) throws ValidationException
        {
            return new BarChart(node, ctx);
        }
    }
    
    protected static boolean isVertical(ChartOrientation orientation) {
        return ChartOrientation.VERTICAL.equals(orientation);
    }

    protected static boolean isPositiveDirection(ChartDirection direction) {
        return ChartDirection.POSITIVE.equals(direction);
    }

    protected void doBuild()
    {

        ChartOrientation orientation = getOrientation();
        
        final BarChartBuilder builder = (isVertical(orientation)) ? new VerticalBarChartBuilder() : new HorizontalBarChartBuilder();
        
        // **** Build all shape instances. ****
        
        // Build the categories axis title shape (Text)
        builder.buildCategoriesAxisTitle()
        // Build the values axis title shape (Text)
                .buildValuesAxisTitle()
        // Build the categories axis intervals shapes (Text and Line)
                .buildCategoriesAxisIntervals()
        // Build the values axis intervals shapes (Text and Line)
                .buildValuesAxisIntervals()
        // Build the values shapes (Rectangles)
                .buildValues();
        

        // Set positions and sizes for shapes.
        Double chartWidth = getChartWidth();
        Double chartHeight = getChartHeight();
        redraw(builder, chartWidth, chartHeight, true);
        
        // Add the attributes event change handlers.
        this.addAttributesChangedHandler(ChartAttribute.XY_CHART_DATA, new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                GWT.log("BarChart - XYData attribute changed.");
                redraw(builder, getChartWidth(), getChartHeight(), true);
                LayerRedrawManager.get().schedule(getLayer());
            }
        });

        this.addAttributesChangedHandler(ChartAttribute.WIDTH, new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                GWT.log("BarChart - WIDTH attribute changed.");
                redraw(builder, getChartWidth(), getChartHeight(), false);
            }
        });

        this.addAttributesChangedHandler(ChartAttribute.HEIGHT, new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                GWT.log("BarChart - WIDTH attribute changed.");
                redraw(builder, getChartWidth(), getChartHeight(), false);
            }
        });

    }
    
    private void redraw(BarChartBuilder builder, Double chartWidth, Double chartHeight, boolean animate) {
        
        // Reload axis builder as data has changed.
        builder.reloadBuilders()
        // Recalculate positions and size for categories axis title shape.
                .setCategoriesAxisTitleAttributes(chartWidth, chartHeight, animate)
        // Recalculate positions and size for categories intervals shapes.
                .setCategoriesAxisIntervalsAttributes(chartWidth, chartHeight, animate)
        // Recalculate positions and size for values axis title shape.
                .setValuesAxisTitleAttributes(chartWidth, chartHeight, animate)
        // Recalculate positions and size for values intervals shapes.
                .setValuesAxisIntervalsAttributes(chartWidth, chartHeight, animate)
        // Recalculate positions, size and add or remove rectangles (if data has changed).
                .setValuesAttributes(chartWidth, chartHeight, animate);
    }
    
    
    private abstract class BarChartBuilder<T extends BarChartBuilder> {

        final AxisBuilder[] categoriesAxisBuilder = new AxisBuilder[1];
        final AxisBuilder[] valuesAxisBuilder = new AxisBuilder[1];
        Axis.AxisJSO categoriesAxisJSO;
        Axis.AxisJSO valuesAxisJSO;
        Text categoriesAxisTitle;
        Text valuesAxisTitle;
        Line[] valuesAxisIntervals; // The lines that represents the intervals in the Y axis.
        Text[] valuesAxisIntervalLabels; // The texts that represents the interval values in the Y axis.
        final Map<String, List<Rectangle>> seriesValues = new LinkedHashMap(); // The rectangles that represents the data.
        final List<Text> seriesLabels = new LinkedList(); // The labels for each interval (rectangle) in the X axis.

        public BarChartBuilder() {
           
        }
        
        public AxisBuilder getCategoriesAxisBuilder() {
            return categoriesAxisBuilder[0];
        }

        public AxisBuilder getValuesAxisBuilder() {
            return valuesAxisBuilder[0];
        }
        
        public Axis.AxisJSO getCategoriesAxis() {
            return categoriesAxisJSO;
        }
        
        public Axis.AxisJSO getValuesAxis() {
            return valuesAxisJSO;
        }
        
        public abstract T buildCategoriesAxisTitle();

        public abstract T buildValuesAxisTitle();

        public T buildCategoriesAxisIntervals() {
            List<AxisBuilder.AxisLabel> xAxisLabels = categoriesAxisBuilder[0].getLabels();
            if (xAxisLabels != null) {
                for (int i = 0; i < xAxisLabels.size(); i++) {
                    AxisBuilder.AxisLabel axisLabel = xAxisLabels.get(i);
                    Text serieLabel = new Text(axisLabel.getText(), AXIS_LABEL_DEFAULT_FONT_NAME, AXIS_LABEL_DEFAULT_FONT_STYLE, AXIS_LABEL_DEFAULT_FONT_SIZE).setFillColor(AXIS_LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.MIDDLE);
                    seriesLabels.add(serieLabel);
                    addCategoryAxisIntervalLabel(serieLabel);

                }
            }
            return (T) this;
        }
        
        protected abstract void addCategoryAxisIntervalLabel(Text label);
        
        
        public T buildValuesAxisIntervals() {
            // Build the shapes axis instances (line for intervals and text for labels).
            int yAxisDivisions = valuesAxisBuilder[0].getLabels().size();
            valuesAxisIntervals = new Line[yAxisDivisions + 1];
            valuesAxisIntervalLabels = new Text[yAxisDivisions + 1];
            for (int x = 0; x <= yAxisDivisions; x++) {
                valuesAxisIntervals[x] = new Line(0,0,0,0).setStrokeColor(AXIS_LABEL_COLOR);
                valuesAxisIntervalLabels[x] = new Text("", AXIS_LABEL_DEFAULT_FONT_NAME, AXIS_LABEL_DEFAULT_FONT_STYLE, AXIS_LABEL_DEFAULT_FONT_SIZE).setFillColor(AXIS_LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.MIDDLE);
                chartArea.add(valuesAxisIntervals[x]);
                addValuesAxisIntervalLabel(valuesAxisIntervalLabels[x]);
            }
            return (T) this;

        }

        protected abstract void addValuesAxisIntervalLabel(Text label);
        
        public T buildValues() {
            // Build the chart values as rectangle shapes.
            XYChartSerie[] series = getData().getSeries();
            for (int numSerie = 0; numSerie < series.length; numSerie++) {
                XYChartSerie serie = series[numSerie];
                buildSerieValues(serie);
            }
            return (T) this;
        }
        
        protected void buildSerieValues(XYChartSerie serie) {
            List<AxisBuilder.AxisValue> xAxisValues = categoriesAxisBuilder[0].getValues(getData().getCategoryAxisProperty());

            if (xAxisValues != null) {
                List<Rectangle> bars = new LinkedList();
                for (int i = 0; i < xAxisValues.size(); i++) {
                    AxisBuilder.AxisValue axisValue = xAxisValues.get(i);
                    final Rectangle bar = new Rectangle(0,0);

                    // Mouse events for the bar shape.
                    bar.addNodeMouseClickHandler(new NodeMouseClickHandler() {
                        @Override
                        public void onNodeMouseClick(NodeMouseClickEvent event) {
                            // GWT.log("X: " + bar.getX());
                            // GWT.log("Y: " + bar.getY());
                            // GWT.log("W: " + bar.getWidth());
                            // GWT.log("H: " + bar.getHeight());
                        }
                    });
                    bars.add(bar);
                    chartArea.add(bar);
                }
                seriesValues.put(serie.getName(), bars);

            }
            
        }

        protected void removeSerieValues(final String serieName) {
            if (serieName != null) {
                List<Rectangle> barsForSerie = seriesValues.get(serieName);
                if (seriesValues != null) {
                    for (final Rectangle bar : barsForSerie) {
                        setShapeAttributes(bar, null, null, 0d, 0d, true);
                    }
                }
                seriesValues.remove(serieName);
            }

        }
        
        public abstract T setCategoriesAxisTitleAttributes(Double width, Double height, boolean animate);
        public abstract T setValuesAxisTitleAttributes(Double width, Double height, boolean animate);
        public abstract T setCategoriesAxisIntervalsAttributes(Double width, Double height, boolean animate);
        public abstract T setValuesAxisIntervalsAttributes(Double width, Double height, boolean animate);

        public T setValuesAttributes(Double width, Double height, boolean animate) {
            XYChartSerie[] series = getData().getSeries();

            // Find removed series in order to remove bar rectangle instances.
            for (String removedSerieName : categoriesAxisBuilder[0].getDataSummary().getRemovedSeries()) {
                removeSerieValues(removedSerieName);
            }

            // Iterate over all series.
            for (int numSerie = 0; numSerie < series.length; numSerie++) {
                final XYChartSerie serie = series[numSerie];
                if (serie != null) {

                    // If a new serie is added, build new bar rectangle instances.
                    if (categoriesAxisBuilder[0].getDataSummary().getAddedSeries().contains(serie.getName())) {
                        buildCategoriesAxisIntervals();
                        buildSerieValues(serie);
                    }

                    setValuesAttributesForSerie(serie, numSerie, width, height, animate);
                }
            }
            return (T) this;
        }
        
        protected abstract T setValuesAttributesForSerie(final XYChartSerie serie, int numSerie, Double width, Double height, boolean animate);
        
        public abstract T reloadBuilders();
    }

    private class VerticalBarChartBuilder extends BarChartBuilder<VerticalBarChartBuilder> {

        public VerticalBarChartBuilder() {
            // Build categories axis builder.
            categoriesAxisJSO = BarChart.this.getCategoriesAxis();
            AxisBuilder.AxisDirection direction = isPositiveDirection(getDirection()) ? AxisBuilder.AxisDirection.DESC : AxisBuilder.AxisDirection.ASC;
            if (categoriesAxisJSO.getType().equals(Axis.AxisType.CATEGORY)) {
                categoriesAxisBuilder[0] = new CategoryAxisBuilder(getData(), getChartWidth(), direction, categoriesAxisJSO);
            } else if (categoriesAxisJSO.getType().equals(Axis.AxisType.NUMBER)) {
                categoriesAxisBuilder[0] = new NumericAxisBuilder(getData(), getChartWidth(),direction, categoriesAxisJSO);
            } else {
                // TODO: categoriesAxisBuilder = new DateAxisBuilder(getData(), xAxisJSO);
            }

            // Build values axis builder.
            valuesAxisJSO = BarChart.this.getValuesAxis();
            if (valuesAxisJSO.getType().equals(Axis.AxisType.CATEGORY)) {
                throw new RuntimeException("CategoryAxis type cannot be used in BarChart (vertical) for the values axis.");
            } else if (valuesAxisJSO.getType().equals(Axis.AxisType.NUMBER)) {
                valuesAxisBuilder[0] = new NumericAxisBuilder(getData(), getChartHeight(),direction, valuesAxisJSO);
            } else {
                // TODO: valuesAxisBuilder = new DateAxisBuilder(getData(), yAxisJSO);
            }
        }

        public VerticalBarChartBuilder buildCategoriesAxisTitle() {
            // Build the X axis line and title.
            categoriesAxisTitle = new Text(getCategoriesAxis().getTitle(), getFontFamily(), getFontStyle(), getFontSize()).setFillColor(ColorName.SILVER).setX(getChartWidth() / 2).setY(30).setTextAlign(TextAlign.CENTER).setTextBaseLine(TextBaseLine.MIDDLE);
            bottomArea.add(categoriesAxisTitle);
            return this;
        }

        public VerticalBarChartBuilder buildValuesAxisTitle() {
            // Build the Y axis line and title.
            valuesAxisTitle = new Text(getValuesAxis().getTitle(), getFontFamily(), getFontStyle(), getFontSize()).setFillColor(ColorName.SILVER).setX(10).setY(getChartHeight() / 2).setTextAlign(TextAlign.RIGHT).setTextBaseLine(TextBaseLine.MIDDLE).setRotationDegrees(270);
            leftArea.add(valuesAxisTitle);
            return this;
        }
        @Override
        protected void addCategoryAxisIntervalLabel(Text label) {
            bottomArea.add(label);
        }

        @Override
        protected void addValuesAxisIntervalLabel(Text label) {
            rightArea.add(label);
        }

        public VerticalBarChartBuilder setCategoriesAxisTitleAttributes(Double width, Double height, boolean animate) {
            setShapeAttributes(categoriesAxisTitle, width / 2, 30d, null, null, animate);
            return this;
        }
        
        public VerticalBarChartBuilder setValuesAxisTitleAttributes(Double width, Double height, boolean animate) {
            setShapeAttributes(valuesAxisTitle, null, height / 2, null, null, animate);
            return this;
        }
        
        @Override
        public VerticalBarChartBuilder setValuesAxisIntervalsAttributes(Double width, Double height, boolean animate) {
            List<AxisBuilder.AxisLabel> labels = valuesAxisBuilder[0].getLabels();

            for (int i = 0; i < labels.size(); i++) {
                AxisBuilder.AxisLabel label = labels.get(i);
                double position = label.getPosition();
                String text = label.getText();
                valuesAxisIntervals[i].setPoints(new Point2DArray(new Point2D(0, position), new Point2D(width, position)));
                valuesAxisIntervalLabels[i].setText(text);
                setShapeAttributes(valuesAxisIntervalLabels[i], null, position, width, null, animate);
            }
            return this;
        }

        public VerticalBarChartBuilder setCategoriesAxisIntervalsAttributes(Double width, Double height, boolean animate) {
            List<AxisBuilder.AxisLabel> labels = categoriesAxisBuilder[0].getLabels();

            for (int i = 0; i < labels.size(); i++) {
                AxisBuilder.AxisLabel label = labels.get(i);
                double position = label.getPosition();
                String text = label.getText();
                seriesLabels.get(i).setText(text);
                setShapeAttributes(seriesLabels.get(i), position, 10d, null, null, animate);
            }
            return this;
        }

        protected VerticalBarChartBuilder setValuesAttributesForSerie(final XYChartSerie serie, int numSerie, Double width, Double height, boolean animate) {
            XYChartSerie[] series = getData().getSeries();

            // Rebuild bars for serie values
            List<AxisBuilder.AxisValue> valuesAxisValues = valuesAxisBuilder[0].getValues(serie.getValuesAxisProperty());
            List<AxisBuilder.AxisValue> categoryAxisValues = categoriesAxisBuilder[0].getValues(getData().getCategoryAxisProperty());
            List<Rectangle> bars = seriesValues.get(serie.getName());

            if (categoryAxisValues != null && categoryAxisValues.size() > 0) {
                for (int i = 0; i < categoryAxisValues.size(); i++) {
                    AxisBuilder.AxisValue categoryAxisvalue = categoryAxisValues.get(i);
                    AxisBuilder.AxisValue valueAxisvalue = valuesAxisValues.get(i);
                    double yAxisValuePosition = valueAxisvalue.getPosition();
                    Object yValue = valueAxisvalue.getValue();
                    String yValueFormatted = valuesAxisBuilder[0].format(yValue);

                    // Obtain width and height values for the bar.
                    double barHeight = yAxisValuePosition;
                    double barWidth = getWithForBar(width, series.length, categoryAxisValues.size());

                    // Calculate bar positions.
                    double y = height - barHeight;
                    double x = (barWidth * series.length * i) + (barWidth * numSerie) + (getValuesAxis().getSegments() * (i +1));
                    double alpha = 1d;

                    // If current bar is not in Y axis intervals (max / min values), resize it and apply an alpha.
                    boolean isOutOfChartArea = y < 0;
                    if (isOutOfChartArea) {
                        alpha = 0.1d;
                        barHeight = getChartHeight();
                        y = 0;
                    }

                    // Obtain the shape instance, add mouse handlers and reposition/resize it.
                    Rectangle barObject = bars.get(i);
                    barObject.moveToTop();
                    barObject.setDraggable(true);
                    barObject.addNodeDragEndHandler(new NodeDragEndHandler() {
                        @Override
                        public void onNodeDragEnd(NodeDragEndEvent nodeDragEndEvent) {
                            double x = nodeDragEndEvent.getX();
                            double y = nodeDragEndEvent.getY();
                            if (x < chartArea.getX() || x > (chartArea.getX() + getChartWidth()) ||
                                    y < chartArea.getY() || y > (chartArea.getY() + getChartHeight())) {
                                // Remove the series from data.
                                XYChartData data = getData();
                                data.removeSerie(serie);
                                // Force firing attributes changed event in order to capture it and redraw the chart.
                                setData(data);
                            }
                        }
                    });
                    setShapeAttributes(barObject, x, y, barWidth - BAR_SEPARATION, barHeight, serie.getColor(), alpha, animate);
                }
            }
            return this;
        }

        public VerticalBarChartBuilder reloadBuilders() {
            // Rebuild data summary as columns, series and values can have been modified.
            categoriesAxisBuilder[0].reload(getData(), seriesValues.keySet(), getChartWidth());
            valuesAxisBuilder[0].reload(getData(), seriesValues.keySet(), getChartHeight());
            return this;
        }

        protected double getWithForBar(final double chartWidth, final int numSeries, final int valuesCount) {
            // If exist more than one serie, and no stacked attribute is set, split each serie bar into the series count value.
            return getAvailableWidth(chartWidth, valuesCount) / valuesCount / numSeries;
        }

        protected double getAvailableWidth(final double chartWidth, final int valuesCount) {
            int yAxisDivisions = getValuesAxis().getSegments();
            return chartWidth - (yAxisDivisions * (valuesCount+1) );
        }
        
    }

    private class HorizontalBarChartBuilder extends BarChartBuilder<HorizontalBarChartBuilder> {

        public HorizontalBarChartBuilder() {
            // Build X axis builder.
            valuesAxisJSO = BarChart.this.getValuesAxis();
            AxisBuilder.AxisDirection direction = isPositiveDirection(getDirection()) ? AxisBuilder.AxisDirection.ASC : AxisBuilder.AxisDirection.DESC;
            if (valuesAxisJSO.getType().equals(Axis.AxisType.CATEGORY)) {
                throw new RuntimeException("CategoryAxis type cannot be used in BarChart (horizontal) for the values axis.");
            } else if (valuesAxisJSO.getType().equals(Axis.AxisType.NUMBER)) {
                valuesAxisBuilder[0] = new NumericAxisBuilder(getData(), getChartWidth(), direction, valuesAxisJSO);
            } else {
                // TODO: yAxisBuilder = new DateAxisBuilder(getData(), yAxisJSO);
            }

            // Build Y axis builder.
            categoriesAxisJSO = BarChart.this.getCategoriesAxis();
            if (categoriesAxisJSO.getType().equals(Axis.AxisType.CATEGORY)) {
                categoriesAxisBuilder[0] = new CategoryAxisBuilder(getData(), getChartHeight(), direction, categoriesAxisJSO);
            } else if (categoriesAxisJSO.getType().equals(Axis.AxisType.NUMBER)) {
                categoriesAxisBuilder[0] = new NumericAxisBuilder(getData(), getChartHeight(), direction, categoriesAxisJSO);
            } else {
                // TODO: xAxisBuilder = new DateAxisBuilder(getData(), xAxisJSO);
            }
        }

        public HorizontalBarChartBuilder buildCategoriesAxisTitle() {
            // Build the X axis line and title.
            categoriesAxisTitle = new Text(getCategoriesAxis().getTitle(), getFontFamily(), getFontStyle(), getFontSize()).setFillColor(ColorName.SILVER).setX(10).setY(getChartHeight() / 2).setTextAlign(TextAlign.RIGHT).setTextBaseLine(TextBaseLine.MIDDLE).setRotationDegrees(270);
            leftArea.add(categoriesAxisTitle);
            return this;
        }

        public HorizontalBarChartBuilder buildValuesAxisTitle() {
            // Build the Y axis line and title.
            valuesAxisTitle = new Text(getValuesAxis().getTitle(), getFontFamily(), getFontStyle(), getFontSize()).setFillColor(ColorName.SILVER).setX(getChartWidth() / 2).setY(30).setTextAlign(TextAlign.CENTER).setTextBaseLine(TextBaseLine.MIDDLE);
            bottomArea.add(valuesAxisTitle);
            return this;
        }
        
        @Override
        protected void addCategoryAxisIntervalLabel(Text label) {
            leftArea.add(label.setRotationDegrees(270));
        }

        @Override
        protected void addValuesAxisIntervalLabel(Text label) {
            bottomArea.add(label);
        }

        public HorizontalBarChartBuilder setCategoriesAxisTitleAttributes(Double width, Double height, boolean animate) {
            setShapeAttributes(categoriesAxisTitle, null, height / 2, null, null, animate);
            return this;
        }

        public HorizontalBarChartBuilder setValuesAxisTitleAttributes(Double width, Double height, boolean animate) {
            setShapeAttributes(valuesAxisTitle, width / 2, 30d, null, null, animate);
            return this;
        }
        
        @Override
        public HorizontalBarChartBuilder setValuesAxisIntervalsAttributes(Double width, Double height, boolean animate) {
            List<AxisBuilder.AxisLabel> labels = valuesAxisBuilder[0].getLabels();

            for (int i = 0; i < labels.size(); i++) {
                AxisBuilder.AxisLabel label = labels.get(i);
                double position = label.getPosition();
                String text = label.getText();
                valuesAxisIntervals[i].setPoints(new Point2DArray(new Point2D(position, 0), new Point2D(position, height)));
                valuesAxisIntervalLabels[i].setText(text);
                setShapeAttributes(valuesAxisIntervalLabels[i], position, 10d, null, height, animate);
            }

            return this;
        }

        public HorizontalBarChartBuilder setCategoriesAxisIntervalsAttributes(Double width, Double height, boolean animate) {
            List<AxisBuilder.AxisLabel> labels = categoriesAxisBuilder[0].getLabels();

            for (int i = 0; i < labels.size(); i++) {
                AxisBuilder.AxisLabel label = labels.get(i);
                double position = label.getPosition();
                String text = label.getText();
                seriesLabels.get(i).setText(text);
                // The position value must be calculed plus a constant, as the labels are rotated 270 degrees. 
                setShapeAttributes(seriesLabels.get(i), AREA_PADDING - 10d, position + 20d, null, null, animate);
            }

            return this;
        }
        
        protected HorizontalBarChartBuilder setValuesAttributesForSerie(final XYChartSerie serie, int numSerie, Double width, Double height, boolean animate) {
            XYChartSerie[] series = getData().getSeries();

            // Rebuild bars for serie values
            List<AxisBuilder.AxisValue> yAxisValues = categoriesAxisBuilder[0].getValues(getData().getCategoryAxisProperty());
            List<AxisBuilder.AxisValue> xAxisValues = valuesAxisBuilder[0].getValues(serie.getValuesAxisProperty());
            List<AxisBuilder.AxisLabel> xAxisLabels = valuesAxisBuilder[0].getLabels();
            List<Rectangle> bars = seriesValues.get(serie.getName());

            if (yAxisValues != null && yAxisValues.size() > 0) {
                for (int i = 0; i < yAxisValues.size(); i++) {
                    AxisBuilder.AxisValue xAxisvalue = xAxisValues.get(i);
                    AxisBuilder.AxisValue yAxisvalue = yAxisValues.get(i);

                    double xAxisValuePosition = xAxisvalue.getPosition();
                    Object xValue = xAxisvalue.getValue();
                    String xValueFormatted = valuesAxisBuilder[0].format(xValue);

                    // Obtain width and height values for the bar.
                    double barWidth = xAxisValuePosition;
                    double barHeight = getHeightForBar(height, series.length, yAxisValues.size());

                    // Calculate bar positions.
                    double x = 0;
                    double y = (barHeight * series.length * i) + (barHeight * numSerie) + (getCategoriesAxis().getSegments() * (i +1));
                    double alpha = 1d;

                    // If current bar is not in Y axis intervals (max / min values), resize it and apply an alpha.
                    double lastXIntervalPosition = xAxisLabels.get(xAxisLabels.size() - 1).getPosition();
                    boolean isOutOfChartArea = barWidth > lastXIntervalPosition;
                    if (isOutOfChartArea) {
                        alpha = 0.1d;
                        barWidth = width;
                    }

                    // Obtain the shape instance, add mouse handlers and reposition/resize it.
                    Rectangle barObject = bars.get(i);
                    barObject.setDraggable(true);
                    barObject.addNodeDragEndHandler(new NodeDragEndHandler() {
                        @Override
                        public void onNodeDragEnd(NodeDragEndEvent nodeDragEndEvent) {
                            double x = nodeDragEndEvent.getX();
                            double y = nodeDragEndEvent.getY();
                            if (x < chartArea.getX() || x > (chartArea.getX() + getChartWidth()) ||
                                    y < chartArea.getY() || y > (chartArea.getY() + getChartHeight())) {
                                // Remove the series from data.
                                XYChartData data = getData();
                                data.removeSerie(serie);
                                // Force firing attributes changed event in order to capture it and redraw the chart.
                                setData(data);
                            }
                        }
                    });
                    setShapeAttributes(barObject, x, y, barWidth, barHeight - BAR_SEPARATION, serie.getColor(), alpha, animate);
                }
            }
            return this;
        }

        public HorizontalBarChartBuilder reloadBuilders() {
            // Rebuild data summary as columns, series and values can have been modified.
            categoriesAxisBuilder[0].reload(getData(), seriesValues.keySet(), getChartHeight());
            valuesAxisBuilder[0].reload(getData(), seriesValues.keySet(), getChartWidth());
            return this;
        }

        protected double getHeightForBar(double chartHeight, int numSeries, int valuesCount) {
            // If exist more than one serie, and no stacked attribute is set, split each serie bar into the series count value.
            return getAvailableHeight(chartHeight, valuesCount) / valuesCount / numSeries;

        }

        protected double getAvailableHeight(double chartHeight, int valuesCount) {
            int xAxisDivisions = getCategoriesAxis().getSegments();
            return chartHeight - (xAxisDivisions * (valuesCount+1) );

        }
    }
    
}