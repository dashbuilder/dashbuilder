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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.client.validation.DataSetDefEditWorkflow;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;

import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * <p>Data Set Definition editor widget.</p>
 * <p>This widget allows edition or creation of a data set definitions.</p>
 *  
 * <p>The default view for this widget is displayed as:</p>
 * <ul>
 *     <li>Provider type editor view - @see <code>org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor</code></li>     
 *     <li>Basic attributes editor view - @see <code>org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor</code></li>     
 *     <li>Advanced attributes editor view - @see <code>org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor</code></li>     
 *     <li>Column, filter and table results editor view - @see <code>org.dashbuilder.dataset.client.widgets.editors.DataSetColumnsAndFilterEditor</code></li>     
 * </ul>
 * 
 * <p>This editor provides four edition screens:</p>
 * <ul>
 *     <li>Provider selection (Only if creating new data set).</li>
 *     <li>Basic data set attributes & Provider specific attributes.</li>     
 *     <li>Basic data set attributes & Data set columns and initial filter edition.</li>     
 *     <li>Basic data set attributes & Advanced data set attributes edition.</li>
 * </ul> 
 */
@Dependent
public class DataSetEditor implements IsWidget {

    final DataSetDefEditWorkflow workflow = new DataSetDefEditWorkflow();
    
    public interface View extends IsWidget, HasHandlers {
        View edit(DataSetDef dataSetDef, DataSetDefEditWorkflow workflow);
        View setEditMode(boolean editMode);
        View showInitialView(ClickHandler newDataSetHandler);
        View showProviderSelectionView();
        View showBasicAttributesEditionView();
        View showSQLAttributesEditorView();
        View showBeanAttributesEditorView();
        View showCSVAttributesEditorView();
        View showELAttributesEditorView();
        View showColumnsAndFilterEditionView();
        View showAdvancedAttributesEditionView();
        View showNextButton(ClickHandler nextHandler);
        View showBackButton(ClickHandler backHandler);
        View showCancelButton(ClickHandler cancelHandler);
        View onSave(Set<ConstraintViolation<? extends DataSetDef>> violations);
        View clear();
    }

    final View view = new DataSetEditorView();

    private DataSetDef dataSetDef;
    private boolean isEdit;
    
    public DataSetEditor() {
        view.showInitialView(newDataSetHandler);
    }
    
    public DataSetEditor newDataSet(String uuid) {
        
        if (uuid == null || uuid.trim().length() == 0) {
            error("DataSetEditor#newDataSet - No UUID specified.");
            return this;
        }

        isEdit = false;
        
        // Create a new data set def.
        final DataSetDef dataSetDef = new DataSetDef();
        dataSetDef.setUUID(uuid);
        
        this.dataSetDef = dataSetDef;

        view.setEditMode(isEdit);
        buildProviderSelectionView();
                
        return this;
    }

