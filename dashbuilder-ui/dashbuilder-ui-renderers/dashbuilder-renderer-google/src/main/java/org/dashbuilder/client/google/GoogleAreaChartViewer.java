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
package org.dashbuilder.client.google;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.corechart.AreaChart;
import com.googlecode.gwt.charts.client.corechart.AreaChartOptions;
import com.googlecode.gwt.charts.client.options.Animation;
import com.googlecode.gwt.charts.client.options.AnimationEasing;
import org.dashbuilder.model.displayer.AreaChartDisplayer;

public class GoogleAreaChartViewer extends GoogleXAxisChartViewer<AreaChartDisplayer> {

    protected AreaChart chart;

    @Override
    public ChartPackage getPackage() {
        return ChartPackage.CORECHART;
    }

    @Override
    protected Widget createVisualization() {
        chart = new AreaChart();
        chart.addSelectHandler(createSelectHandler(chart));
        chart.draw(createTable(), createOptions());

        HTML titleHtml = new HTML();
        if (dataDisplayer.isTitleVisible()) {
            titleHtml.setText(dataDisplayer.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(chart);
        return verticalPanel;
    }

    protected void updateVisualization() {
        chart.draw(createTable(), createOptions());
    }

    private AreaChartOptions createOptions() {
        Animation anim = Animation.create();
        anim.setDuration(500);
        anim.setEasing(AnimationEasing.IN_AND_OUT);

        AreaChartOptions options = AreaChartOptions.create();
        options.setWidth(dataDisplayer.getWidth());
        options.setHeight(dataDisplayer.getHeight());
        options.setColors(createColorArray(googleTable));
        options.setAnimation(anim);
        return options;
    }
}
