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

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.gwt.visualization.client.visualizations.Table.Options;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;

@Dependent
@Named("google_table_viewer")
public class GoogleTableViewer extends GoogleChartViewer {

    @Override
    public String getPackage() {
        return Table.PACKAGE;
    }

    @Override
    public Widget createChart() {
        Table w = new Table(createTable(), createOptions());
        w.addSelectHandler(createSelectHandler(w));
        return w;
    }

    public AbstractDataTable createTable() {
        DataTable data = DataTable.create();
        data.addRows(dataSet.getRowCount());

        List<DataColumn> columns = dataSet.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            DataColumn column = columns.get(i);
            List values = column.getValues();
            ColumnType type = column.getColumnType();
            data.addColumn(getColumnType(column), column.getId());
            for (int j = 0; j < values.size(); j++) {

                if (ColumnType.DATE.equals(type)) {
                    data.setValue(j, i, (Date) values.get(j));
                }
                else if (ColumnType.NUMBER.equals(type)) {
                    // TODO: format decimal number
                    data.setValue(j, i, (Double) values.get(j));
                }
                else {
                    data.setValue(j, i, values.get(j).toString());
                }
            }
        }
        return data;
    }

    private Options createOptions() {
        Options options = Options.create();
        options.setPageSize(10);
        options.setShowRowNumber(true);
        return options;
    }

    private SelectHandler createSelectHandler(final Table w) {
        return new SelectHandler() {
            public void onSelect(SelectEvent event) {
                String message = "";

                // May be multiple selections.
                JsArray<Selection> selections = w.getSelections();

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
                        message += "Selections should be either row selections or cell selections.";
                        message += "  Other visualizations support column selections as well.";
                    }
                }
                //Window.alert(message);
            }
        };
    }
}
