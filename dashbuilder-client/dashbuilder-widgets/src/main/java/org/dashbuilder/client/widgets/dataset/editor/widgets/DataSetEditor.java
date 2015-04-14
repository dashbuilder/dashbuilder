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
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetColumnsEditor;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.ClientDataSetManager;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.client.uuid.ClientUUIDGenerator;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.validation.ConstraintViolation;
import java.util.*;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

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

    private static final String EDIT_SUFFIX = "_edit";
    final DataSetDefEditWorkflow workflow = new DataSetDefEditWorkflow();
    
    public interface View extends IsWidget, HasHandlers {
        View edit(final DataSetDef dataSetDef, final DataSetDefEditWorkflow workflow);
        View setEditMode(final boolean editMode);
        View showHomeView(final int dsetCount, final ClickHandler newDataSetHandler);
        View showProviderSelectionView();
        View showBasicAttributesEditionView();
        View showSQLAttributesEditorView(ClickHandler testHandler);
        View showBeanAttributesEditorView(ClickHandler testHandler);
        View showCSVAttributesEditorView(ClickHandler testHandler);
        View showELAttributesEditorView(ClickHandler testHandler);
        View showPreviewTableEditionView(final DisplayerListener tableListener);
        View showColumnsEditorView(final List<DataColumn> columns, final DataSet dataSet, final DataSetColumnsEditor.ColumnsChangedEventHandler columnsChangedEventHandler);
        View showFilterEditionView(final DataSet dataSet, final DataSetFilterEditor.Listener filterListener);
        View showAdvancedAttributesEditionView();
        View showNextButton(String title, String helpText, ClickHandler nextHandler);
        View showCancelButton(ClickHandler cancelHandler);
        View onSave();
        Set getViolations();
        View clear();
    }

    final View view = new DataSetEditorView();

    private DataSetDef dataSetDef;
    private List<DataColumn> columns;
    private DataSetDef edit;
    private boolean isHomeViewVisible;
    
    public DataSetEditor() {
        showHomeView();
    }

    public DataSetEditor(final String width) {
        showHomeView();
        setWidth(width);
    }
    
    public DataSetEditor setWidth(final String w) {
        ((DataSetEditorView)view).setWidth(w);
        return this;
    }
    
    public DataSetEditor newDataSet(String uuid) {
        
        if (uuid == null || uuid.trim().length() == 0) {
            error("DataSetEditor#newDataSet - No UUID specified.");
            return this;
        }

        
        // Create a new data set def.
        this.dataSetDef = createDataSetDef(uuid, null);

        edit = null;
        view.setEditMode(false);

        // Restart workflow.
        edit();
        
        // Build provider selection view.
        showProviderSelectionView();

        // Next button.
        view.showNextButton(DataSetEditorConstants.INSTANCE.next(),
                DataSetEditorConstants.INSTANCE.next_description(), 
                providerScreenNextButtonHandler);
                
        return this;
    }
    
    public DataSetEditor editDataSet(final String uuid) throws Exception{

        if (uuid == null || uuid.trim().length() == 0) {
            error("DataSetEditor#editDataSet - No UUID specified.");
            return this;
        }

        DataSetClientServices.get().fetchMetadata(uuid, new DataSetMetadataCallback() {
            @Override
            public void callback(DataSetMetadata metatada)
            {
                final DataSetDef def = metatada.getDefinition();
                final String uuid = def.getUUID();
                
                // Clone the definition in order to edit the cloned copy.
                DataSetEditor.this.dataSetDef = def.clone();
                DataSetEditor.this.dataSetDef.setUUID(uuid + EDIT_SUFFIX);
                DataSetEditor.this.dataSetDef.setPublic(false);

                edit = def;
                view.setEditMode(true);
                
                // Restart workflow.
                edit();

                // Build views.
                showBasicAttributesEditionView();
                showProviderSpecificAttributesEditionView();
                showPreviewTableEditionView();
                showAdvancedAttributesEditionView();
                
                view.showNextButton(DataSetEditorConstants.INSTANCE.save(),
                        DataSetEditorConstants.INSTANCE.save_description(),
                        saveButtonHandler);
            }

            @Override
            public void notFound() {
                error("Data set definition with uuid [" + uuid + "] not found.");
                // TODO: Show error popup?
            }
        });
        return this;
    }
    
    private DataSetDef createDataSetDef(final String uuid, final DataSetProviderType type) {
        DataSetDef result = new DataSetDef();
        if (type != null) result = DataSetProviderType.createDataSetDef(dataSetDef.getProvider());
        result.setUUID(uuid);
        result.setPublic(false);
        return result;
    }

    private final ClickHandler providerScreenNextButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            // Save basic attributes (name and uuid) and provider type attribute.
            // Check if exist validation violations.
            final Set<ConstraintViolation<? extends DataSetDef>> violations = save();
            if (isValid(violations)) {
                // Build the data set class instance for the given provider type.
                DataSetEditor.this.dataSetDef = createDataSetDef(dataSetDef.getUUID(), dataSetDef.getProvider());

                // Restart workflow.
                edit();

                // Build basic attributes view.
                showBasicAttributesEditionView();
                showProviderSpecificAttributesEditionView();

            }
            log(violations, violations);
        }
    };

    private final ClickHandler testButtonHandler = new ClickHandler() {
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
                showProviderSpecificAttributesEditionView();
                showPreviewTableEditionView();

                view.showNextButton(DataSetEditorConstants.INSTANCE.next(),
                        DataSetEditorConstants.INSTANCE.next_description(),
                        advancedAttrsButtonHandler);
            }
            log(violations, violations);
        }
    };

    private final ClickHandler advancedAttrsButtonHandler = new ClickHandler() {
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
                showProviderSpecificAttributesEditionView();
                showPreviewTableEditionView();
                showAdvancedAttributesEditionView();

                view.showNextButton(DataSetEditorConstants.INSTANCE.save(),
                        DataSetEditorConstants.INSTANCE.save_description(),
                        saveButtonHandler);
            }
            log(violations, violations);
        }
    };

    private final ClickHandler saveButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            final Set violations = save();
            if (isValid(violations)) {
                // Valid
                GWT.log("Data set edition finished.");
                update();

                try {
                    persist();
                    clear();
                } catch (Exception e) {
                    // TODO: Display error.
                    e.printStackTrace();
                    GWT.log("Error persisting data set defintion with uuid [" + dataSetDef.getUUID() + "]. Message: " + e.getMessage());
                }
            }
            log(violations);

        }
    };

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    private void persist() throws Exception {
        if (edit == null) 
        {
            // If creating a new data set, just persist it.
            dataSetDef.setPublic(true);
            DataSetClientServices.get().persistDataSetDef(dataSetDef);
        }
        else {
            // If editing an existing data set, remove original data set and persist the new edited one.
            final DataSetClientServices clientServices = DataSetClientServices.get();
            clientServices.removeDataSetDef(edit.getUUID());
            this.dataSetDef.setUUID(edit.getUUID());
            this.dataSetDef.setPublic(true);
            registerDataSetDef();
            DataSetClientServices.get().persistDataSetDef(dataSetDef);
        }
    }
    
    private void update() {

        // Remove the current data set definition.
        removeDataSetDef();

        // Register new data set definition.
        registerDataSetDef();

        // Update preview table.
        showPreviewTableEditionView();
    }

    private void removeDataSetDef() {
       removeDataSetDef(dataSetDef);
    }

    private void removeDataSetDef(final DataSetDef def) {
        if (def != null) {
            final DataSetClientServices clientServices = DataSetClientServices.get();
            clientServices.removeDataSetDef(def.getUUID());
        }
    }

    private void registerDataSetDef() {
        if (dataSetDef != null) {
            // Register the data set in backend as non public.
            final DataSetClientServices clientServices = DataSetClientServices.get();
            clientServices.registerDataSetDef(dataSetDef);
        }
    }

    

    private void showHomeView() {
        DataSetClientServices.get().getRemoteSharedDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(List<DataSetDef> dataSetDefs) {
                final int i = dataSetDefs != null ? dataSetDefs.size() : 0;
                view.showHomeView(i, newDataSetHandler);
                isHomeViewVisible = true;
            }
        });
    }
    
    private void showProviderSelectionView() {
        // Show provider selection widget.
        view.showProviderSelectionView();
        isHomeViewVisible = false;
    }
    
    private void showBasicAttributesEditionView() {
        view.showBasicAttributesEditionView();
        isHomeViewVisible = false;
    }
    
    private void showProviderSpecificAttributesEditionView() {
        isHomeViewVisible = false;
        switch (dataSetDef.getProvider()) {
            case SQL:
                view.showSQLAttributesEditorView(testButtonHandler);
                break;
            case CSV:
                view.showCSVAttributesEditorView(testButtonHandler);
                break;
            case BEAN:
                view.showBeanAttributesEditorView(testButtonHandler);
                break;
            case ELASTICSEARCH:
                view.showELAttributesEditorView(testButtonHandler);
                break;
        }
    }
    
    
    private void showColumnsEditorView(final DataSet dataSet) {
        isHomeViewVisible = false;
        view.showColumnsEditorView(this.columns, dataSet, columnsChangedEventHandler);
    }

    private void showFilterEditorView(final DataSet dataSet) {
        isHomeViewVisible = false;
        view.showFilterEditionView(dataSet, filterListener);
    }

    private void showPreviewTableEditionView() {
        isHomeViewVisible = false;
        registerDataSetDef();
        
        // Show table preview preview.
        view.showPreviewTableEditionView(tablePreviewListener);
    }
    
    private void showAdvancedAttributesEditionView() {
        isHomeViewVisible = false;
        view.showAdvancedAttributesEditionView();
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
            removeDataSetDef();
            clear();
        }
    };
    private final ClickHandler newDataSetHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            newDataSet(ClientUUIDGenerator.get().newUuid());
        }
    };
    
    private final DataSetColumnsEditor.ColumnsChangedEventHandler columnsChangedEventHandler = new DataSetColumnsEditor.ColumnsChangedEventHandler() {
        @Override
        public void onColumnsChanged(DataSetColumnsEditor.ColumnsChangedEvent event) {
            DataSetEditor.this.dataSetDef.getDataSet().setColumns(event.getColumns());
            update();
        }
    };
    
    private final DataSetFilterEditor.Listener filterListener = new DataSetFilterEditor.Listener() {
        @Override
        public void filterChanged(DataSetFilter filter) {
            DataSetEditor.this.dataSetDef.setDataSetFilter(filter);
            update();
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
                final boolean isNew = DataSetEditor.this.columns == null;
                
                if (dataSet != null) {
                    // Register data set on client.
                    ClientDataSetManager.get().registerDataSet(dataSet);
                    
                    if (isNew) {

                        // Original columns.
                        DataSetEditor.this.columns = new LinkedList<DataColumn>(dataSet.getColumns());
                        DataSetEditor.this.dataSetDef.getDataSet().setColumns(DataSetEditor.this.columns);
                        
                        // Show initial filter and columns edition view.
                        showColumnsEditorView(dataSet);
                        showFilterEditorView(dataSet);
                    }
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
        public void onFilterEnabled(Displayer displayer, DataSetGroup groupOp) {
            
        }

        @Override
        public void onFilterReset(Displayer displayer, List<DataSetGroup> groupOps) {

        }

    };
    
    private void clear() {
        this.dataSetDef = null;
        this.columns = null;
        this.edit = null;
        view.clear();
        showHomeView();
    }
    
    private boolean isValid(Set violations) {
        return violations.isEmpty();
    }

    // Be aware of data set lifecycle events

    private void onDataSetDefRegisteredEvent(@Observes DataSetDefRegisteredEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getDataSetDef();
        if (isHomeViewVisible && def != null && def.isPublic()) {
            // Reload home view with new data set count value.
            this.showHomeView();
        }
    }

    private void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        checkNotNull("event", event);

        if(isHomeViewVisible) {
            // Reload home view with new data set count value.
            this.showHomeView();
        }
    }
    
    // TODO: Display message to user.
    private void error(String message) {
        GWT.log(message);
    }

    private void log(Set<ConstraintViolation<? extends DataSetDef>>... violations) {
        if (true) return;
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
            if (dataSetDef instanceof ElasticSearchDataSetDef) {
                GWT.log("ElasticSearchDataSetDef server URL: " + ((ElasticSearchDataSetDef)dataSetDef).getServerURL());
                GWT.log("ElasticSearchDataSetDef cluster name: " + ((ElasticSearchDataSetDef)dataSetDef).getClusterName());
                String[] _index = ((ElasticSearchDataSetDef)dataSetDef).getIndex();
                String[] _type  = ((ElasticSearchDataSetDef)dataSetDef).getType();
                GWT.log("ElasticSearchDataSetDef index: " + _index);
                GWT.log("ElasticSearchDataSetDef type: " + _type);
                if (_index != null && _index.length > 0 ) GWT.log("ElasticSearchDataSetDef index[0]: " + _index[0]);
                if (_type != null && _type.length > 0 ) GWT.log("ElasticSearchDataSetDef type[0]: " + _type[0]);
            }
            if (dataSetDef instanceof BeanDataSetDef) {
                GWT.log("BeanDataSetDef generator class: " + ((BeanDataSetDef)dataSetDef).getGeneratorClass());

                Map<String, String> params = ((BeanDataSetDef)dataSetDef).getParamaterMap();
                if (params != null && !params.isEmpty()) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        GWT.log("BeanDataSetDef parameter - key: " + entry.getKey() + " / value: " + entry.getValue());
                    }
                }
            }
        }
    }

}
