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
package org.dashbuilder.renderer.client.selector;

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
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.renderer.client.resources.i18n.SelectorConstants;

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
                    //if (!StringUtils.isBlank(displayerSettings.getTitle())) initMsg += " '" + displayerSettings.getTitle() + "'";
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

    public void redraw() {
        if (!drawn) {
            draw();
        } else {
            try {
                dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                    public void callback(DataSet result) {
                        dataSet = result;
                        populateSelector();

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

    public void close() {
        panel.clear();

        // Close done
        afterClose();
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxColumns(-1)
                .setMinColumns(1)
                .setExtraColumnsAllowed(true)
                .setGroupsTitle("Categories")
                .setColumnsTitle("Values")
                .setColumnTypes(new ColumnType[] {
                        ColumnType.LABEL});

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute( DisplayerAttributeDef.TYPE )
                .supportsAttribute( DisplayerAttributeDef.COLUMNS )
                .supportsAttribute( DisplayerAttributeGroupDef.FILTER_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.REFRESH_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.TITLE_GROUP );
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
                String firstColumnId = dataSet.getColumnByIndex(0).getId();
                List<Integer> currentFilter = filterIndexes(firstColumnId);
                if (currentFilter != null && !currentFilter.isEmpty()) {
                    filterReset();
                }

                // Filter by the selected value (if any)
                int index = listBox.getSelectedIndex();
                if (index > 0) {
                    filterUpdate(firstColumnId, index-1);
                }
            }
        });
        return listBox;
    }

    protected void populateSelector() {
        listBox.clear();
        final DataColumn firstColumn = dataSet.getColumnByIndex(0);
        final String firstColumnId = firstColumn.getId();
        final String firstColumnName = firstColumn.getName();
        listBox.addItem("- Select " + firstColumnName + " -");
        SelectElement selectElement = SelectElement.as(listBox.getElement());
        NodeList<OptionElement> options = selectElement.getOptions();

        // Generate the list entries from the current data set
        List<Integer> currentFilter = super.filterIndexes(firstColumnId);
        for (int i = 0; i < dataSet.getRowCount(); i++) {

            Object obj = dataSet.getValueAt(i, 0);
            if (obj == null) continue;

            String value = super.formatValue(obj, firstColumn);
            listBox.addItem(value);
            if (currentFilter != null && currentFilter.contains(i)) {
                listBox.setSelectedIndex(i+1);
            }

            // Generate an option tooltip (only if extra data set columns are defined)
            int ncolumns = getNumberOfColumns(dataSet);
            if (ncolumns > 1) {
                StringBuilder out = new StringBuilder();
                for (int j = 1; j < ncolumns; j++) {
                    final DataColumn extraColumn = dataSet.getColumnByIndex(j);
                    String extraColumnName = extraColumn.getName();
                    Object extraValue = dataSet.getValueAt(i, j);

                    if (extraValue != null) {
                        if (j > 1) out.append("  ");
                        String formattedValue = super.formatValue(extraValue, extraColumn);
                        out.append(extraColumnName).append("=").append(formattedValue);
                    }
                }
                OptionElement optionElement = options.getItem(i+1); 
                if (optionElement != null) optionElement.setTitle(out.toString());
            }
        }
    }

    protected int getNumberOfColumns(DataSet dataSet) {
        return dataSet.getColumns().size();
    }

    // KEEP IN SYNC THE CURRENT SELECTION WITH ANY EXTERNAL FILTER

    public void onGroupIntervalsSelected(Displayer displayer, DataSetGroup groupOp) {
        String firstColumnId = dataSet.getColumnByIndex(0).getId();
        if (firstColumnId.equals(groupOp.getColumnGroup().getColumnId())) {
            columnSelectionMap.put(groupOp.getColumnGroup().getColumnId(), groupOp.getSelectedIntervalList());
        }
        super.onGroupIntervalsSelected(displayer, groupOp);
    }

    public void onGroupIntervalsReset(Displayer displayer, List<DataSetGroup> groupOps) {
        String firstColumnId = dataSet.getColumnByIndex(0).getId();
        for (DataSetGroup groupOp : groupOps) {
            if (firstColumnId.equals(groupOp.getColumnGroup().getColumnId())) {
                columnSelectionMap.remove(groupOp.getColumnGroup().getColumnId());
            }
        }
        super.onGroupIntervalsReset(displayer, groupOps);
    }
}
