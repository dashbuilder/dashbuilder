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
package org.dashbuilder.dataset.client.widgets.editors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.ImageListEditorDecorator;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.widgets.DataSetEditor;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;


/**
 * <p>This is the view implementation for Data Set Editor widget for editing data set columns, intial filter and testing the results in a table.</p>
 */
@Dependent
public class DataSetColumnsAndFilterEditor extends Composite implements DataSetEditor.View {

    interface DataSetColumnsAndFilterEditorBinder extends UiBinder<Widget, DataSetColumnsAndFilterEditor> {}
    private static DataSetColumnsAndFilterEditorBinder uiBinder = GWT.create(DataSetColumnsAndFilterEditorBinder.class);

    @UiField
    HorizontalPanel columnFilterTablePanel;

    private DataSetDef dataSetDef;
    private boolean isEditMode;

    public DataSetColumnsAndFilterEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void set(DataSetDef dataSetDef) {
        this.dataSetDef = dataSetDef;
    }

    @Override
    public Widget show(final boolean isEditMode) {
        this.isEditMode = isEditMode;
        
        // Clear the widget.
        clearScreen();

        // TODO
        
        return asWidget();
    }

    @Override
    public void hide() {
        columnFilterTablePanel.setVisible(false);
    }

    @Override
    public void clear() {
        clearScreen();
        clearStatus();
    }
    
    private void clearScreen() {
        
    }
    
    private void clearStatus() {
        this.dataSetDef = null;
    }

}
