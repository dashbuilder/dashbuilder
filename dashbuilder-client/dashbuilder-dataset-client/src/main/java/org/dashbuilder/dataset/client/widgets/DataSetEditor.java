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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.client.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataset.client.validation.DataSetDefEditWorkflow;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetColumnsAndFilterEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * <p>Data Set Definition editor widget.</p>
 * <p>This widget allows edition or creation of a data set definitions.</p>
 *  
 * <p>This widget is presented using four views:</p>
 * <ul>
 *     <li>Provider type editor view - @see <code>org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor</code></li>     
 *     <li>Basic attributes editor view - @see <code>org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor</code></li>     
 *     <li>Advanced attributes editor view - @see <code>org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor</code></li>     
 *     <li>Column, filter and table results editor view - @see <code>org.dashbuilder.dataset.client.widgets.editors.DataSetColumnsAndFilterEditor</code></li>     
 * </ul>
 * 
 * <p>This editor provides three screens:</p>
 * <ul>
 *     <li>Basic data set attributes edition.</li>     
 *     <li>Advanced data set attributes edition.</li>
 *     <li>Data set columns and initial filter edition.</li>     
 * </ul> 
 */
@Dependent
public class DataSetEditor implements IsWidget {

    final DataSetDefEditWorkflow workflow = new DataSetDefEditWorkflow();
    
    public interface View extends IsWidget, HasHandlers, DataSetDefEditor {
        void set(DataSetDef dataSetDef);
        void clear();
        Widget show(final boolean isEditMode);
        void hide();
    }

    final DataSetProviderTypeEditor dataSetProviderTypeEditorView = new DataSetProviderTypeEditor();
    final DataSetBasicAttributesEditor dataSetBasicAttributesEditorView = new DataSetBasicAttributesEditor();
    final DataSetAdvancedAttributesEditor dataSetAdvancedAttributesEditorView = new DataSetAdvancedAttributesEditor();
    final DataSetColumnsAndFilterEditor dataSetColumnsAndFilterEditorView = new DataSetColumnsAndFilterEditor();
    final private FlowPanel mainPanel = new FlowPanel();
    private DataSetDef dataSetDef;
    private boolean isCreate;
    
    public DataSetEditor() {
        buildInitialView();
    }
    
    public void newDataSet(String uuid) {
        
        if (uuid == null || uuid.trim().length() == 0) {
            error("DataSetEditor#newDataSet - No UUID specified.");
            return;
        }

        isCreate = true;
        
        // Create a new data set def.
        DataSetDef dataSetDef = new DataSetDef();
        dataSetDef.setUUID(uuid);
        
        setDataSetDef(dataSetDef);
    }

