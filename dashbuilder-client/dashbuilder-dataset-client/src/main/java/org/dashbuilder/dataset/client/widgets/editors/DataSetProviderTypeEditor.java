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

import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.ImageListEditorDecorator;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.client.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;
import org.dashbuilder.dataset.client.widgets.DataSetEditor;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing the data set provider type.</p>
 */
@Dependent
public class DataSetProviderTypeEditor extends Composite implements DataSetEditor.View, DataSetDefEditor {

    private static final int ICONS_SIZE = 250;
    private static final double ALPHA_ICON_NOT_SELECTED = 0.2;
    
    interface DataSetProviderTypeEditorBinder extends UiBinder<Widget, DataSetProviderTypeEditor> {}
    private static DataSetProviderTypeEditorBinder uiBinder = GWT.create(DataSetProviderTypeEditorBinder.class);

    @UiField
    ImageListEditorDecorator<DataSetProviderType> provider;

    private DataSetDef dataSetDef;
    private  boolean isEditMode;
    
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

    @Override
    public void set(DataSetDef dataSetDef) {
        this.dataSetDef = dataSetDef;
    }

    @Override
    public Widget show(final  boolean isEditMode) {
        this.isEditMode = isEditMode;
        provider.setEditMode(isEditMode);
        provider.setVisible(true);
        return asWidget();
    }

    @Override
    public void hide() {
        provider.setVisible(false);
    }

    @Override
    public void clear() {
        this.dataSetDef = null;
        this.provider.clear();
    }
    
    private Image buildTypeSelectorWidget(DataSetProviderType type) {
        Image typeIcon = null;
        switch (type) {
            case BEAN:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().javaIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.bean());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.bean());
                break;
            case CSV:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().csvIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.csv());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.csv());
                break;
            case SQL:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().sqlIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.sql());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.sql());
                break;
            case ELASTICSEARCH:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().elIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.el());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.el());
                break;
        }
        return typeIcon;
    }
    
}
