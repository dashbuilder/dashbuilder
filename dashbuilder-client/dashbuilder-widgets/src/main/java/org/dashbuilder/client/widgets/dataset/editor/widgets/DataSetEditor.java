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
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.client.uuid.ClientUUIDGenerator;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.TableDisplayerSettingsBuilder;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;
import org.dashbuilder.displayer.impl.TableDisplayerSettingsBuilderImpl;
import org.dashbuilder.renderer.client.DefaultRenderer;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.validation.ConstraintViolation;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 *     <li>Columns editor view - @see <code>org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetColumnsEditor</code></li>
 *     <li>Initial filter editor - @see <code>org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor</code></li>
 * </ul>
 * 
 * <p>This editor provides 4 edition screens:</p>
 * <ul>
 *     <li>Provider selection (Only if creating new data set).</li>
 *     <li>Basic data set attributes & Provider specific attributes.</li>     
 *     <li>Basic data set attributes & Data set columns and initial filter edition.</li>     
 *     <li>Basic data set attributes & Advanced data set attributes edition.</li>
 * </ul> 
 *  
 * @since 0.3.0 
 */
@Dependent
public class DataSetEditor implements IsWidget {

    private static final String EDIT_SUFFIX = "_edit";
    final DataSetDefEditWorkflow workflow = new DataSetDefEditWorkflow();
    
    public interface View extends IsWidget, HasHandlers {
        View edit(final DataSetDef dataSetDef, final DataSetDefEditWorkflow workflow);
        Set getViolations();
        View setEditMode(final boolean editMode);
        View showHomeView(final int dsetCount, final ClickHandler newDataSetHandler);
        View showProviderSelectionView();
        View showBasicAttributesEditionView(final String uuid);
        View showSQLAttributesEditorView(ClickHandler testHandler);
        View showBeanAttributesEditorView(ClickHandler testHandler);
        View showCSVAttributesEditorView(ClickHandler testHandler);
        View showELAttributesEditorView(ClickHandler testHandler);
        View showPreviewTableEditionView(final Displayer tableDisplayer);
        View showColumnsEditorView(final List<DataColumn> columns, final DataSet dataSet, final DataSetColumnsEditor.ColumnsChangedEventHandler columnsChangedEventHandler);
        View showFilterEditionView(final DataSet dataSet, final DataSetFilterEditor.Listener filterListener);
        View showAdvancedAttributesEditionView();
        View showNextButton(String title, String helpText, ClickHandler nextHandler);
        View showCancelButton(ClickHandler cancelHandler);
        View onSave();
        View showLoadingView();
        View showError(final String type, final String message, final String cause);
        View clear();
    }
    
    private enum WorkflowView {
        HOME, PROVIDER_SELECTION, DATA_CONF, PREVIEW, ADVANCED        
    }

    final View view = new DataSetEditorView();

