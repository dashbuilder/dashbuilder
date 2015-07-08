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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.DataSetDefEditWorkflow;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetColumnsEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.SaveDataSetEvent;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.UpdateDataSetEvent;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.*;
import org.dashbuilder.dataset.backend.EditDataSetDef;
import org.dashbuilder.dataset.client.*;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.TableDisplayerSettingsBuilder;
import org.dashbuilder.displayer.client.AbstractDisplayerListener;
import org.dashbuilder.displayer.client.DataSetHandlerImpl;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;
import org.dashbuilder.displayer.impl.TableDisplayerSettingsBuilderImpl;
import org.dashbuilder.renderer.client.DefaultRenderer;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import java.util.*;

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
 *     <li>Data set columns and initial filter edition.</li>     
 *     <li>Advanced data set attributes edition.</li>
 * </ul> 
 *
 * <p>This editor a three step workflow:</p>
 * <ul>
 *     <li>STEP 1 - Provider selection (Only if creating new data set).</li>
 *     <li>STEP 2 - Basic data set attributes & Provider specific attributes.</li>     
 *     <li>STEP 3 - Data set columns and initial filter edition & Advanced data set attributes edition.</li>
 * </ul> 

 *  
 * @since 0.3.0 
 */
@Dependent
public class DataSetEditor implements IsWidget {

    private static final int PREVIEW_TABLE_PAGE_SIZE = 6; // Scroll is not visible using this size.
    final DataSetDefEditWorkflow workflow = new DataSetDefEditWorkflow();
    
    public interface View extends IsWidget, HasHandlers {
        View edit(final DataSetDef dataSetDef, final DataSetDefEditWorkflow workflow);
        Set getViolations();
        View setEditMode(final boolean editMode);
        View showProviderSelectionView();
        View showBasicAttributesEditionView(final String uuid);
        View showSQLAttributesEditorView();
        View showBeanAttributesEditorView();
        View showCSVAttributesEditorView(final FormPanel.SubmitCompleteHandler submitCompleteHandler);
        View showELAttributesEditorView();
        View showPreviewTableEditionView(final Displayer tableDisplayer);
        View showColumnsEditorView(final List<DataColumnDef> columns, final DataSet dataSet, final DataSetColumnsEditor.ColumnsChangedEventHandler columnsChangedEventHandler);
        View showFilterEditionView(final DataSet dataSet, final DataSetFilterEditor.Listener filterListener);
        View showAdvancedAttributesEditionView();
        View showTestButton(final String title, final String helpText, final ClickHandler testHandler);
        View hideTestButton();
        View showNextButton(final String title, final String helpText, final ButtonType type, final ClickHandler nextHandler);
        View enableNextButton(final boolean enabled);
        View showCancelButton(final ClickHandler cancelHandler);
        View hideCancelButton();
        HandlerRegistration addConfigurationTabClickHandler(final ClickHandler handler);
        HandlerRegistration addPreviewTabClickHandler(final ClickHandler handler);
        HandlerRegistration addAdvancedConfigurationTabClickHandler(final ClickHandler handler);
        View onSave();
        View showLoadingView();
        View hideLoadingView();
        View showError(final String message, final String cause);
        View clear();
    }
    
    private enum WorkflowView {
        PROVIDER_SELECTION, DATA_CONF, PREVIEW_AND_ADVANCED
    }

    @Inject
    private Event<SaveDataSetEvent> saveDataSetEvent;

    @Inject
    private Event<UpdateDataSetEvent> updateDataSetEvent;
    
    protected final View view = new DataSetEditorView();

    protected DataSetClientServices clientServices;
    protected DataSetDef dataSetDef;
    private String originalUUID;
    protected List<DataColumnDef> originalColumns;
    protected boolean updateColumnsView = false;
    protected Displayer tableDisplayer;
    protected WorkflowView currentWfView;
    protected boolean editMode = false;
    protected DataSetDefValidationCallback validationCallback = null;

    @Inject
    public DataSetEditor(DataSetClientServices clientServices) {
        this.clientServices = clientServices;
        init();
    }

    private void init() {
        // Init view handlers.
        view.addConfigurationTabClickHandler(configurationTabClickHandler);
        view.addPreviewTabClickHandler(previewTabClickHandler);
        view.addAdvancedConfigurationTabClickHandler(advancedConfigurationTabClickHandler);
    }
    
    public DataSetEditor setWidth(final String w) {
        ((DataSetEditorView)view).setWidth(w);
        return this;
    }

