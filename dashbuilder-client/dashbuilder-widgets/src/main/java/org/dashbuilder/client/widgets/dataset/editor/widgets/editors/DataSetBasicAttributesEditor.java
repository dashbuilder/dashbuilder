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
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing data set UUID and name attributes.</p>
 *
 * <p>NOTE: The <code>uuid</code> is not bind directly to the data set definiton instance, as when editing a data set def, the instance is cloned temporary for editing it and the uuid is different than the original one, so it must be set programatically.</p>
 *
 * @since 0.3.0 
 */
@Dependent
public class DataSetBasicAttributesEditor extends AbstractDataSetDefEditor implements DataSetDefEditor {

    interface DataSetBasicAttributesEditorBinder extends UiBinder<Widget, DataSetBasicAttributesEditor> {}
    private static DataSetBasicAttributesEditorBinder uiBinder = GWT.create(DataSetBasicAttributesEditorBinder.class);

    @UiField
    FlowPanel basicAttributesPanel;
    
    @UiField
    @Ignore
    ValueBoxEditorDecorator<String> attributeUUID;

    @UiField
    ValueBoxEditorDecorator<String> name;
    
    private  boolean isEditMode;

    private final ValueChangeHandler<String> uuidValueChangeHandler = new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
            dataSetDef.setUUID(event.getValue());
        }
    };
    
    public DataSetBasicAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        attributeUUID.addValueChangeHandler(uuidValueChangeHandler);
    }

    public void setUUID(final String uuid) {
        this.attributeUUID.asEditor().setValue(uuid);
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        consumeErrors(errors);
    }

    public void clear() {
        super.clear();
        attributeUUID.clear();
        name.clear();
    }
}
