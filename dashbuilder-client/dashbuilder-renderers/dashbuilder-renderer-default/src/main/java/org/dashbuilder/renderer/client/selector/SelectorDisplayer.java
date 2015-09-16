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

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.renderer.client.resources.i18n.SelectorConstants;

import java.util.List;

public class SelectorDisplayer extends AbstractDisplayer {

    ListBox listBox = null;

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxColumns(-1)
                .setMinColumns(1)
                .setExtraColumnsAllowed(true)
                .setGroupsTitle(SelectorConstants.INSTANCE.selectorDisplayer_groupsTitle())
                .setColumnsTitle(SelectorConstants.INSTANCE.selectorDisplayer_columnsTitle())
                .setColumnTypes(new ColumnType[] {
                        ColumnType.LABEL});

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute( DisplayerAttributeDef.TYPE )
                .supportsAttribute( DisplayerAttributeGroupDef.COLUMNS_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.FILTER_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.REFRESH_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.GENERAL_GROUP );
    }

    @Override
    protected Widget createVisualization() {
        listBox = new ListBox();
        updateVisualization();

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

    @Override
    protected void updateVisualization() {
        listBox.clear();
        final DataColumn firstColumn = dataSet.getColumnByIndex(0);
        final String firstColumnId = firstColumn.getId();
        ColumnSettings columnSettings = displayerSettings.getColumnSettings(firstColumn);
        final String firstColumnName = columnSettings.getColumnName();

        listBox.addItem("- " + SelectorConstants.INSTANCE.selectorDisplayer_select()  + " " + firstColumnName + " -");
        SelectElement selectElement = SelectElement.as(listBox.getElement());
        NodeList<OptionElement> options = selectElement.getOptions();

        // Generate the list entries from the current data set
        List<Integer> currentFilter = super.filterIndexes(firstColumnId);
        for (int i = 0; i < dataSet.getRowCount(); i++) {

            Object obj = dataSet.getValueAt(i, 0);
            if (obj == null) continue;

            String value = super.formatValue(i, 0);
            listBox.addItem(value);
            if (currentFilter != null && currentFilter.contains(i)) {
                listBox.setSelectedIndex(i+1);
            }

            // Generate an option tooltip (only if extra data set columns are defined)
            int ncolumns = getNumberOfColumns(dataSet);
            if (ncolumns > 1) {
                StringBuilder out = new StringBuilder();
                for (int j = 1; j < ncolumns; j++) {
                    DataColumn extraColumn = dataSet.getColumnByIndex(j);
                    columnSettings = displayerSettings.getColumnSettings(extraColumn);
                    String extraColumnName = columnSettings.getColumnName();
                    Object extraValue = dataSet.getValueAt(i, j);

                    if (extraValue != null) {
                        if (j > 1) out.append("  ");
                        String formattedValue = super.formatValue(i, j);
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

    @Override
    public void onFilterEnabled(Displayer displayer, DataSetGroup groupOp) {
        String firstColumnId = dataSet.getColumnByIndex(0).getId();
        if (firstColumnId.equals(groupOp.getColumnGroup().getColumnId())) {
            columnSelectionMap.put(groupOp.getColumnGroup().getColumnId(), groupOp.getSelectedIntervalList());
        }
        super.onFilterEnabled(displayer, groupOp);
    }

    @Override
    public void onFilterReset(Displayer displayer, List<DataSetGroup> groupOps) {
        String firstColumnId = dataSet.getColumnByIndex(0).getId();
        for (DataSetGroup groupOp : groupOps) {
            if (firstColumnId.equals(groupOp.getColumnGroup().getColumnId())) {
                columnSelectionMap.remove(groupOp.getColumnGroup().getColumnId());
            }
        }
        super.onFilterReset(displayer, groupOps);
    }
}
