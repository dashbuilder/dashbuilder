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
package org.dashbuilder.renderer.google.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.gauge.Gauge;
import com.googlecode.gwt.charts.client.gauge.GaugeOptions;
import com.googlecode.gwt.charts.client.options.Animation;
import com.googlecode.gwt.charts.client.options.AnimationEasing;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.renderer.google.client.resources.i18n.GoogleDisplayerConstants;

public class GoogleMeterChartDisplayer extends GoogleChartDisplayer {

    private Gauge chart;

    @Override
    public ChartPackage getPackage() {
        return ChartPackage.GAUGE;
    }

    @Override
    public Widget createVisualization() {
        chart = new Gauge();
        chart.draw(createTable(), createOptions());

        HTML titleHtml = new HTML();
        if (displayerSettings.isTitleVisible()) {
            titleHtml.setText(displayerSettings.getTitle());
        }

        FlowPanel container = new FlowPanel();
        container.add(titleHtml);
        container.add(chart);
        return container;
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(false)
                .setGroupAllowed(true)
                .setGroupColumn(true)
                .setMaxColumns(2)
                .setMinColumns(1)
                .setExtraColumnsAllowed(false)
                .setGroupsTitle(GoogleDisplayerConstants.INSTANCE.common_Categories())
                .setColumnsTitle(GoogleDisplayerConstants.INSTANCE.common_Value())
                .setFunctionRequired(true)
                .setColumnTypes(new ColumnType[] {ColumnType.NUMBER},
                        new ColumnType[] {ColumnType.LABEL, ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                   .supportsAttribute(DisplayerAttributeDef.TYPE)
                   .supportsAttribute(DisplayerAttributeDef.RENDERER)
                   .supportsAttribute(DisplayerAttributeGroupDef.COLUMNS_GROUP)
                   .supportsAttribute(DisplayerAttributeGroupDef.FILTER_GROUP)
                   .supportsAttribute( DisplayerAttributeGroupDef.REFRESH_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.GENERAL_GROUP)
                   .supportsAttribute( DisplayerAttributeDef.CHART_WIDTH )
                   .supportsAttribute( DisplayerAttributeDef.CHART_HEIGHT )
                   .supportsAttribute( DisplayerAttributeGroupDef.CHART_MARGIN_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.METER_GROUP );
    }

    protected void updateVisualization() {
        chart.draw(createTable(), createOptions());
    }

    private GaugeOptions createOptions() {
        Animation anim = Animation.create();
        anim.setDuration(700);
        anim.setEasing(AnimationEasing.IN_AND_OUT);

        GaugeOptions options = GaugeOptions.create();
        options.setWidth(displayerSettings.getChartWidth());
        options.setHeight(displayerSettings.getChartHeight());
        options.setMin(displayerSettings.getMeterStart());
        options.setMax(displayerSettings.getMeterEnd());
        options.setGreenFrom(displayerSettings.getMeterStart());
        options.setGreenTo(displayerSettings.getMeterWarning());
        options.setYellowFrom(displayerSettings.getMeterWarning());
        options.setYellowTo(displayerSettings.getMeterCritical());
        options.setRedFrom(displayerSettings.getMeterCritical());
        options.setRedTo(displayerSettings.getMeterEnd());
        options.setAnimation(anim);
        return options;
    }
}
