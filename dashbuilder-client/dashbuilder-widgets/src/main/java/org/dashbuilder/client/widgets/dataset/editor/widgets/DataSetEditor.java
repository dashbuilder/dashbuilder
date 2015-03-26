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
package org.dashbuilder.client.widgets.dataset.editor.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.DataSetDefEditWorkflow;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.ClientDataSetManager;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerListener;

import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

/**
 * <p>Data Set Definition editor widget.</p>
 * <p>This widget allows edition or creation of a data set definitions.</p>
 *  
 * <p>The default view for this widget is displayed as:</p>
 * <ul>
 *     <li>Provider type editor view - @see <code>org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetProviderTypeEditor</code></li>     
 *     <li>Basic attributes editor view - @see <code>org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetBasicAttributesEditor</code></li>     
 *     <li>Advanced attributes editor view - @see <code>org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetAdvancedAttributesEditor</code></li>     
 *     <li>Table preview editor view - @see <code>org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetPreviewEditor</code></li>
 *     <li>Columns and initial filter editor view - @see <code>org.dashbuilder.client.widgets.dataset.editor.widgets.editors.TODO</code></li>*     
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
        View showPreviewTableEditionView(DisplayerListener tableListener);
        View showColumnsAndFilterEditionView(DataSet dataSet);
        View showAdvancedAttributesEditionView();
        View showNextButton(String title, ClickHandler nextHandler);
        View showCancelButton(ClickHandler cancelHandler);
        View onSave();
        Set getViolations();
        View clear();
    }

    final View view = new DataSetEditorView();

    private DataSetDef dataSetDef;
    private DataSet dataSet;
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

        // Restart workflow.
        edit();
        
        // Build provider selection view.
        showProviderSelectionView();

        // Next button.
        view.showNextButton(DataSetEditorConstants.INSTANCE.next(), new ClickHandler() {
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

                    // Restart workflow.
                    edit();

                    // Build basic attributes view.
                    showBasicAttributesEditionView();
                    
                    // Next button.
                    view.showNextButton(DataSetEditorConstants.INSTANCE.test(), new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            // Save basic attributes (name and uuid) and provider type attribute.
                            // Check if exist validation violations.
                            final Set violations = save();
                            if (isValid(violations)) {

                                // Restart workflow.
                                edit();

                                // Build views.
                                showBasicAttributesEditionView();
                                showAdvancedAttributesEditionView();
                                showPreviewTableEditionView();
                                
                                view.showNextButton(DataSetEditorConstants.INSTANCE.save(),new ClickHandler() {
                                    @Override
                                    public void onClick(ClickEvent event) {
                                        final Set violations = save();
                                        if (isValid(violations)) {
                                            // Valid
                                            
                                            // TODO
                                            GWT.log("Data set edition finished.");
                                        }
                                        saveLog(violations);

                                    }
                                });
                            }
                            saveLog(violations, violations);
                        }
                    });
                }
                saveLog(violations, violations);
            }
        });
                
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
                showBasicAttributesEditionView();
                
                view.showNextButton(DataSetEditorConstants.INSTANCE.test(), new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        // TODO: Workflow for edit mode.
                    }
                });
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

    private void showProviderSelectionView() {
        // Show provider selection widget.
        view.showProviderSelectionView();
    }
    
    private void showBasicAttributesEditionView() {
        view.showBasicAttributesEditionView();

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
    }
    
    
    private void showColumnsAndFilterEditionView() {
        view.showColumnsAndFilterEditionView(dataSet);
    }

    private void showPreviewTableEditionView() {
        
        if (dataSetDef != null) {
            // Register the data set in backend as non public.
            dataSetDef.setPublic(false);
            DataSetClientServices.get().registerDataSetDef(dataSetDef);
        }
        
        view.showBasicAttributesEditionView()
            .showPreviewTableEditionView(tablePreviewListener);
    }
    
    private void showAdvancedAttributesEditionView() {
        view.showBasicAttributesEditionView()
            .showAdvancedAttributesEditionView();
    }
    
    private View edit() {
        return view.edit(dataSetDef, workflow).showCancelButton(cancelHandler);
    }
    
    private Set save() {
        workflow.save();
        view.onSave();
        return view.getViolations();
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

    /**
     * <p>When creating the table preview screen, this listener waits for data set available and then performs other operations.</p> 
     */
    private final DisplayerListener tablePreviewListener = new DisplayerListener() {
        @Override
        public void onDraw(Displayer displayer) {

            if (displayer != null) {
                final DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
                DataSetEditor.this.dataSet = dataSet;
                
                if (dataSet != null) {
                    // Register data set on client.
                    ClientDataSetManager.get().registerDataSet(dataSet);

                    // Show initial filter and columns edition view.
                    showColumnsAndFilterEditionView();
                }
            }
        }

        @Override
        public void onRedraw(Displayer displayer) {

        }

        @Override
        public void onClose(Displayer displayer) {

        }

        @Override
        public void onGroupIntervalsSelected(Displayer displayer, DataSetGroup groupOp) {

        }

        @Override
        public void onGroupIntervalsReset(Displayer displayer, List<DataSetGroup> groupOps) {

        }
    };
    
    private void clear() {
        this.dataSetDef = null;
        this.dataSet = null;
        view.clear();
        view.showInitialView(newDataSetHandler);
    }
    
    private boolean isValid(Set violations) {
        return violations.isEmpty();
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
        }
    }

}
