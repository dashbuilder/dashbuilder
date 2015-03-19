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
package org.dashbuilder.dataset.client.widgets.editors.csv;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.client.validation.editors.CSVDataSetDefEditor;

import javax.enterprise.context.Dependent;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing CSV provider specific attributes.</p>
 */
@Dependent
public class CSVDataSetDefAttributesEditor extends Composite implements CSVDataSetDefEditor {
    
    interface CSVDataSetDefAttributesEditorBinder extends UiBinder<Widget, CSVDataSetDefAttributesEditor> {}
    private static CSVDataSetDefAttributesEditorBinder uiBinder = GWT.create(CSVDataSetDefAttributesEditorBinder.class);

    @UiField
    HorizontalPanel csvAttributesPanel;

    private boolean isEditMode;

    public CSVDataSetDefAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }
}
