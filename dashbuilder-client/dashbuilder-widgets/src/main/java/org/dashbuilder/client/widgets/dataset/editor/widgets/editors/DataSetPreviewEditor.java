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
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.TableDisplayerSettingsBuilder;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.dashbuilder.displayer.impl.TableDisplayerSettingsBuilderImpl;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * <p>This is the view implementation widget for Data Set Editor widget for previewing the data set in a table displayer.</p>
 * 
 * <p>Consider that this widget is not a GWT Data Set editor component, as it does not edit any dataset attributes. It's just for previewing the results.</p> 
 */
@Dependent
public class DataSetPreviewEditor extends AbstractDataSetDefEditor {

    interface DataSetPreviewEditorBinder extends UiBinder<Widget, DataSetPreviewEditor> {}
    private static DataSetPreviewEditorBinder uiBinder = GWT.create(DataSetPreviewEditorBinder.class);

    @UiField
    FlowPanel columnFilterTablePanel;

    @UiField
    FlowPanel tablePanel;

    final DisplayerCoordinator coordinator = new DisplayerCoordinator();
    Displayer tableDisplayer = null;
    private DataSet dataSet = null;

    private boolean isEditMode;

    public DataSetPreviewEditor() {
        // Initialize the widget.
        initWidget(uiBinder.createAndBindUi(this));
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    @Override
    public void set(DataSetDef dataSetDef) {
        super.set(dataSetDef);
    }
    
    public void build(final DisplayerListener listener) {
        
        showTableDisplayer();
        
        // TODO: Show loading screen...
        
        if (tableDisplayer != null && listener != null) tableDisplayer.addListener(listener); 
       
    }

    public void update(final DisplayerListener listener) {
        this.dataSet = null;

        build(listener);
    }
    
    private void showTableDisplayer() {
        
        // Clear current view.
        clearView();
        
        // Build the table displayer.
        tableDisplayer = buildTableDisplayer();
        if (tableDisplayer != null) {

            // Create and draw the preview table.
            coordinator.addDisplayer(tableDisplayer);
            tablePanel.add(tableDisplayer);

            final TableListener tableListener = new TableListener();
            tableDisplayer.addListener(tableListener);

            coordinator.drawAll();
        }

    }
    
    private void showColumnsView() {
        if (dataSet != null) {
            List<DataColumn> columns = dataSet.getColumns();
            if (columns != null) {
                for (DataColumn column : columns) {
                    GWT.log("Found column");
                    GWT.log("************");
                    GWT.log("id="+column.getId());
                    GWT.log("name="+column.getName());
                    GWT.log("type="+column.getColumnType());
                }
            }
        }
        
    }

    private Displayer buildTableDisplayer() {
        if (dataSetDef != null) {
            TableDisplayerSettingsBuilder<TableDisplayerSettingsBuilderImpl> tableDisplayerSettingsBuilder = DisplayerSettingsFactory.newTableSettings()
                    .dataset(dataSetDef.getUUID())
                    .titleVisible(false)
                    .tablePageSize(10)
                    .tableOrderEnabled(false)
                    .filterOn(false, false, false);
            
            List<DataColumn> columns =  dataSetDef.getDataSet().getColumns();
            if (columns != null && !columns.isEmpty()) {
                for (DataColumn column : columns) {
                    tableDisplayerSettingsBuilder.column(column.getId());
                }
            }

            DisplayerSettings settings = tableDisplayerSettingsBuilder.buildSettings();
            return DisplayerHelper.lookupDisplayer(settings);
        }
        
        return null;
    }

    private void clearView() {
        tablePanel.clear();
    }

    private class TableListener implements DisplayerListener {

        public void onDraw(Displayer displayer) {
            DataSetPreviewEditor.this.dataSet = displayer.getDataSetHandler().getLastDataSet();
            if (DataSetPreviewEditor.this.dataSet != null) {
                showColumnsView();
            }
        }

        public void onRedraw(Displayer displayer) {

        }

        public void onClose(Displayer displayer) {

        }

        public void onGroupIntervalsSelected(Displayer displayer, DataSetGroup groupOp) {

        }

        public void onGroupIntervalsReset(Displayer displayer, List<DataSetGroup> groupOps) {

        }
    }
}