    private DataSetDef dataSetDef;
    private List<DataColumn> columns;
    private DataSetDef edit;
    private Displayer tableDisplayer;
    private WorkflowView currentWfView;
    
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
            showError("DataSetEditor#newDataSet - No UUID specified.");
            return this;
        }

        clear();
        
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
    
    public DataSetEditor editDataSet(final String uuid) {

        if (uuid == null || uuid.trim().length() == 0) {
            showError("DataSetEditor#editDataSet - No UUID specified.");
            return this;
        }
        
        clear();

        try {
            DataSetClientServices.get().fetchMetadata(uuid, new DataSetMetadataCallback() {
                @Override
                public void callback(DataSetMetadata metatada)
                {
                    final DataSetDef def = metatada.getDefinition();
                    final String uuid = def.getUUID();
                    
                    // Clone the definition in order to edit the cloned copy.
                    DataSetEditor.this.dataSetDef = def.clone();
                    final String editionUUID = getEditionUUID(def);
                    DataSetEditor.this.dataSetDef.setUUID(editionUUID);
                    DataSetEditor.this.dataSetDef.setPublic(false);
    
                    edit = def;
                    view.setEditMode(true);
                    
                    // Update the displayer & Restart workflow.
                    showBasicAttributesEditionView();
                    showProviderSpecificAttributesEditionView();
                    updateTableDisplayer();
    
                }
    
                @Override
                public void notFound() {
                    showError("Data set definition with uuid [" + uuid + "] not found.");
                }
    
                @Override
                public boolean onError(DataSetClientServiceError error) {
                    showError(error);
                    return false;
                }
            });
        } catch (Exception e) {
            showError(e.getMessage());
        }
        return this;
    }
    
    private DataSetDef createDataSetDef(final String uuid, final DataSetProviderType type) {
        DataSetDef result = new DataSetDef();
        if (type != null) result = DataSetProviderType.createDataSetDef(dataSetDef.getProvider());
        result.setUUID(uuid);
        result.setPublic(false);
        return result;
    }
    
    private String getEditionUUID(final DataSetDef def) {
        String uuid = null;
        if (def != null) {
            final String _uuid = def.getUUID();
            return _uuid + EDIT_SUFFIX;            
        }
        
        return uuid;
    }
    
    private void updateTableDisplayer() {
        if (dataSetDef != null) {
            // Register changes or new definition on backend in order to use the table displayer 
            // for previewing data on next screen.
            if (tableDisplayer == null) registerDataSetDef();
            else updateDataSetDef();

            // Build the displayer, so perform the lookup.
            TableDisplayerSettingsBuilder<TableDisplayerSettingsBuilderImpl> tableDisplayerSettingsBuilder = DisplayerSettingsFactory.newTableSettings()
                    .dataset(dataSetDef.getUUID())
                    .renderer(DefaultRenderer.UUID)
                    .titleVisible(false)
                    .tablePageSize(10)
                    .tableOrderEnabled(true)
                    .filterOn(true, false, false);

            List<DataColumn> columns =  dataSetDef.getDataSet().getColumns();
            if (columns != null && !columns.isEmpty()) {
                for (DataColumn column : columns) {
                    tableDisplayerSettingsBuilder.column(column.getId());
                }
            }

            DisplayerSettings settings = tableDisplayerSettingsBuilder.buildSettings();
            tableDisplayer  =  DisplayerHelper.lookupDisplayer(settings);
            tableDisplayer .addListener(tablePreviewListener);

            // Wait for displayer listener callbacks.
            if (DataSetEditor.this.tableDisplayer != null) DataSetEditor.this.tableDisplayer.draw();
            
            // Show basic views and the loading screen while performing the backend service call.
            edit();
            view.showLoadingView();
        }
    }
    
    private final ClickHandler providerScreenNextButtonHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
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
        public void onClick(final ClickEvent event) {
            // Save basic attributes (name and uuid) and provider type attribute.
            // Check if exist validation violations.
            final Set violations = save();
            if (isValid(violations)) {
                updateTableDisplayer();

            }
            log(violations, violations);
        }
    };

    private final ClickHandler advancedAttrsButtonHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
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
        public void onClick(final ClickEvent event) {
            final Set violations = save();
            if (isValid(violations)) {
                try {
                    // Valid
                    persist();

                } catch (Exception e) {
                    showError("Error persisting data set defintion with uuid [" + dataSetDef.getUUID() + "]. Message: " + e.getMessage());
                } finally {
                    clear();
                    showHomeView();
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
    
    private void updateDataSetDef() {

        // Remove the current data set definition.
        removeDataSetDef();

        // Register new data set definition.
        registerDataSetDef();

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
                currentWfView = WorkflowView.HOME;
            }
        });
    }

    private void showError(final DataSetClientServiceError error) {
        final String type = error.getThrowable() != null ? error.getThrowable().getClass().getName() : null;
        final String message = error.getThrowable() != null ? error.getThrowable().getMessage() : error.getMessage().toString();
        final String cause = error.getThrowable() != null && error.getThrowable().getCause() != null ? error.getThrowable().getCause().getMessage() : null;
        showError(type, message, cause);
    }

    private void showError(final String message) {
        showError(null, message, null);
    }
    
    private void showError(final String type, final String message, final String cause) {
        if (type != null) GWT.log("Error type: " + type);
        if (message != null) GWT.log("Error message: " + message);
        if (cause != null) GWT.log("Error cause: " + cause);
        view.showError(type, message, cause);
    }
    
    private void showProviderSelectionView() {
        // Show provider selection widget.
        view.showProviderSelectionView();
        currentWfView = WorkflowView.PROVIDER_SELECTION;
    }
    
    private void showBasicAttributesEditionView() {
        final String _uuid = edit != null ? edit.getUUID() : dataSetDef.getUUID();
        view.showBasicAttributesEditionView(_uuid);
        currentWfView = WorkflowView.DATA_CONF;
    }
    
    private void showProviderSpecificAttributesEditionView() {
        currentWfView = WorkflowView.DATA_CONF;
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
        currentWfView = WorkflowView.PREVIEW;
        view.showColumnsEditorView(this.columns, dataSet, columnsChangedEventHandler);
    }

    private void showFilterEditorView(final DataSet dataSet) {
        currentWfView = WorkflowView.PREVIEW;
        view.showFilterEditionView(dataSet, filterListener);
    }

    private void showPreviewTableEditionView() {
        currentWfView = WorkflowView.PREVIEW;
        // Show table preview preview.
        view.showPreviewTableEditionView(tableDisplayer);
    }
    
    private void showAdvancedAttributesEditionView() {
        currentWfView = WorkflowView.ADVANCED;
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
            showHomeView();
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
            updateTableDisplayer();
        }
    };
    
    private final DataSetFilterEditor.Listener filterListener = new DataSetFilterEditor.Listener() {
        @Override
        public void filterChanged(DataSetFilter filter) {
            DataSetEditor.this.dataSetDef.setDataSetFilter(filter);
            updateTableDisplayer();
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
                
                if (dataSet != null) {
                    // Rezgister data set on client.
                    ClientDataSetManager.get().registerDataSet(dataSet);
                    
                    // Original columns.
                    final boolean isEdit = edit != null;
                    final boolean mustEdit = DataSetEditor.this.columns == null; 
                    DataSetEditor.this.columns = new LinkedList<DataColumn>(dataSet.getColumns());
                    DataSetEditor.this.dataSetDef.getDataSet().setColumns(DataSetEditor.this.columns);

                    // Restart workflow.
                    if (mustEdit) edit();
                    
                    // Build views.
                    showBasicAttributesEditionView();
                    showProviderSpecificAttributesEditionView();
                    if (isEdit) showAdvancedAttributesEditionView();
                    showPreviewTableEditionView();
                    
                    // Show initial filter and columns edition view.
                    if (mustEdit) {
                        showColumnsEditorView(dataSet);
                        showFilterEditorView(dataSet);
                    }

                    if (isEdit) {
                        view.showNextButton(DataSetEditorConstants.INSTANCE.save(),
                                DataSetEditorConstants.INSTANCE.save_description(),
                                saveButtonHandler);
                    } else {
                        view.showNextButton(DataSetEditorConstants.INSTANCE.next(),
                                DataSetEditorConstants.INSTANCE.next_description(),
                                advancedAttrsButtonHandler);
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

        @Override
        public void onError(Displayer displayer, DataSetClientServiceError error) {
            showError(error);
        }

    };
    
    private void clear() {
        this.dataSetDef = null;
        this.columns = null;
        this.edit = null;
        this.tableDisplayer = null;
        view.clear();
    }
    
    private boolean isValid(Set violations) {
        return violations.isEmpty();
    }

    private boolean isHomeViewVisible() {
        return WorkflowView.HOME.equals(currentWfView);
    }
    
    // Be aware of data set lifecycle events

    private void onDataSetDefRegisteredEvent(@Observes DataSetDefRegisteredEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getDataSetDef();
        if (isHomeViewVisible() && def != null && def.isPublic()) {
            // Reload home view with new data set count value.
            this.showHomeView();
        }
    }

    private void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        checkNotNull("event", event);

        if(isHomeViewVisible()) {
            // Reload home view with new data set count value.
            this.showHomeView();
        }
    }
    
    // TODO: Remove, just for testing.
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
