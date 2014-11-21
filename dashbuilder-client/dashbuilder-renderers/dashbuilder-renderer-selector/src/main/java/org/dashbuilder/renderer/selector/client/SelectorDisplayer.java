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
package org.dashbuilder.renderer.selector.client;

import java.util.List;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.renderer.selector.client.resources.i18n.SelectorConstants;

public class SelectorDisplayer extends AbstractDisplayer {

    protected FlowPanel panel = new FlowPanel();
    ListBox listBox = null;
    protected boolean drawn = false;
    protected DataSet dataSet = null;

    public SelectorDisplayer() {
        initWidget(panel);
    }

    public void draw() {
        if ( !drawn ) {
            drawn = true;

            if ( displayerSettings == null ) {
                displayMessage( "ERROR: DisplayerSettings property not set" );
            } else if ( dataSetHandler == null ) {
                displayMessage( "ERROR: DataSetHandler property not set" );
            } else {
                try {
                    String initMsg = SelectorConstants.INSTANCE.selectorDisplayer_initializing();
                    if (!StringUtils.isBlank(displayerSettings.getTitle())) initMsg += " '" + displayerSettings.getTitle() + "'";
                    displayMessage(initMsg + " ...");

                    dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                        public void callback(DataSet result) {
                            dataSet = result;
                            Widget w = createSelector();
                            panel.clear();
                            panel.add(w);

                            // Set the id of the container panel so that the displayer can be easily located
                            // by testing tools for instance.
                            String id = getDisplayerId();
                            if (!StringUtils.isBlank(id)) {
                                panel.getElement().setId(id);
                            }
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

    public void redraw() {
        if (!drawn) {
            draw();
        } else {
            try {
                dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                    public void callback(DataSet result) {
                        dataSet = result;
                        populateSelector();
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
    public DisplayerConstraints createDisplayerConstraints() {

        return new DisplayerConstraints(new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxColumns(1)
                .setMinColumns(1)
                .setGroupsTitle("Categories")
                .setColumnsTitle("Values")
                .setColumnTypes(new ColumnType[] {
                        ColumnType.LABEL}));
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

    protected Widget createSelector() {
        listBox = new ListBox();
        populateSelector();

        // Catch list selections
        listBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                // Reset the current filter (if any)
                String firstColumnId = getColumnId(dataSet, 0);
                List<String> currentFilter = filterValues(firstColumnId);
                if (currentFilter != null && !currentFilter.isEmpty()) {
                    filterReset();
                }

                // Filter by the selected value (if any)
                int index = listBox.getSelectedIndex();
                if (index > 0) {
                    filterUpdate(firstColumnId, listBox.getValue(index));
                }
            }
        });
        return listBox;
    }

    protected void populateSelector() {
        listBox.clear();
        final String firstColumnId = getColumnId(dataSet, 0);
        final String firstColumnName = getColumnName(dataSet, 0);
        listBox.addItem("- Select " + firstColumnName + " -");
        SelectElement selectElement = SelectElement.as(listBox.getElement());
        NodeList<OptionElement> options = selectElement.getOptions();

        // Generate the list entries from the current data set
        List<String> currentFilter = filterValues(firstColumnId);
        for (int i = 0; i < dataSet.getRowCount(); i++) {
            String value = dataSet.getValueAt(i, firstColumnId).toString();
            listBox.addItem(value);
            if (currentFilter != null && currentFilter.contains(value)) {
                listBox.setSelectedIndex(i+1);
            }

            // Generate an option tooltip (only if extra data set columns are defined)
            int ncolumns = getNumberOfColumns(dataSet);
            if (ncolumns > 1) {
                StringBuilder out = new StringBuilder();
                for (int j = 1; j < ncolumns; j++) {
                    String extraColumnId = getColumnId(dataSet, j);
                    String extraColumnName = getColumnName(dataSet, j);
                    Object extraValue = dataSet.getValueAt(i, extraColumnId);

                    if (j > 1) out.append("  ");
                    out.append(extraColumnName).append("=").append(extraValue.toString());
                }
                options.getItem(i+1).setTitle(out.toString());
            }
        }
    }

    protected int getNumberOfColumns(DataSet dataSet) {
        return dataSet.getColumns().size();
    }

    protected String getColumnId(DataSet dataSet, int index) {
        int ncolumns = dataSet.getColumns().size();
        if (index < ncolumns) {
            return dataSet.getColumnByIndex(index).getId();
        }
        throw new IndexOutOfBoundsException("Index " + index + " is greater than " +
                "the number of columns in the data set " + ncolumns);
    }

    protected String getColumnName(DataSet dataSet, int index) {
        int ncolumns = dataSet.getColumns().size();
        if (index < ncolumns) {
            return dataSet.getColumnByIndex(index).getName();
        }
        throw new IndexOutOfBoundsException("Index " + index + " is greater than " +
                "the number of columns in the data set " + ncolumns);
    }
}
