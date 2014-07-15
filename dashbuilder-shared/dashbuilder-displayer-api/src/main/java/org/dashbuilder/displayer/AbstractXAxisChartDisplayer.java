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
 * Base class for X-axis based chart displayers.
 */
public abstract class AbstractXAxisChartDisplayer extends AbstractChartDisplayer implements XAxisChartDisplayer {

    protected boolean xAxisShowLabels = DEFAULT_XAXIS_SHOW_LABELS;
    protected int xAxisLabelsAngle = DEFAULT_XAXIS_LABELS_ANGLE;
    protected String xAxisTitle = null;

    @Override
    public boolean isXAxisShowLabels() {
        return xAxisShowLabels;
    }

    @Override
    public void setXAxisShowLabels( boolean showXAxisLabels ) {
        this.xAxisShowLabels = showXAxisLabels;
    }

    @Override
    public int getXAxisLabelsAngle() {
        return xAxisLabelsAngle;
    }

    @Override
    public void setXAxisLabelsAngle( int angle ) {
        this.xAxisLabelsAngle = angle;
    }

    @Override
    public String getXAxisTitle() {
        return xAxisTitle;
    }

    @Override
    public void setXAxisTitle( String title ) {
        this.xAxisTitle = title;
    }
}
