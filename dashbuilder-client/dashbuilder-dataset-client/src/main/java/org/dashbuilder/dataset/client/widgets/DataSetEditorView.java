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
package org.dashbuilder.dataset.client.widgets;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.client.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataset.client.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;

// TODO
@Dependent
public class DataSetEditorView extends Composite implements DataSetEditor.View {

    protected static final String EMPTY_STRING = "";
    
    interface DataSetEditorViewBinder extends UiBinder<Widget, DataSetEditorView> {}
    private static DataSetEditorViewBinder uiBinder = GWT.create(DataSetEditorViewBinder.class);

    interface DataSetEditorViewStyle extends CssResource {

    }

    @UiField
    DataSetEditorViewStyle style;
    
    @UiField
    Label titleLabel;

    @UiField
    HorizontalPanel typesPanel;

    @UiField
    HorizontalPanel buttonsPanel;

    @UiField
    Button cancelButton;

    @UiField
    Button backButton;

    @UiField
    Button testButton;

    @UiField
    Button nextButton;

    @UiField
    WellForm attributesForm;
    
    @UiField
    VerticalPanel attributesPanel;

    @UiField
    TextBox attributeUUID;

    @UiField
    TextBox attributeName;
            
    @UiField
    Label attributeBackendCacheStatus;

    @UiField
    TextBox attributeMaxRows;

    private DataSetEditor presenter;
    private DataSetDef dataSetDef;
    
    public DataSetEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    

    @Override
    public void init(DataSetEditor presenter) {
        this.dataSetDef = null;
        this.presenter = presenter;
        // Clear the widget.
        clear();
    }

    @Override
    public void create(String uuid) {
        // Clear the widget.
        clear();
        
        // Create a new data set def.
        dataSetDef = new DataSetDef();
        dataSetDef.setUUID(uuid);
        
        // Show provider type selection screen.
        screenProviderTypeSelection();
    }
    
    private void screenProviderTypeSelection() {
        // Clear the widget.
        clearScreen();
        
        // Set the view components attributes.
        titleLabel.setText(DataSetEditorConstants.INSTANCE.selectType());
        titleLabel.setType(LabelType.INFO);
        titleLabel.setVisible(true);
        cancelButton.setVisible(true);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clear();
            }
        });

        nextButton.setVisible(true);
        nextButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                screenDataSetAttributes();
            }
        });
        buttonsPanel.setVisible(true);

        // Show available types.
        DataSetProviderType[] types = DataSetProviderType.values();
        for (final DataSetProviderType type : types) {
            final Image typeSelector = buildTypeSelectorWidget(type);
            if (typeSelector != null) {
                typeSelector.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        selectProviderType(type, typeSelector);
                    }
                });
                typesPanel.add(typeSelector);
            }
        }
        typesPanel.setVisible(true);
    }

    private void screenDataSetAttributes() {
        // Clear the widget.
        clearScreen();

        // Build common attributes editors.
        buildCommonAttributesEditors();
        
        // Build backend cache attributes editors.
        
        // Build client cache attributes editors.

        // Build refresh policy attributes editors.

        // Build specific provider attributes editors.
        
        // Show the attributes edition widgets.
        attributesForm.setVisible(true);
        attributesPanel.setVisible(true);

        // Screen buttons.
        cancelButton.setVisible(true);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clear();
            }
        });
        testButton.setVisible(true);
        testButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO
            }
        });
        nextButton.setVisible(true);
        nextButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attributesForm.submit();
                // TODO
            }
        });
        buttonsPanel.setVisible(true);
    }
    
    private void buildCommonAttributesEditors() {
        attributeUUID.setText(dataSetDef.getUUID());
        // TODO: if (dataSetDef.getName() != null) attributeName.setText(dataSetDef.getName());
    }

    private void buildBackendCacheAttributesEditors() {
        attributeBackendCacheStatus.setText(dataSetDef.isCacheEnabled() ? 
            DataSetEditorConstants.INSTANCE.on() : DataSetEditorConstants.INSTANCE.off() );
        attributeBackendCacheStatus.setType(dataSetDef.isCacheEnabled() ? LabelType.SUCCESS : LabelType.WARNING );
        attributeMaxRows.setValue(Integer.toString(dataSetDef.getCacheMaxRows()));
    }

    private void selectProviderType(DataSetProviderType type, Image typeSelector) {
        titleLabel.setText(typeSelector.getTitle());
        titleLabel.setType(LabelType.SUCCESS);
        dataSetDef.setProvider(type);        
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
        if (typeIcon != null) typeIcon.setSize("250px", "250px");
        return typeIcon;
    }

    @Override
    public void edit(DataSetDef dataSetDef) {
        // Clear the widget.
        clear();
        
        // TODO
    }

    @Override
    public void clear() {
        clearScreen();
        clearStatus();
    }
    
    private void clearScreen() {
        // Base.
        titleLabel.setText(EMPTY_STRING);
        titleLabel.setVisible(false);
        
        // Creation type selection.
        typesPanel.clear();
        typesPanel.setVisible(false);
        
        // Buttons.
        cancelButton.setVisible(false);
        backButton.setVisible(false);
        testButton.setVisible(false);
        nextButton.setVisible(false);
        buttonsPanel.setVisible(false);
        
        // Attributes edition.
        attributeMaxRows.setValue(EMPTY_STRING);
        attributeBackendCacheStatus.setText(EMPTY_STRING);
        attributeName.setValue(EMPTY_STRING);
        attributeUUID.setVisible(false);
        attributesForm.setVisible(false);
        attributesPanel.setVisible(false);
    }
    
    private void clearStatus() {
        dataSetDef = null;
    }

}
