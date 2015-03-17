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
package org.dashbuilder.dataset.client.widgets.editors.sql;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.validation.editors.SQLDataSetDefEditor;
import org.dashbuilder.dataset.client.widgets.DataSetEditor;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;

import javax.enterprise.context.Dependent;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing sql provider specific attributes.</p>
 */
@Dependent
public class SQLDataSetDefAttributesEditor extends Composite implements DataSetEditor.View, SQLDataSetDefEditor {
    
    private static final int DEFAULT_CACHE_MAX_ROWS = -1;
    private static final int DEFAULT_CACHE_MAX_BYTES = -1;
    private static final long DEFAULT_REFRESH_INTERVAL = -1;

    interface SQLDataSetDefAttributesEditorBinder extends UiBinder<Widget, SQLDataSetDefAttributesEditor> {}
    private static SQLDataSetDefAttributesEditorBinder uiBinder = GWT.create(SQLDataSetDefAttributesEditorBinder.class);

    @UiField
    HorizontalPanel sqlAttributesPanel;

    @UiField
    ValueBoxEditorDecorator<String> dataSource;
    
    private SQLDataSetDef dataSetDef;
    private boolean isEditMode;

    public SQLDataSetDefAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void set(DataSetDef dataSetDef) {
        this.dataSetDef = (SQLDataSetDef) dataSetDef;
    }

    @Override
    public Widget show(final boolean isEditMode) {
        this.isEditMode = isEditMode;

        // Clear the widget.
        clearScreen();

        // Attributes.
        buildAtrributes();

        return asWidget();
    }
    
    private void buildAtrributes() {
        // TODO: Check edit mode.
    }

    @Override
    public void hide() {
        sqlAttributesPanel.setVisible(false);
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