    public DataSetEditor editDataSet(final String uuid) throws Exception{

        if (uuid == null || uuid.trim().length() == 0) {
            error("DataSetEditor#editDataSet - No UUID specified.");
            return this;
        }

        isEdit = true;
        view.setEditMode(isEdit);
        
        DataSetClientServices.get().fetchMetadata(uuid, new DataSetMetadataCallback() {
            @Override
            public void callback(DataSetMetadata metatada)
            {
                DataSetEditor.this.dataSetDef = metatada.getDefinition();
                buildBasicAttributesEditionView();
            }

            @Override
            public void notFound() {
                error("Data set definition with uuid [" + uuid + "] not found.");
                // TODO: Show error popup?
            }
        });
        return this;
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    private void buildProviderSelectionView() {
        view.edit(dataSetDef, workflow)
                // Show provider selection widget.
                .showProviderSelectionView()
                // Show next button.
                .showNextButton(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        // Save basic attributes (name and uuid) and provider type attribute.
                        // Check if exist validation violations.
                        final Set<ConstraintViolation<? extends DataSetDef>> violations = save();
                        if (isValid(violations)) {
                            // Build the data set class instance for the given provider type.
                            final DataSetDef _dataSetDef = DataSetProviderType.createDataSetDef(dataSetDef.getProvider());
                            _dataSetDef.setUUID(dataSetDef.getUUID());
                            DataSetEditor.this.dataSetDef = _dataSetDef;
                            buildBasicAttributesEditionView();
                        }
                        saveLog(violations, violations);
                    }
                })
                // Show cancel button.
                .showCancelButton(cancelHandler);
    }
    
    private void buildBasicAttributesEditionView() {
        view.edit(dataSetDef, workflow).showBasicAttributesEditionView();

        switch (dataSetDef.getProvider()) {
            case SQL:
                view.showSQLAttributesEditorView();
                break;
            case CSV:
                view.showCSVAttributesEditorView();
                break;
            case BEAN:
                view.showBeanAttributesEditorView();
                break;
            case ELASTICSEARCH:
                view.showELAttributesEditorView();
                break;
        }
        
        view.showNextButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Save basic attributes (name and uuid) and provider type attribute.
                // Check if exist validation violations.
                final Set<ConstraintViolation<? extends DataSetDef>> violations = save();
                if (isValid(violations)) {
                    buildColumnsAndFilterEditionView();
                }
                saveLog(violations, violations);
            }
        });
        view.showCancelButton(cancelHandler);
    }

    private void buildColumnsAndFilterEditionView() {
        view.edit(dataSetDef, workflow)
            .showBasicAttributesEditionView()
            .showColumnsAndFilterEditionView();

        view.showNextButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buildAdvancedAttributesEditionView();
            }
        });
        view.showCancelButton(cancelHandler);
        view.showBackButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buildBasicAttributesEditionView();
            }
        });
    }
    
    private void buildAdvancedAttributesEditionView() {
        view.edit(dataSetDef, workflow)
            .showBasicAttributesEditionView()
            .showAdvancedAttributesEditionView();
        
        view.showNextButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Validate and save attributes.
                final Set<ConstraintViolation<? extends DataSetDef>> violations = save();
                if (isValid(violations)) {
                    // Valid
                    buildColumnsAndFilterEditionView();
                }
                saveLog(violations);
            }
        });
        
        view.showCancelButton(cancelHandler);
        view.showBackButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buildColumnsAndFilterEditionView();
            }
        });
    }
    
    private Set<ConstraintViolation<? extends DataSetDef>> save() {
        final Set<ConstraintViolation<? extends DataSetDef>> violations = workflow.save();
        view.onSave(violations);
        return violations;
    }
    
    private final ClickHandler cancelHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            clear();
        }
    };
    private final ClickHandler newDataSetHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            // TODO: Generate uuid using the backend uuid generator. Perform a RPC call.
            newDataSet("new-uuid");;
        }
    };
    
    private void clear() {
        this.dataSetDef = null;
        view.clear();
        view.showInitialView(newDataSetHandler);
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
                GWT.log("SQLDataSetDef schema: " + ((SQLDataSetDef)dataSetDef).getDbSchema());
                GWT.log("SQLDataSetDef table: " + ((SQLDataSetDef)dataSetDef).getDbTable());
            }
            if (dataSetDef instanceof CSVDataSetDef) {
                GWT.log("CSVDataSetDef file path: " + ((CSVDataSetDef)dataSetDef).getFilePath());
                GWT.log("CSVDataSetDef file URL: " + ((CSVDataSetDef)dataSetDef).getFileURL());
                GWT.log("CSVDataSetDef sep char: " + ((CSVDataSetDef)dataSetDef).getSeparatorChar());
                GWT.log("CSVDataSetDef quote char: " + ((CSVDataSetDef)dataSetDef).getQuoteChar());
                GWT.log("CSVDataSetDef escape char: " + ((CSVDataSetDef)dataSetDef).getEscapeChar());
                GWT.log("CSVDataSetDef date pattern: " + ((CSVDataSetDef)dataSetDef).getDatePattern());
                GWT.log("CSVDataSetDef number pattern: " + ((CSVDataSetDef)dataSetDef).getNumberPattern());
            }
            // TODO: Print data set's attributes for other types
        }
    }

}