    public DataSetEditor newDataSetDef() {
        
        clear();

        // Clear any old status
        dataSetDef = new DataSetDef();
        originalUUID = null;
        originalColumns = null;
        updateColumnsView = true;
        view.setEditMode(editMode = false);

        // Restart workflow.
        edit();
        
        // Build provider selection view.
        currentWfView = WorkflowView.PROVIDER_SELECTION;
        showProviderSelectionView();

        // Next button.
        showNextButton();
        hideCancelButton();
                
        return this;
    }

    public void editDataSetDef(final EditDataSetDef editDataSetDef) {

        clear();

        dataSetDef = editDataSetDef.getDefinition();
        originalUUID = editDataSetDef.getUuid();
        originalColumns = editDataSetDef.getColumns();
        updateColumnsView = true;

        if (dataSetDef == null) {
            showError(DataSetEditorConstants.INSTANCE.defNotFound());
            return;
        }

        // Update the screens, displayers & restart the workflow.
        edit();
        view.setEditMode(editMode = true);
        showBasicAttributesEditionView();
        showProviderSpecificAttributesEditionView(true);
        hideTestButton();
        updateTableDisplayer();
    }

    private class DataSetHandlerForEdit extends DataSetHandlerImpl {

        private DataSetDef defEdit;

        public DataSetHandlerForEdit(DataSetLookup lookup, DataSetDef defEdit) {
            super(lookup);
            this.defEdit = defEdit;
        }

        @Override
        public void lookupDataSet(final DataSetReadyCallback callback) throws Exception {
            lookupCurrent.setTestMode(true);
            clientServices.lookupDataSet(defEdit, lookupCurrent, new DataSetReadyCallback() {
                public void callback(DataSet dataSet) {
                    lastLookedUpDataSet = dataSet;
                    callback.callback(dataSet);
                }

                public void notFound() {
                    callback.notFound();
                }

                @Override
                public boolean onError(final ClientRuntimeError error) {
                    return callback.onError(error);
                }
            });
        }
    }
    
    private void updateTableDisplayer() {
        if (dataSetDef != null) {

            // Build the displayer, so perform the lookup.
            TableDisplayerSettingsBuilder<TableDisplayerSettingsBuilderImpl> tableDisplayerSettingsBuilder = DisplayerSettingsFactory.newTableSettings()
                    .dataset(dataSetDef.getUUID())
                    .renderer(DefaultRenderer.UUID)
                    .titleVisible(false)
                    .tablePageSize(PREVIEW_TABLE_PAGE_SIZE)
                    .tableOrderEnabled(true)
                    .filterOn(true, false, false);

            final Collection<DataColumnDef> columns =  dataSetDef.getColumns();
            if (columns != null && !columns.isEmpty()) {
                for (final DataColumnDef column : columns) {
                    tableDisplayerSettingsBuilder.column(column.getId());
                }
            }

            DataSetDef editCloneWithoutCacheSettings = dataSetDef.clone();
            editCloneWithoutCacheSettings.setCacheEnabled(false);

            DisplayerSettings settings = tableDisplayerSettingsBuilder.buildSettings();
            tableDisplayer = DisplayerHelper.lookupDisplayer(settings);
            tableDisplayer.setDataSetHandler(new DataSetHandlerForEdit(settings.getDataSetLookup(), editCloneWithoutCacheSettings));
            tableDisplayer.addListener(tablePreviewListener);

            // Wait for displayer listener callbacks.
            if (DataSetEditor.this.tableDisplayer != null) {
                DataSetEditor.this.tableDisplayer.draw();
            }

            // Show basic views and the loading screen while performing the backend service call.
            view.showLoadingView();
        }
    }

    private final RemoteCallback<DataSetDef> createBrandNewDataSetDefCallback  = new RemoteCallback<DataSetDef>() {
        @Override
        public void callback(DataSetDef dataSetDef) {
            DataSetEditor.this.dataSetDef = dataSetDef;

            // Restart workflow.
            edit();

            // Build basic attributes view.
            currentWfView = WorkflowView.DATA_CONF;
            showBasicAttributesEditionView();
            showCancelButton();
            showTestButton();
            showProviderSpecificAttributesEditionView(false);
        }
    };

