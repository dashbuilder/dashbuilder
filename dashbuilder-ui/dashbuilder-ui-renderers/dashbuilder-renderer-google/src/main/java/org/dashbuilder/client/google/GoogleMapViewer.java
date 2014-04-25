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

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.GeoMap.Options;
import com.google.gwt.visualization.client.visualizations.GeoMap;
import org.dashbuilder.model.displayer.Chart;

@Dependent
@Named("google_map_viewer")
public class GoogleMapViewer extends GoogleXAxisChartViewer {

    @Override
    public String getPackage() {
        return GeoMap.PACKAGE;
    }

    @Override
    public Widget createChart() {
        GeoMap chart = new GeoMap(createTable(), createOptions());
        chart.addSelectHandler(createSelectHandler(chart));
        return chart;
    }

    private Options createOptions() {
        Options options = Options.create();
        if (dataDisplayer instanceof Chart) {
            Chart chart = (Chart) dataDisplayer;
            options.setWidth(chart.getWidth());
            options.setHeight(chart.getHeight());
        }
        options.setShowLegend(true);
        return options;
    }

    private SelectHandler createSelectHandler(final GeoMap chart) {
        return new SelectHandler() {
            public void onSelect(SelectEvent event) {
                String message = "";

                // May be multiple selections.
                JsArray<Selection> selections = chart.getSelections();

                for (int i = 0; i < selections.length(); i++) {
                    // add a new line for each selection
                    message += i == 0 ? "" : "\n";

                    Selection selection = selections.get(i);

                    if (selection.isCell()) {
                        // isCell() returns true if a cell has been selected.

                        // getRow() returns the row number of the selected cell.
                        int row = selection.getRow();
                        // getColumn() returns the column number of the selected cell.
                        int column = selection.getColumn();
                        message += "cell " + row + ":" + column + " selected";
                    } else if (selection.isRow()) {
                        // isRow() returns true if an entire row has been selected.

                        // getRow() returns the row number of the selected row.
                        int row = selection.getRow();
                        message += "row " + row + " selected";
                    } else {
                        // unreachable
                        message += "Chart selections should be either row selections or cell selections.";
                        message += "  Other visualizations support column selections as well.";
                    }
                }
                //Window.alert(message);
            }
        };
    }
}
