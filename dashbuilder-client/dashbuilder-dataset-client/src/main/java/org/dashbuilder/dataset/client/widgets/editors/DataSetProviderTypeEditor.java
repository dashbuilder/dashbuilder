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
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.client.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataset.client.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.dataset.client.widgets.DataSetEditor;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;
import java.util.*;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing the data set provider type.</p>
 */
@Dependent
public class DataSetProviderTypeEditor extends Composite implements DataSetEditor.View {

    private static final String ICONS_SIZE = "250px";
    private static final double ALPHA_ICON_NOT_SELECTED = 0.2;
    
    interface DataSetProviderTypeEditorBinder extends UiBinder<Widget, DataSetProviderTypeEditor> {}
    private static DataSetProviderTypeEditorBinder uiBinder = GWT.create(DataSetProviderTypeEditorBinder.class);

    interface DataSetProviderTypeEditorStyle extends CssResource {
        String mainPanel();
        String imagePointer();
    }

    @UiField DataSetProviderTypeEditorStyle style;
    
    @UiField
    HorizontalPanel typesPanel;

    private DataSetDef dataSetDef;
    Map<DataSetProviderType ,Image> typeSelectors = new LinkedHashMap<DataSetProviderType, Image>();
    private  boolean isEditMode;
    
    public DataSetProviderTypeEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void set(DataSetDef dataSetDef) {
        this.dataSetDef = dataSetDef;
    }

    @Override
    public Widget show(final  boolean isEditMode) {
        this.isEditMode = isEditMode;
        
        // Clear the widget.
        clearScreen();

        // Show available types.
        final DataSetProviderType[] types = DataSetProviderType.values();
        for (final DataSetProviderType type : types) {
            final Image typeSelector = buildTypeSelectorWidget(type);

            if (typeSelector != null) {
                if (isEditMode) {
                    typeSelector.addStyleName(style.imagePointer());
                    typeSelector.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            selectProviderType(type);
                        }
                    });
                    
                }
                typesPanel.add(typeSelector);
                typeSelectors.put(type, typeSelector);
            }
        }
        typesPanel.setVisible(true);
        
        // If defintion has a provider type set, show it.
        if (dataSetDef != null && dataSetDef.getProvider() != null) selectProviderType(dataSetDef.getProvider());

        return asWidget();
    }

    @Override
    public void hide() {
        typesPanel.setVisible(false);
    }

    @Override
    public void clear() {
        clearScreen();
        clearStatus();
    }
    
    private void clearScreen() {
        typesPanel.clear();
    }
    
    private void clearStatus() {
        this.dataSetDef = null;
        this.typeSelectors.clear();
    }

    private void selectProviderType(DataSetProviderType type) {
        dataSetDef.setProvider(type);
        
        // Apply alphas.
        for (Map.Entry<DataSetProviderType, Image> entry : typeSelectors.entrySet()) {
            DataSetProviderType _type = entry.getKey();
            Image typeSelector = entry.getValue();
            if (type.equals(_type)) applyAlpha(typeSelector, 1);
            else applyAlpha(typeSelector, ALPHA_ICON_NOT_SELECTED);
        }
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
        if (typeIcon != null) typeIcon.setSize(ICONS_SIZE, ICONS_SIZE);
        return typeIcon;
    }
    
    private void applyAlpha(final Image image, final double alpha) {
        image.getElement().setAttribute("style", "filter: alpha(opacity=5);opacity: " + alpha);
        image.setSize(ICONS_SIZE, ICONS_SIZE);
    }
}
