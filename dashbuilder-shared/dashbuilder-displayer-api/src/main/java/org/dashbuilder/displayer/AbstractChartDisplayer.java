/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.displayer;

/**
 * Base class for chart displayers.
 */
public abstract class AbstractChartDisplayer extends AbstractDataDisplayer implements ChartDisplayer {

    protected int width = DEFAULT_WIDTH;
    protected int height = DEFAULT_HEIGHT;
    protected int marginTop = DEFAULT_MARGINTOP;
    protected int marginBottom = DEFAULT_MARGINBOTTOM;
    protected int marginLeft = DEFAULT_MARGINLEFT;
    protected int marginRight = DEFAULT_MARGINRIGHT;
    protected boolean showLegend = DEFAULT_LEGEND_SHOW;
    protected Position legendPosition = DEFAULT_LEGEND_POSITION;

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth( int width ) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight( int height ) {
        this.height = height;
    }

    @Override
    public int getMarginTop() {
        return marginTop;
    }

    @Override
    public void setMarginTop( int marginTop ) {
        this.marginTop = marginTop;
    }

    @Override
    public int getMarginBottom() {
        return marginBottom;
    }

    @Override
    public void setMarginBottom( int marginBottom ) {
        this.marginBottom = marginBottom;
    }

    @Override
    public int getMarginLeft() {
        return marginLeft;
    }

    @Override
    public void setMarginLeft( int marginLeft ) {
        this.marginLeft = marginLeft;
    }

    @Override
    public int getMarginRight() {
        return marginRight;
    }

    @Override
    public void setMarginRight( int marginRight ) {
        this.marginRight = marginRight;
    }

    @Override
    public boolean isLegendShow() {
        return showLegend;
    }

    @Override
    public void setLegendShow( boolean showLegend ) {
        this.showLegend = showLegend;
    }

    @Override
    public Position getLegendPosition() {
        return legendPosition;
    }

    @Override
    public void setLegendPosition( Position position ) {
        this.legendPosition = position;
    }
}
