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
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors.datacolumn;

import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.AbstractEditor;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.common.client.validation.editors.BooleanSwitchEditor;
import org.dashbuilder.common.client.validation.editors.DropDownImageListEditor;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;

import javax.enterprise.context.Dependent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This is the view implementation widget for editing data a given set column's name and type.</p>
 * 
 */
@Dependent
public class DataColumnBasicEditor extends AbstractEditor implements org.dashbuilder.dataset.client.validation.editors.DataColumnEditor {

    private static final int ICONS_SIZE = 16;
    
    interface DataColumnBasicEditorBinder extends UiBinder<Widget, DataColumnBasicEditor> {}
    private static DataColumnBasicEditorBinder uiBinder = GWT.create(DataColumnBasicEditorBinder.class);

    @UiField
    FlowPanel columnPanel;

    @UiField
    ValueBoxEditorDecorator<String> id;

    @UiField
    DropDownImageListEditor<ColumnType> columnType;
    
    private boolean isEditMode;

    public DataColumnBasicEditor() {
        // Initialize the widget.
        initWidget(uiBinder.createAndBindUi(this));

        setEditMode(true);
        
        // Initialize the dropdown editor with image for each column type.
        final Map<ColumnType, Image> providerEditorValues = new LinkedHashMap<ColumnType, Image>();
        for (final ColumnType type : ColumnType.values()) {
            final Image _image = buildTypeSelectorWidget(type);
            if (_image != null) providerEditorValues.put(type, _image);
        }
        columnType.setSize(ICONS_SIZE, ICONS_SIZE);
        columnType.setAcceptableValues(providerEditorValues);
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
         columnType.setEditMode(isEditMode);
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        consumeErrors(errors);
    }

    protected void consumeErrors(List<EditorError> errors) {
        for (EditorError error : errors) {
            if (error.getEditor().equals(this)) {
                error.setConsumed(true);
            }
        }
    }

    private Image buildTypeSelectorWidget(ColumnType type) {
        Image typeIcon = null;
        switch (type) {
            case LABEL:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().labelIconSmall().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.label());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.label());
                break;
            case TEXT:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().textIconSmall().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.text());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.test());
                break;
            case NUMBER:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().numberIconSmall().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.number());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.number());
                break;
            case DATE:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().dateIconSmall().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.date());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.date());
                break;
        }
        return typeIcon;
    }

}
