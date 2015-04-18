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
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;

import javax.enterprise.context.Dependent;

/**
 * <p>This is the view implementation widget for Data Set Editor widget for previewing the data set in a table displayer.</p>
 * 
 * <p>Consider that this widget is not a GWT Data Set editor component, as it does not edit any dataset attributes. It's just for previewing the results.</p> 
 *
 * @since 0.3.0 
 */
@Dependent
public class DataSetPreviewEditor extends AbstractDataSetDefEditor {

    interface DataSetPreviewEditorBinder extends UiBinder<Widget, DataSetPreviewEditor> {}
    private static DataSetPreviewEditorBinder uiBinder = GWT.create(DataSetPreviewEditorBinder.class);

    @UiField
    FlowPanel columnFilterTablePanel;

    @UiField
    FlowPanel tablePanel;

    Displayer tableDisplayer = null;

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
    
    public void build(final Displayer displayer) {
        this.tableDisplayer = displayer;
        showTableDisplayer();
    }
    
    public void clear() {
        tableDisplayer = null;
    }

    private void showTableDisplayer() {
        
        // Clear current view.
        clearView();
        
        // Build the table displayer.
        if (tableDisplayer != null) {

            // Create and draw the preview table.
            tablePanel.add(tableDisplayer);
            tableDisplayer.draw();
        }

    }
    
    private void clearView() {
        tablePanel.clear();
    }

}
