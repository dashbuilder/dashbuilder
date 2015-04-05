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

import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.common.client.validation.editors.ImageListEditor;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;

import javax.enterprise.context.Dependent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing the data set provider type.</p>
 */
@Dependent
public class DataSetProviderTypeEditor extends AbstractDataSetDefEditor implements DataSetDefEditor {

    private static final int ICONS_SIZE = 150;

    interface DataSetProviderTypeEditorBinder extends UiBinder<Widget, DataSetProviderTypeEditor> {}
    private static DataSetProviderTypeEditorBinder uiBinder = GWT.create(DataSetProviderTypeEditorBinder.class);

    @UiField
    ImageListEditor<DataSetProviderType> provider;

    private  boolean isEditMode;

    @Override
    public void showErrors(List<EditorError> errors) {
        consumeErrors(errors);
    }
    
    public DataSetProviderTypeEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        // Initialize the ImageListEditorDecorator with image for each data provider type.
        final Map<DataSetProviderType, Image> providerEditorValues = new LinkedHashMap<DataSetProviderType, Image>();
        for (final DataSetProviderType type : DataSetProviderType.values()) {
            final Image _image = buildTypeSelectorWidget(type);
            if (_image != null) providerEditorValues.put(type, _image);
        }
        provider.setSize(ICONS_SIZE, ICONS_SIZE);
        provider.setAcceptableValues(providerEditorValues);
    }

    private Image buildTypeSelectorWidget(DataSetProviderType type) {
        Image typeIcon = null;
        switch (type) {
            case BEAN:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().javaIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.bean());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.bean());
                break;
            case CSV:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().csvIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.csv());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.csv());
                break;
            case SQL:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().sqlIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.sql());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.sql());
                break;
            case ELASTICSEARCH:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().elIconLarge().getSafeUri());
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

}
