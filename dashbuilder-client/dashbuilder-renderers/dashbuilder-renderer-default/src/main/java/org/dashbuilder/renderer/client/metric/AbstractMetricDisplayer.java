/**
 * Copyright (C) 2015 JBoss Inc
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
package org.dashbuilder.renderer.client.metric;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.renderer.client.resources.i18n.MetricConstants;

public abstract class AbstractMetricDisplayer extends AbstractDisplayer {

    protected FlowPanel panel = new FlowPanel();
    protected boolean drawn = false;
    protected DataSet dataSet = null;

    public AbstractMetricDisplayer() {
        initWidget(panel);
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupAllowed(false)
                .setMaxColumns(1)
                .setMinColumns(1)
                .setFunctionRequired(true)
                .setExtraColumnsAllowed(false)
                .setColumnsTitle("Metric")
                .setColumnTypes(new ColumnType[] {
                        ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute(DisplayerAttributeDef.TYPE)
                .supportsAttribute(DisplayerAttributeDef.RENDERER)
                .supportsAttribute(DisplayerAttributeDef.COLUMNS)
                .supportsAttribute(DisplayerAttributeGroupDef.FILTER_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.REFRESH_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.TITLE_GROUP)
                .supportsAttribute(DisplayerAttributeDef.CHART_WIDTH)
                .supportsAttribute(DisplayerAttributeDef.CHART_HEIGHT)
                .supportsAttribute(DisplayerAttributeDef.CHART_BGCOLOR)
                .supportsAttribute(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP);
    }

    @Override
    public void draw() {
        if ( !drawn ) {
            drawn = true;

            if ( displayerSettings == null ) {
                displayMessage( "ERROR: DisplayerSettings property not set" );
            } else if ( dataSetHandler == null ) {
                displayMessage( "ERROR: DataSetHandler property not set" );
            } else {
                try {
                    String initMsg = MetricConstants.INSTANCE.metricDisplayer_initializing();
                    displayMessage(initMsg + " ...");

                    dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                        public void callback(DataSet result) {
                            dataSet = result;
                            Widget w = createMetricWidget();
                            panel.clear();
                            panel.add(w);

                            // Set the id of the container panel so that the displayer can be easily located
                            // by testing tools for instance.
                            String id = getDisplayerId();
                            if (!StringUtils.isBlank(id)) {
                                panel.getElement().setId(id);
                            }
                            // Draw done
                            afterDraw();
                        }
                        public void notFound() {
                            displayMessage("ERROR: Data set not found.");
                        }
                    });
                } catch ( Exception e ) {
                    displayMessage( "ERROR: " + e.getMessage() );
                }
            }
        }
    }

    @Override
    public void redraw() {
        if (!drawn) {
            draw();
        } else {
            try {
                dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                    public void callback(DataSet result) {
                        dataSet = result;
                        updateMetricWidget();

                        // Redraw done
                        afterRedraw();
                    }
                    public void notFound() {
                        displayMessage("ERROR: Data set not found.");
                    }
                });
            } catch ( Exception e ) {
                displayMessage( "ERROR: " + e.getMessage() );
            }
        }
    }

    @Override
    public void close() {
        panel.clear();

        // Close done
        afterClose();
    }

    /**
     * Clear the current display and show a notification message.
     */
    public void displayMessage(String msg) {
        panel.clear();
        Label label = new Label();
        panel.add(label);
        label.setText(msg);
    }

    /**
     * Create the widget for displaying the metric
     */
    protected abstract Widget createMetricWidget();

    /**
     * Update the existing metric widget with the latest value
     */
    protected abstract void updateMetricWidget();
}
