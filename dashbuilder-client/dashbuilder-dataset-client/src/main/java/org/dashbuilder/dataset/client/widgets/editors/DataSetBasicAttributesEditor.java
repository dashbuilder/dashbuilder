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

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.WellForm;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.ClientDataSetManager;
import org.dashbuilder.dataset.client.widgets.DataSetEditor;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing data set UUID and name attributes.</p>
 */
@Dependent
public class DataSetBasicAttributesEditor extends Composite implements DataSetEditor.View {

    interface DataSetBasicAttributesEditorBinder extends UiBinder<Widget, DataSetBasicAttributesEditor> {}
    private static DataSetBasicAttributesEditorBinder uiBinder = GWT.create(DataSetBasicAttributesEditorBinder.class);

    @UiField
    HorizontalPanel basicAttributesPanel;
    
    @UiField
    @Path("UUID")
    ValueBoxEditorDecorator<String> attributeUUID;

    @UiField
    ValueBoxEditorDecorator<String> name;

    private DataSetDef dataSetDef;
    private  boolean isEditMode;

    public DataSetBasicAttributesEditor() {
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

        // If defintion has a provider type set, show it.
        if (dataSetDef != null ) {
            // TODO: attributeName.setEnabled(isEditMode);
        }

        return asWidget();
    }

    @Override
    public void hide() {
        basicAttributesPanel.setVisible(false);
    }

    @Override
    public void clear() {
        clearScreen();
        clearStatus();
    }
    
    private void clearScreen() {
        // TODO: attributeUUID.setText("");
        // TODO: attributeName.setText("");
    }
    
    private void clearStatus() {
        this.dataSetDef = null;
    }

}