    public void editDataSet(final String uuid) throws Exception{

        if (uuid == null || uuid.trim().length() == 0) {
            error("DataSetEditor#editDataSet - No UUID specified.");
            return;
        }

        isCreate = false;
        
        DataSetClientServices.get().fetchMetadata(uuid, new DataSetMetadataCallback() {
            @Override
            public void callback(DataSetMetadata metatada) {
                setDataSetDef(metatada.getDefinition());
            }

            @Override
            public void notFound() {
                error("Data set definition with uuid [" + uuid + "] not found.");
            }
        });
        
    }

    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    public void buildInitialView() {
        FlowPanel mainPanel = new FlowPanel();
        Hyperlink link = new InlineHyperlink();
        link.setText(DataSetEditorConstants.INSTANCE.newDataSet());
        link.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO: Generate uuid.
                newDataSet("new-uuid");
            }
        });
        mainPanel.add(link);
        this.mainPanel.clear();
        this.mainPanel.add(mainPanel);
    }

    public void buildBasicAttributesEditionView() {
        workflow.edit(dataSetBasicAttributesEditorView, dataSetDef);
        workflow.edit(dataSetProviderTypeEditorView, dataSetDef);

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(dataSetBasicAttributesEditorView.show(true));
        mainPanel.add(dataSetProviderTypeEditorView.show(isCreate));
        final ClickHandler nextButtonClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Save basic attributes (name and uuid).
                Set<ConstraintViolation<DataSetDef>> basicAttributesViolations = workflow.saveBasicAttributes();
                
                // Save provider type attribute.
                Set<ConstraintViolation<DataSetDef>> providerTypeAttributeViolations = workflow.saveProviderTypeAttribute();
                
                boolean isValid = basicAttributesViolations == null || basicAttributesViolations.isEmpty()
                    || providerTypeAttributeViolations == null || providerTypeAttributeViolations.isEmpty();
                if (isValid) {
                    buildAdvancedAttributesEditionView();                    
                }
                saveLog(basicAttributesViolations, providerTypeAttributeViolations);
            }
        };
        mainPanel.add(buildButtons(false, true, null, nextButtonClickHandler));
        this.mainPanel.clear();
        this.mainPanel.add(mainPanel);
    }

    public void buildAdvancedAttributesEditionView() {
        workflow.edit(dataSetAdvancedAttributesEditorView, dataSetDef);
        
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(10);
        mainPanel.add(dataSetAdvancedAttributesEditorView.show(true));

        final ClickHandler nextButtonClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                // validate and save attributes.
                Set<ConstraintViolation<DataSetDef>> advancedAttributesViolations = workflow.saveAdvancedAttributes();
                boolean isValid = advancedAttributesViolations == null || advancedAttributesViolations.isEmpty();
                if (isValid) {
                    // buildColumnsAndFilterEditionView();
                }
                saveLog(advancedAttributesViolations);
            }
        };
        mainPanel.add(buildButtons(false, true, null, nextButtonClickHandler));
        this.mainPanel.clear();
        this.mainPanel.add(mainPanel);
    }

    public void buildColumnsAndFilterEditionView() {
        // TODO: workflow.edit(dataSetBasicAttributesEditorView, dataSetDef);
        
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(dataSetBasicAttributesEditorView.show(true));
        mainPanel.add(dataSetColumnsAndFilterEditorView.show(true));
        
        final ClickHandler backButtonClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buildAdvancedAttributesEditionView();
            }
        };
        final ClickHandler nextButtonClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO
            }
        };
        mainPanel.add(buildButtons(true, true, backButtonClickHandler, nextButtonClickHandler));
        this.mainPanel.clear();
        this.mainPanel.add(mainPanel);
    }
    
    private void saveLog(Set<ConstraintViolation<DataSetDef>>... violations) {
        if (violations != null && violations.length > 0) {
            for (int x = 0; x < violations.length; x++) {
                Set<ConstraintViolation<DataSetDef>> driverViolation = violations[x];
                if (driverViolation != null) {
                    for (ConstraintViolation<DataSetDef> violation : driverViolation) {
                        GWT.log("Validation error - " + violation.getMessage());
                    }
                }
            }
        }
        if (dataSetDef != null) {
            GWT.log("DataSetDef uuid: " + dataSetDef.getUUID());
            GWT.log("DataSetDef name: " + dataSetDef.getName());
            GWT.log("DataSetDef provider: " + dataSetDef.getProvider());
            GWT.log("DataSetDef backend cache enabled: " + dataSetDef.isCacheEnabled());
            GWT.log("DataSetDef backend cache max rows: " + dataSetDef.getCacheMaxRows());
            GWT.log("DataSetDef client cache enabled: " + dataSetDef.isPushEnabled());
            GWT.log("DataSetDef client cache max rows: " + dataSetDef.getPushMaxSize());
            GWT.log("DataSetDef refresh always: " + dataSetDef.isRefreshAlways());
            GWT.log("DataSetDef refresh interval: " + dataSetDef.getRefreshTime());
        }
    }

    private Panel buildButtons(final boolean showBackButton, final boolean showNextButton, final ClickHandler backButtonClickHandler, final ClickHandler nextButtonClickHandler) {
        final HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(10);
        
        final com.github.gwtbootstrap.client.ui.Button cancelButton = new com.github.gwtbootstrap.client.ui.Button(DataSetEditorConstants.INSTANCE.cancel());
        buttonsPanel.add(cancelButton);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clear();
            }
        });

        if (showBackButton) {
            final com.github.gwtbootstrap.client.ui.Button backButton = new com.github.gwtbootstrap.client.ui.Button(DataSetEditorConstants.INSTANCE.back());
            buttonsPanel.add(backButton);
            if (backButtonClickHandler != null) backButton.addClickHandler(backButtonClickHandler);
        }

        if (showNextButton) {
            final com.github.gwtbootstrap.client.ui.Button nextButton = new com.github.gwtbootstrap.client.ui.Button(DataSetEditorConstants.INSTANCE.next());
            buttonsPanel.add(nextButton);
            if (nextButtonClickHandler != null) nextButton.addClickHandler(nextButtonClickHandler);
        }
        
        return buttonsPanel;
    }

    public void clear() {
        this.dataSetDef = null;
        dataSetProviderTypeEditorView.clear();
        dataSetBasicAttributesEditorView.clear();
        dataSetAdvancedAttributesEditorView.clear();
        dataSetColumnsAndFilterEditorView.clear();
        buildInitialView();
    }
    
    private void setDataSetDef(DataSetDef dataSetDef) {
        this.dataSetDef = dataSetDef;
        if (this.dataSetDef != null) buildBasicAttributesEditionView();
        else buildInitialView();
        dataSetProviderTypeEditorView.set(dataSetDef);
        dataSetBasicAttributesEditorView.set(dataSetDef);
        dataSetAdvancedAttributesEditorView.set(dataSetDef);
        dataSetColumnsAndFilterEditorView.set(dataSetDef);
    }

    // TODO: Display message to user.
    private void error(String message) {
        GWT.log(message);
    }
}