    private final ClickHandler providerScreenNextButtonHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
            // Save basic attributes (name and uuid) and provider type attribute.
            // Check if exist validation violations.
            final Set<ConstraintViolation<? extends DataSetDef>> violations = save();
            if (isValid(violations)) {
                try {
                    clientServices.newDataSet(dataSetDef.getProvider(), createBrandNewDataSetDefCallback);
                } catch (Exception e) {
                    showError(e);
                }
            }
        }
    };

    private final ClickHandler testButtonHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
            // Save basic attributes (name and uuid) and provider type attribute.
            // Check if exist validation violations.
            final Set violations = save();
            if (isValid(violations)) {
                // Reset columns and filter configuration.
                dataSetDef.setAllColumnsEnabled(true);
                dataSetDef.setColumns(null);
                dataSetDef.setDataSetFilter(null);
                originalColumns = null;
                updateColumnsView = true;

                // Update the preview table.
                updateTableDisplayer();
            }
        }
    };

    private final ClickHandler configurationTabClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            showTestButton();
        }
    };

    private final ClickHandler previewTabClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            hideTestButton();
            view.enableNextButton(!editMode);
        }
    };

    private final ClickHandler advancedConfigurationTabClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            hideTestButton();
            view.enableNextButton(!editMode);
        }
    };

    private final ClickHandler saveButtonHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
            checkValid(new DataSetDefValidationCallback() {
                @Override
                public void valid(DataSetDef def, DataSet dataSet) {
                    onSave();
                }
                @Override
                public void invalid(ClientRuntimeError error) {
                    showError(error);
                }
            });
        }
    };

    public void checkValid(DataSetDefValidationCallback callback) {
        final Set violations = save();
        if (!isValid(violations)) {
            callback.invalid(null);
        } else {
            validationCallback = callback;
            updateTableDisplayer();
        }
    }

    private boolean isValid(Set violations) {
        return violations.isEmpty();
    }

    private void onSave() {

        dataSetDef.setPublic(true);
        dataSetDef.setAllColumnsEnabled(false);

        
        if (originalUUID == null) {
            // Is saving a new data set.
            saveDataSetEvent.fire(new SaveDataSetEvent(this, dataSetDef));
        } else {
            // Is updating an existing data set.
            updateDataSetEvent.fire(new UpdateDataSetEvent(this, originalUUID, dataSetDef));
        }
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void showError(final Throwable e) {
        showError(new ClientRuntimeError(e));
    }

    public void showError(final ClientRuntimeError error) {
        view.showError(error.getMessage(), error.getCause());
    }

    public void showError(final String message) {
        view.showError(message, null);
    }

    private void showProviderSelectionView() {
        // Show provider selection widget.
        view.showProviderSelectionView();
    }
    
    private void showBasicAttributesEditionView() {
        final String _uuid = dataSetDef.getUUID();
        view.showBasicAttributesEditionView(_uuid);
    }
    
    private void showProviderSpecificAttributesEditionView(final boolean isEdit) {
        switch (dataSetDef.getProvider()) {
            case SQL:
                view.showSQLAttributesEditorView();
                break;
            case CSV:
                view.showCSVAttributesEditorView(submitCompleteHandler);
                break;
            case BEAN:
                view.showBeanAttributesEditorView();
                break;
            case ELASTICSEARCH:
                view.showELAttributesEditorView();
                break;
        }
    }
    
    private final FormPanel.SubmitCompleteHandler submitCompleteHandler = new FormPanel.SubmitCompleteHandler() {
        @Override
        public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
            // TODO: Check successful result.
            GWT.log("DataSetEditor#submitCompleteHandler: " + event.getResults());
        }
    };
    
    private void showColumnsEditorView(final DataSet dataSet) {
        view.showColumnsEditorView(this.originalColumns, dataSet, columnsChangedEventHandler);
        this.updateColumnsView = false;
    }

    private void showFilterEditorView(final DataSet dataSet) {
        view.showFilterEditionView(dataSet, filterListener);
    }

    private void showPreviewTableEditionView() {
        
        // Show table preview preview.
        view.showPreviewTableEditionView(tableDisplayer);
    }
    
    private void showAdvancedAttributesEditionView() {
        view.showAdvancedAttributesEditionView();
    }
    
    private View edit() {
        return view.edit(dataSetDef, workflow);
    }
    
    private Set save() {
        workflow.save();
        view.onSave();
        return view.getViolations();
    }
    
    private final ClickHandler cancelHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            newDataSetDef();
        }
    };

    private final DataSetColumnsEditor.ColumnsChangedEventHandler columnsChangedEventHandler = new DataSetColumnsEditor.ColumnsChangedEventHandler() {
        @Override
        public void onColumnsChanged(DataSetColumnsEditor.ColumnsChangedEvent event) {
            updateDataSetDefColumns(event.getColumns());
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
    
    // Saves columns and update "allColumns" flag for types that support it.
    private void updateDataSetDefColumns(final List<DataColumnDef> columns) {
        dataSetDef.setAllColumnsEnabled(false);
        dataSetDef.setColumns(columns);
    }
    
    /**
     * <p>When creating the table preview screen, this listener waits for data set available and then performs other operations.</p> 
     */
    private final DisplayerListener tablePreviewListener = new AbstractDisplayerListener() {
        
        @Override
        public void onDraw(Displayer displayer) {

            if (displayer != null) {
                final DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
                
                if (dataSet != null) {

                    currentWfView = WorkflowView.PREVIEW_AND_ADVANCED;

                    // If creating a new data set, its UUID for the client side instance is not present yet, as it's generated on backend side.
                    if (dataSetDef.getUUID() == null) {
                        dataSetDef.setUUID(dataSet.getUUID());
                    }

                    // If creating a data set, use the columns information for first dataset lookup result as the original columns,
                    // If editing an existing data set, this condition evaluates to false, as originalColumns have been provided by the backend call to prepareEdit., 
                    final boolean isFillColumns = DataSetEditor.this.originalColumns == null;
                    if (isFillColumns) {
                        final List<DataColumn> dataSetColumns = dataSet.getColumns();
                        if (dataSetColumns != null) {
                            final List<DataColumnDef> cDefs = new LinkedList<DataColumnDef>();
                            for (final DataColumn column : dataSetColumns) {
                                final DataColumnDef cDef = new DataColumnDef(column.getId(), column.getColumnType());
                                cDefs.add(cDef);
                            }
                            DataSetEditor.this.originalColumns = new LinkedList<DataColumnDef>(cDefs);
                            updateDataSetDefColumns(cDefs);
                        }
                    }

                    // Build views.
                    showBasicAttributesEditionView();

                    // Build advanced view.
                    showAdvancedAttributesEditionView();

                    // Reload table preview.
                    showPreviewTableEditionView();

                    // Show initial filter and columns edition views only if the current lookup has been updated. 
                    // If not, do not refresh columns and filter views.
                    if (updateColumnsView) {
                        showColumnsEditorView(dataSet);
                        showFilterEditorView(dataSet);
                    }
                    // Enable the right buttons
                    hideTestButton();
                    showSaveButton();

                    // Call the validation callback if any
                    if (validationCallback != null) {
                        validationCallback.valid(dataSetDef, dataSet);
                        validationCallback = null;
                    }
                }
            }
        }

        @Override
        public void onError(Displayer displayer, ClientRuntimeError error) {
            showError(error);
            if (validationCallback != null) {
                validationCallback.invalid(error);
                validationCallback = null;
            }
        }
    };

    private void showNextButton() {
        view.enableNextButton(true);
        view.showNextButton(DataSetEditorConstants.INSTANCE.next(),
                DataSetEditorConstants.INSTANCE.next_description(),
                ButtonType.PRIMARY,
                providerScreenNextButtonHandler);
    }

    private void showSaveButton() {
        view.enableNextButton(!editMode);
        if (!editMode) {
            view.showNextButton(DataSetEditorConstants.INSTANCE.save(),
                    DataSetEditorConstants.INSTANCE.save_description(),
                    ButtonType.SUCCESS,
                    saveButtonHandler);
        }
    }

    private void showTestButton() {
        view.enableNextButton(false);
        if (editMode) {
            view.showTestButton(DataSetEditorConstants.INSTANCE.test(), DataSetEditorConstants.INSTANCE.updateTest_description(), testButtonHandler);
        } else {
            view.showTestButton(DataSetEditorConstants.INSTANCE.test(), DataSetEditorConstants.INSTANCE.test_description(), testButtonHandler);
        }
    }

    private void showCancelButton() {
        view.showCancelButton(cancelHandler);
    }

    private void hideTestButton() {
        view.hideTestButton();
    }
    
    private void hideCancelButton() {
        view.hideCancelButton();
    }

    private void clear() {
        this.dataSetDef = null;
        this.originalUUID = null;
        this.originalColumns = null;
        this.updateColumnsView = false;
        this.tableDisplayer = null;
        view.clear();
    }
}
