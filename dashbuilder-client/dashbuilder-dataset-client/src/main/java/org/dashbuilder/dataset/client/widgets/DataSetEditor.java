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
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.client.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataset.client.validation.DataSetDefEditWorkflow;
import org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetColumnsAndFilterEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.dataset.client.widgets.editors.sql.SQLDataSetDefAttributesEditor;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;

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
    
    public interface View extends IsWidget, HasHandlers {
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
    
    public DataSetEditor newDataSet(String uuid) {
        
        if (uuid == null || uuid.trim().length() == 0) {
            error("DataSetEditor#newDataSet - No UUID specified.");
            return this;
        }

        isCreate = true;
        
        // Create a new data set def.
        final DataSetDef dataSetDef = new DataSetDef();
        dataSetDef.setUUID(uuid);
        
        setDataSetDef(dataSetDef);

        return this;
    }

    public DataSetEditor editDataSet(final String uuid) throws Exception{

        if (uuid == null || uuid.trim().length() == 0) {
            error("DataSetEditor#editDataSet - No UUID specified.");
            return this;
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
        return this;
    }

    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    public DataSetEditor buildInitialView() {
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
        return this;
    }

    public DataSetEditor buildBasicAttributesEditionView() {
        workflow.clear().edit(dataSetBasicAttributesEditorView, dataSetDef).edit(dataSetProviderTypeEditorView, dataSetDef);

        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(dataSetBasicAttributesEditorView.show(true));
        mainPanel.add(dataSetProviderTypeEditorView.show(isCreate));
        final ClickHandler nextButtonClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Save basic attributes (name and uuid) and provider type attribute.
                // Check if exist validation violations.
                final Set<ConstraintViolation<? extends  DataSetDef>> violations = workflow.save();
                if (isValid(violations)) {
                    // Build the data set class instance for the given provider type.
                    final DataSetDef _dataSetDef = DataSetProviderType.createDataSetDef(dataSetDef.getProvider());
                    _dataSetDef.setUUID(dataSetDef.getUUID());
                    _dataSetDef.setName(dataSetDef.getName());
                    setDataSetDef(_dataSetDef);
                    buildAdvancedAttributesEditionView();                    
                }
                saveLog(violations, violations);
            }
        };
        mainPanel.add(buildButtons(false, true, null, nextButtonClickHandler));
        this.mainPanel.clear();
        this.mainPanel.add(mainPanel);
        return this;
    }

    public DataSetEditor buildAdvancedAttributesEditionView() {
        workflow.clear().edit(dataSetAdvancedAttributesEditorView, dataSetDef);
        
        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(10);
        mainPanel.add(dataSetAdvancedAttributesEditorView.show(true));

        switch (dataSetDef.getProvider()) {
            case SQL:
                mainPanel.add(buildSQLAttributesEditorView());
        }
        final ClickHandler nextButtonClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Validate and save attributes.
                final Set<ConstraintViolation<? extends DataSetDef>> violations = workflow.save();
                if (isValid(violations)) {
                    // Valid
                    buildColumnsAndFilterEditionView();
                }
                saveLog(violations);
            }
        };
        mainPanel.add(buildButtons(false, true, null, nextButtonClickHandler));
        this.mainPanel.clear();
        this.mainPanel.add(mainPanel);
        return this;
    }

    private Widget buildSQLAttributesEditorView() {
        final SQLDataSetDefAttributesEditor sqlDataSetDefAttributesEditor = new SQLDataSetDefAttributesEditor();
        workflow.edit(sqlDataSetDefAttributesEditor, (SQLDataSetDef) dataSetDef);
        return sqlDataSetDefAttributesEditor.show(true);
    }

    public DataSetEditor buildColumnsAndFilterEditionView() {
        // TODO: workflow.clear().edit(dataSetBasicAttributesEditorView, dataSetDef);

        final VerticalPanel mainPanel = new VerticalPanel();
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
        return this;
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

    public DataSetEditor clear() {
        this.dataSetDef = null;
        dataSetProviderTypeEditorView.clear();
        dataSetBasicAttributesEditorView.clear();
        dataSetAdvancedAttributesEditorView.clear();
        dataSetColumnsAndFilterEditorView.clear();
        buildInitialView();
        return this;
    }
    
    private void setDataSetDef(DataSetDef dataSetDef) {
        this.dataSetDef = dataSetDef;
        dataSetProviderTypeEditorView.set(dataSetDef);
        dataSetBasicAttributesEditorView.set(dataSetDef);
        dataSetAdvancedAttributesEditorView.set(dataSetDef);
        dataSetColumnsAndFilterEditorView.set(dataSetDef);
    }
    
    private boolean isValid(Set<ConstraintViolation<? extends  DataSetDef>> violations) {
        return violations == null || (violations != null && violations.isEmpty());
        
    }

    // TODO: Display message to user.
    private void error(String message) {
        GWT.log(message);
    }

    // TODO: For testing.
    private void saveLog(Set<ConstraintViolation<? extends DataSetDef>>... violations) {
        if (violations != null && violations.length > 0) {
            for (int x = 0; x < violations.length; x++) {
                Set<ConstraintViolation<? extends DataSetDef>> driverViolation = violations[x];
                if (driverViolation != null) {
                    for (ConstraintViolation<? extends DataSetDef> violation : driverViolation) {
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
            if (dataSetDef instanceof SQLDataSetDef) {
                GWT.log("SQLDataSetDef data source: " + ((SQLDataSetDef)dataSetDef).getDataSource());
            }
        }
    }

}
