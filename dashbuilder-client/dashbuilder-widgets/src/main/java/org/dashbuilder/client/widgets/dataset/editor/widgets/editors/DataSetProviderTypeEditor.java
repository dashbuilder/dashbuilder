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
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import java.util.EnumMap;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.common.client.validation.editors.ImageListEditor;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;
import org.gwtbootstrap3.client.ui.Image;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.Map;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing the data set provider type.</p>
 *
 * @since 0.3.0 
 */
@Dependent
public class DataSetProviderTypeEditor extends AbstractDataSetDefEditor implements DataSetDefEditor {

    private static final int ICONS_SIZE = 160;

    interface DataSetProviderTypeEditorBinder extends UiBinder<Widget, DataSetProviderTypeEditor> {}
    private static DataSetProviderTypeEditorBinder uiBinder = GWT.create(DataSetProviderTypeEditorBinder.class);

    @UiField
    ImageListEditor<DataSetProviderType> provider;

    interface DataSetProviderTypeEditorStyle extends CssResource {
        String providerIcon();
    }
    @UiField
    DataSetProviderTypeEditorStyle style;
    
    private  boolean isEditMode;

    @Override
    public void showErrors(List<EditorError> errors) {
        consumeErrors(errors);
    }
    
    public DataSetProviderTypeEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        // Initialize the ImageListEditorDecorator with image for each data provider type.
        final Map<DataSetProviderType, ImageListEditor.Entry> providerEditorValues
                = new EnumMap<DataSetProviderType, ImageListEditor.Entry>(DataSetProviderType.class);
        for (final DataSetProviderType type : DataSetProviderType.values()) {
            final Image _image = buildTypeSelectorWidget(type);
            final String _heading = buildTypeSelectorHeading(type);
            final String _text = buildTypeSelectorText(type);
            if (_image != null) providerEditorValues.put(type, new ImageListEditor.Entry(_image, _heading, _text));
        }
        provider.setSize(ICONS_SIZE, ICONS_SIZE);
        provider.setImageStyle(style.providerIcon());
        provider.setAcceptableValues(providerEditorValues);
    }

    private String buildTypeSelectorHeading(DataSetProviderType type) {
        return type.name();
    }

    private String buildTypeSelectorText(DataSetProviderType type) {
        String description = null;
        switch (type) {
            case BEAN:
                description = DataSetEditorConstants.INSTANCE.bean_description();
                break;
            case CSV:
                description = DataSetEditorConstants.INSTANCE.csv_description();
                break;
            case SQL:
                description = DataSetEditorConstants.INSTANCE.sql_description();
                break;
            case ELASTICSEARCH:
                description = DataSetEditorConstants.INSTANCE.elasticSearch_description();
                break;
        }
        return description;
    }
    
    private Image buildTypeSelectorWidget(DataSetProviderType type) {
        Image typeIcon = null;
        switch (type) {
            case BEAN:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().javaIcon160().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.bean());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.bean());
                break;
            case CSV:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().csvIcon160().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.csv());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.csv());
                break;
            case SQL:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().sqlIcon160().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.sql());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.sql());
                break;
            case ELASTICSEARCH:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().elIcon160().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.elasticSearch());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.elasticSearch());
                break;
        }
        return typeIcon;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        provider.setEditMode(isEditMode);
    }

    public void clear() {
        super.clear();
        provider.clear();
    }

}
