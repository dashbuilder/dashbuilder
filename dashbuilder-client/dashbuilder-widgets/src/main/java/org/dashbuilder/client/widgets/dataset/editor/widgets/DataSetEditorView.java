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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.client.widgets.dataset.editor.DataSetDefEditWorkflow;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.*;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.bean.BeanDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.csv.CSVDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.elasticsearch.ELDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.sql.SQLDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetExportReadyCallback;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.TabPanel;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.widgets.common.client.common.BusyPopup;

import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Default view for DataSetEditor presenter.</p> 
 * @since 0.3.0 
 */
@Dependent
public class DataSetEditorView extends Composite implements DataSetEditor.View {

    private static final String LOADING_IMAGE_SIZE = "16px";
    private static final int LOADING_SCREEN_TIMEOUT = 60000;
    
    interface DataSetEditorViewBinder extends UiBinder<Widget, DataSetEditorView> {}
    private static DataSetEditorViewBinder uiBinder = GWT.create(DataSetEditorViewBinder.class);

    interface DataSetEditorViewStyle extends CssResource {
        String tabInnerPanel();
        String columnsFilterDisclosurePanelHeaderOpen();
        String with100pc();
    }

    @UiField
    DataSetEditorViewStyle style;
    
    @UiField
    FlowPanel mainPanel;

    @UiField
    FlowPanel titlePanel;
    
    @UiField
    Heading title;
    
    @UiField
    Modal errorPanel;
    
    @UiField
    Button errorPanelButton;
    
    @UiField
    HTML errorMessage;

    @UiField
    HTML errorCause;

    @UiField
    Row errorMessageRow;

    @UiField
    Row errorCauseRow;

    @UiField
    FlowPanel providerSelectionViewPanel;

    @UiField
    DataSetProviderTypeEditor dataSetProviderTypeEditor;

    @UiField
    TabPanel tabPanel;
    
    @UiField
    FlowPanel tabViewPanel;
    
    @UiField
    TabListItem configurationTabItem;

    @UiField
    TabListItem previewTabItem;

    @UiField
    TabListItem advancedConfigurationTabItem;

    @UiField
    TabPane configurationTabPane;

    @UiField
    TabPane previewTabPane;

    @UiField
    TabPane advancedConfigurationTabPane;

    @UiField
    FlowPanel dataConfigurationPanel;
    
    @UiField
    FlowPanel basicAttributesEditionViewPanel;

    @UiField
    DataSetBasicAttributesEditor dataSetBasicAttributesEditor;

    @UiField
    FlowPanel specificProviderAttributesPanel;
    
    @UiField
    FlowPanel sqlAttributesEditionViewPanel;

    @UiField
    SQLDataSetDefAttributesEditor sqlDataSetDefAttributesEditor;
    
    @UiField
    Button testButton;
    
    @UiField
    Popover testPopover;

    @UiField
    FlowPanel filterColumnsPreviewTablePanel;

    @UiField
    FlowPanel previewTableEditionViewPanel;

    @UiField
    DataSetPreviewEditor previewTableEditor;

    @UiField
    DisclosurePanel filterAndColumnsEditionDisclosurePanel;
    
    @UiField
    FlowPanel columnsFilterDisclosurePanelHeader;
    
    @UiField
    Icon columnsFilterDisclosurePanelButton;
    
    @UiField
    FlowPanel filterAndColumnsEditionViewPanel;

    @UiField
    TabPanel filterAndColumnsTabPanel;

    @UiField
    TabListItem columnsTabItem;

    @UiField
    TabListItem filterTabItem;

    @UiField
    TabPane columnsTabPane;

    @UiField
    TabPane filterTabPane;

    @UiField
    DataSetColumnsEditor columnsEditor;

    @UiField
    FlowPanel csvAttributesEditionViewPanel;

    @UiField
    CSVDataSetDefAttributesEditor csvDataSetDefAttributesEditor;

    @UiField
    FlowPanel beanAttributesEditionViewPanel;

    @UiField
    BeanDataSetDefAttributesEditor beanDataSetDefAttributesEditor;

    @UiField
    FlowPanel elAttributesEditionViewPanel;

    @UiField
    ELDataSetDefAttributesEditor elDataSetDefAttributesEditor;
    
    @UiField
    FlowPanel advancedAttributesEditionViewPanel;

    @UiField
    DataSetAdvancedAttributesEditor dataSetAdvancedAttributesEditor;

    @UiField
    HorizontalPanel buttonsPanel;

    @UiField
    Button cancelButton;

    @UiField
    Button nextButton;
    
    @UiField
    Popover nextPopover;

    private DataSetDef dataSetDef = null;
    private boolean isEditMode = true;
    private DataSetDefEditWorkflow workflow;
    
    private HandlerRegistration nextButtonHandlerRegistration = null;
    private HandlerRegistration cancelButtonHandlerRegistration = null;
    private HandlerRegistration testButtonHandlerRegistration = null;

    private HandlerRegistration newDatasetHandlerRegistration = null;
    private HandlerRegistration submitCompleteHandlerRegistration = null;
    private HandlerRegistration columnsChangeHandlerRegistration = null;
    

    private final DataSetExportReadyCallback exportReadyCallback = new DataSetExportReadyCallback() {
        @Override
        public void exportReady(Path exportFilePath) {
            final String u = DataSetClientServices.get().getDownloadFileUrl(exportFilePath);
            GWT.log("Export URL: " + u);
            Window.open(u,
                    "downloading",
                    "resizable=no,scrollbars=yes,status=no");
        }
        @Override
        public void onError(ClientRuntimeError error) {
            showError(error.getMessage(), error.getCause());
        }
    };
    
    public HandlerRegistration addConfigurationTabClickHandler(final ClickHandler handler) {
        return configurationTabItem.addClickHandler(handler);
    }

    public HandlerRegistration addPreviewTabClickHandler(final ClickHandler handler) {
        return previewTabItem.addClickHandler(handler);
    }

    public HandlerRegistration addAdvancedConfigurationTabClickHandler(final ClickHandler handler) {
        return advancedConfigurationTabItem.addClickHandler(handler);
    }

    public DataSetEditorView() {
        initWidget( uiBinder.createAndBindUi( this ) );

        configurationTabItem.setDataTargetWidget( configurationTabPane );
        previewTabItem.setDataTargetWidget( previewTabPane );
        advancedConfigurationTabItem.setDataTargetWidget( advancedConfigurationTabPane );
        columnsTabItem.setDataTargetWidget( columnsTabPane );
        filterTabItem.setDataTargetWidget( filterTabPane );

        // Configure handlers.
        filterAndColumnsEditionDisclosurePanel.addOpenHandler(openColumnsFilterPanelHandler);
        filterAndColumnsEditionDisclosurePanel.addCloseHandler(closeColumnsFilterPanelHandler);
        
        // Hide loading popup at startup.
        hideLoadingView();

        // Error panel button handler.
        errorPanelButton.addClickHandler(errorPanelButtonHandler);

        // Show home view by default.
        showEmptyView();
    }
    
    private final OpenHandler<DisclosurePanel> openColumnsFilterPanelHandler = new OpenHandler<DisclosurePanel>() {
        @Override
        public void onOpen(OpenEvent<DisclosurePanel> event) {
            columnsFilterDisclosurePanelHeader.setTitle(DataSetEditorConstants.INSTANCE.hideColumnsAndFilter());
            columnsFilterDisclosurePanelHeader.addStyleName(style.columnsFilterDisclosurePanelHeaderOpen());
            columnsFilterDisclosurePanelButton.setType(IconType.STEP_BACKWARD);
            columnsFilterDisclosurePanelButton.setTitle(DataSetEditorConstants.INSTANCE.hideColumnsAndFilter());
            previewTableEditionViewPanel.removeStyleName(style.with100pc());
        }
    };

    private final CloseHandler<DisclosurePanel> closeColumnsFilterPanelHandler = new CloseHandler<DisclosurePanel>() {
        @Override
        public void onClose(CloseEvent<DisclosurePanel> event) {
            columnsFilterDisclosurePanelHeader.setTitle(DataSetEditorConstants.INSTANCE.showColumnsAndFilter());
            columnsFilterDisclosurePanelHeader.removeStyleName(style.columnsFilterDisclosurePanelHeaderOpen());
            columnsFilterDisclosurePanelButton.setType(IconType.STEP_FORWARD);
            columnsFilterDisclosurePanelButton.setTitle(DataSetEditorConstants.INSTANCE.showColumnsAndFilter());
            previewTableEditionViewPanel.addStyleName(style.with100pc());
        }
    };
    
    private final ClickHandler errorPanelButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            hideError();
        }
    };

    @Override
    public DataSetEditor.View setEditMode(final boolean editMode) {
        this.isEditMode = editMode;
        return this;
    }

    private void showEmptyView() {
        clearView();
    }

    @Override
    public DataSetEditor.View edit(final DataSetDef dataSetDef, final DataSetDefEditWorkflow workflow) {
        this.dataSetDef = dataSetDef;
        this.workflow = workflow;

        // Reset current view.
        clearView();

        // Clear current workflow state.
        this.workflow.clear();
        
        // Set the definition to be edited in to sub-editors.
        setDataSetDefIntoEditor();
        
        return this;
    }


    @Override
    public DataSetEditor.View showProviderSelectionView() {
        workflow.edit(dataSetProviderTypeEditor, dataSetDef);

        // View title.
        showTitle();

        dataSetProviderTypeEditor.setEditMode(!isEditMode);
        providerSelectionViewPanel.setVisible(true);

        return this;
    }

    private boolean isProviderSelectionViewVisible() {
        return providerSelectionViewPanel.isVisible();
    }


    @Override
    public DataSetEditor.View showBasicAttributesEditionView(final String uuid) {
        workflow.edit(dataSetBasicAttributesEditor, dataSetDef);
        
        // The uuid of the data set, if is the cloned instance for editing, is not the original one.
        dataSetBasicAttributesEditor.setUUID(uuid);

        basicAttributesEditionViewPanel.setVisible(true);
        dataSetBasicAttributesEditor.setEditMode(true);
        showSpecificProviderAttrsEditionView();
        return this;
    }

    @Override
    public DataSetEditor.View showSQLAttributesEditorView() {
        workflow.edit(sqlDataSetDefAttributesEditor, (SQLDataSetDef) dataSetDef);
        sqlAttributesEditionViewPanel.setVisible(true);
        sqlDataSetDefAttributesEditor.setEditMode(true);
        showSpecificProviderAttrsEditionView();
        return this;
    }

    private boolean isSQLAttributesEditorViewVisible() {
        return sqlAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showBeanAttributesEditorView() {
        workflow.edit(beanDataSetDefAttributesEditor, (BeanDataSetDef) dataSetDef);
        beanAttributesEditionViewPanel.setVisible(true);
        beanDataSetDefAttributesEditor.setEditMode(true);
        showSpecificProviderAttrsEditionView();
        return this;
    }

    private boolean isBeanAttributesEditorViewVisible() {
        return beanAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showCSVAttributesEditorView(final FormPanel.SubmitCompleteHandler submitCompleteHandler) {
        workflow.edit(csvDataSetDefAttributesEditor, (CSVDataSetDef) dataSetDef);
        csvAttributesEditionViewPanel.setVisible(true);
        csvDataSetDefAttributesEditor.setEditMode(true);
        if (submitCompleteHandlerRegistration != null) submitCompleteHandlerRegistration.removeHandler();
        submitCompleteHandlerRegistration = csvDataSetDefAttributesEditor.addSubmitCompleteHandler(submitCompleteHandler);
        showSpecificProviderAttrsEditionView();
        return this;
    }

    private boolean isCSVAttributesEditorViewVisible() {
        return csvAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showELAttributesEditorView() {
        workflow.edit(elDataSetDefAttributesEditor, (ElasticSearchDataSetDef) dataSetDef);
        elAttributesEditionViewPanel.setVisible(true);
        elDataSetDefAttributesEditor.setEditMode(true);
        showSpecificProviderAttrsEditionView();
        return this;
    }

    private boolean isELAttributesEditorViewVisible() {
        return elAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showTestButton(final String title, final String helpText, final ClickHandler testHandler) {
        if (title != null) {
            testButton.setText(title);
            testPopover.setTitle(title);
            testPopover.setContent( helpText != null ? helpText : "");
            testPopover.reconfigure();
        }
        if (testButtonHandlerRegistration != null) testButtonHandlerRegistration.removeHandler();
        if (testHandler != null)
        {
            testButtonHandlerRegistration = testButton.addClickHandler(testHandler);
        }
        testButton.setVisible(true);
        buttonsPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View hideTestButton() {
        testButton.setVisible(false);
        return this;
    }

    @Override
    public DataSetEditor.View hideCancelButton() {
        cancelButton.setVisible(false);
        return this;
    }

    private void showSpecificProviderAttrsEditionView()
    {
        showTab(configurationTabItem);
        activeConfigurationTab();
        tabViewPanel.setVisible(true);
        dataConfigurationPanel.setVisible(true);
    }

    @Override
    public DataSetEditor.View showPreviewTableEditionView(final Displayer tableDisplayer) {
        hideLoadingView();

        // Configure tabs and visibility.
        previewTableEditor.setVisible(true);
        previewTableEditor.setEditMode(true);
        previewTableEditor.build(tableDisplayer);
        showTab(configurationTabItem);
        showTab(previewTabItem);
        previewTableEditionViewPanel.setVisible(true);
        filterColumnsPreviewTablePanel.setVisible(true);
        showFilterColumnsPreviewEditionView();
        return this;
    }

    private boolean isPreviewTableEditionViewVisible() {
        return previewTableEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showColumnsEditorView(final List<DataColumnDef> columns, final DataSet dataSet, final DataSetColumnsEditor.ColumnsChangedEventHandler columnsChangedEventHandler) {
        // Columns editor is not a data set editor component, just a widget to handle DataColumnEditor instances.
        // So not necessary to use the editor workflow this instance.

        hideLoadingView();

        // Data Set Columns editor.
        columnsEditor.setVisible(true);
        
        // Special condition - BEAN data provider type does not support column type changes.
        final boolean isBeanType = DataSetProviderType.BEAN.equals(dataSetDef.getProvider());
        columnsEditor.setEditMode(!isBeanType);
        columnsEditor.build(columns, dataSet, workflow);
        if (columnsChangeHandlerRegistration != null) columnsChangeHandlerRegistration.removeHandler();
        columnsChangeHandlerRegistration = columnsEditor.addColumnsChangeHandler(columnsChangedEventHandler);

        // Panels and tab visibility.
        filterAndColumnsEditionViewPanel.setVisible(true);
        filterAndColumnsTabPanel.setVisible(true);
        
        return this;
    }

    private boolean isColumnsEditorViewVisible() {
        return filterAndColumnsEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showFilterEditionView(final DataSet dataSet, final DataSetFilterEditor.Listener filterListener) {
        filterTabPane.clear();

        hideLoadingView();

        // Data Set Filter editor.
        final DataSetFilterEditor filterEditor = new DataSetFilterEditor();
        filterEditor.init(dataSet.getMetadata(), dataSetDef.getDataSetFilter(), filterListener);
        filterTabPane.add(filterEditor);

        // Panels and tab visibility.
        filterAndColumnsEditionViewPanel.setVisible(true);
        filterAndColumnsTabPanel.setVisible(true);
        
        return this;
    }

    private boolean isFilterEditorViewVisible() {
        return filterAndColumnsEditionViewPanel.isVisible();
    }

    private void showFilterColumnsPreviewEditionView()
    {
        tabViewPanel.setVisible(true);
        showTab(configurationTabItem);
        showTab(previewTabItem);
        activePreviewTab();
    }
    
    @Override
    public DataSetEditor.View showAdvancedAttributesEditionView() {
        workflow.edit(dataSetAdvancedAttributesEditor, dataSetDef);

        advancedAttributesEditionViewPanel.setVisible(true);
        dataSetAdvancedAttributesEditor.setEditMode(true);
        showTab(configurationTabItem);
        showTab(previewTabItem);
        showTab(advancedConfigurationTabItem);
        activeAdvancedConfigurationTab();
        tabViewPanel.setVisible(true);
        return this;
    }

    private boolean isAdvancedAttributesEditionViewVisible() {
        return advancedAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showNextButton(final String title, final String helpText, final ButtonType type, final ClickHandler nextHandler) {
        nextButton.setVisible(nextHandler != null);
        if (title != null) {
            nextButton.setText(title);
            nextPopover.setTitle( title );
            nextPopover.setContent( helpText != null ? helpText : "" );
            nextPopover.reconfigure();
        }
        ButtonType _type = type != null ? type : ButtonType.PRIMARY;
        nextButton.setType( _type );
        if (nextHandler != null) {
            removeNextButtonHandler();
            nextButtonHandlerRegistration = nextButton.addClickHandler(nextHandler);
        }
        buttonsPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View enableNextButton(boolean enabled) {
        nextButton.setVisible(enabled);
        return this;
    }

    @Override
    public DataSetEditor.View showCancelButton(final ClickHandler cancelHandler) {
        cancelButton.setVisible(cancelHandler!= null);
        if (cancelHandler != null) {
            removeCancelButtonHandler();
            cancelButtonHandlerRegistration = cancelButton.addClickHandler(cancelHandler);
        }
        buttonsPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View onSave() {

        // Check editor errors. If any, mark error in parent Tab.
        resetTabErrors();
        DataSetProviderType type = dataSetDef.getProvider();
        if (type != null) {
            switch (type) {
                case BEAN:
                    if (isBeanAttributesEditorViewVisible() && hasViolations(beanDataSetDefAttributesEditor.getViolations())) tabErrors(configurationTabItem);
                    break;
                case CSV:
                    if (isCSVAttributesEditorViewVisible() && hasViolations(csvDataSetDefAttributesEditor.getViolations())) tabErrors(configurationTabItem);
                    break;
                case SQL:
                    if (isSQLAttributesEditorViewVisible() && hasViolations(sqlDataSetDefAttributesEditor.getViolations())) tabErrors(configurationTabItem);
                    break;
                case ELASTICSEARCH:
                    if (isELAttributesEditorViewVisible()) {
                        // Save attributes not handled by editor framework.
                        elDataSetDefAttributesEditor.save();

                        // Check violations.
                        if (hasViolations(elDataSetDefAttributesEditor.getViolations())) tabErrors(configurationTabItem);
                    }
                    break;
            }
        }

        if (hasViolations(previewTableEditor.getViolations())) tabErrors(advancedConfigurationTabItem);
        if (hasViolations(dataSetAdvancedAttributesEditor.getViolations())) tabErrors(advancedConfigurationTabItem);
        if (hasViolations(columnsEditor.getViolations())) tabErrors(advancedConfigurationTabItem);

        return this;
    }

    @Override
    public DataSetEditor.View showLoadingView() {
        BusyPopup.showMessage(DataSetEditorConstants.INSTANCE.loading());
        return this;
    }

    public DataSetEditor.View hideLoadingView() {
        BusyPopup.close();
        return this;
    }

    @Override
    public DataSetEditor.View showError(final String message, final String cause) {
        if (message != null) GWT.log("Error message: " + message);
        if (cause != null) GWT.log("Error cause: " + cause);

        errorMessage.setText(message != null ? message : "");
        errorMessageRow.setVisible(message != null);
        errorCause.setText(cause != null ? cause : "");
        errorCauseRow.setVisible(cause != null);
        errorPanel.show();
        hideLoadingView();
        return this;
    }
    
    private void hideError() {
        errorPanel.hide();
    }

    public Set getViolations() {
        final Set violations = new LinkedHashSet<ConstraintViolation<? extends DataSetDef>>();
        if (dataSetProviderTypeEditor.getViolations() != null) violations.addAll((Collection) dataSetProviderTypeEditor.getViolations());
        if (dataSetBasicAttributesEditor.getViolations() != null) violations.addAll((Collection) dataSetBasicAttributesEditor.getViolations());
        if (beanDataSetDefAttributesEditor.getViolations() != null) violations.addAll((Collection) beanDataSetDefAttributesEditor.getViolations());
        if (csvDataSetDefAttributesEditor.getViolations() != null) violations.addAll((Collection) csvDataSetDefAttributesEditor.getViolations());
        if (sqlDataSetDefAttributesEditor.getViolations() != null) violations.addAll((Collection) sqlDataSetDefAttributesEditor.getViolations());
        if (elDataSetDefAttributesEditor.getViolations() != null) violations.addAll((Collection) elDataSetDefAttributesEditor.getViolations());
        if (dataSetAdvancedAttributesEditor.getViolations() != null) violations.addAll((Collection) dataSetAdvancedAttributesEditor.getViolations());
        if (previewTableEditor.getViolations() != null) violations.addAll((Collection) previewTableEditor.getViolations());
        if (columnsEditor.getViolations() != null) violations.addAll((Collection) columnsEditor.getViolations());
        return violations;
    }
    
    private boolean hasViolations(Iterable<ConstraintViolation<?>> violations) {
        return violations != null && violations.iterator().hasNext();
    }

    @Override
    public DataSetEditor.View clear() {
        clearView();
                
        // Clear editors.
        dataSetProviderTypeEditor.clear();
        dataSetBasicAttributesEditor.clear();
        beanDataSetDefAttributesEditor.clear();
        csvDataSetDefAttributesEditor.clear();
        sqlDataSetDefAttributesEditor.clear();
        elDataSetDefAttributesEditor.clear();
        columnsEditor.clear();
        previewTableEditor.clear();
        dataSetAdvancedAttributesEditor.clear();

        this.dataSetDef = null;
        this.workflow = null;
        return this;
    }

    private void activeConfigurationTab() {
        configurationTabItem.showTab();
    }
    
    private void activePreviewTab() {
        previewTabItem.showTab();
    }

    private void activeAdvancedConfigurationTab() {
        advancedConfigurationTabItem.showTab();
    }
    
    private void clearView() {
        titlePanel.setVisible(false);
        title.setVisible(false);
        providerSelectionViewPanel.setVisible(false);
        tabViewPanel.setVisible(false);
        hideTab(configurationTabItem);
        hideTab(previewTabItem);
        hideTab(advancedConfigurationTabItem);
        advancedAttributesEditionViewPanel.setVisible(false);
        sqlAttributesEditionViewPanel.setVisible(false);
        csvAttributesEditionViewPanel.setVisible(false);
        beanAttributesEditionViewPanel.setVisible(false);
        elAttributesEditionViewPanel.setVisible(false);
        previewTableEditionViewPanel.setVisible(false);
        dataConfigurationPanel.setVisible(false);
        filterColumnsPreviewTablePanel.setVisible(false);
        testButton.setVisible(false);
        nextButton.setVisible(false);
        cancelButton.setVisible(false);
        buttonsPanel.setVisible(false);
    }
    
    private void setDataSetDefIntoEditor() {
        dataSetProviderTypeEditor.set(dataSetDef);
        dataSetBasicAttributesEditor.set(dataSetDef);
        beanDataSetDefAttributesEditor.set(dataSetDef);
        csvDataSetDefAttributesEditor.set(dataSetDef);
        sqlDataSetDefAttributesEditor.set(dataSetDef);
        elDataSetDefAttributesEditor.set(dataSetDef);
        previewTableEditor.set(dataSetDef);
        dataSetAdvancedAttributesEditor.set(dataSetDef);
    }
    
    private void showTitle() {

        String _name = null;
        DataSetProviderType _provider = null;

        if (dataSetDef != null) {
            
            if (dataSetDef.getName() != null && dataSetDef.getName().length() > 0) {
                _name = dataSetDef.getName();
            }
            
            if (dataSetDef.getProvider() != null) {
                _provider =  dataSetDef.getProvider();
            }
            
            if (_name == null && _provider == null) {
                title.setText(DataSetEditorConstants.INSTANCE.newDataSet(""));
            } else if (_provider != null && _name == null) {
                title.setText(DataSetEditorConstants.INSTANCE.newDataSet(getProviderName(_provider)));
            } else if (_provider == null) {
                title.setText(_name);
            } else {
                title.setText(_name + " (" + getProviderName(_provider) + ")");
            }
        }
    }
    
    public static String getProviderName(DataSetProviderType type) {
        String s = null;
        switch (type) {
            case BEAN:
                s = DataSetEditorConstants.INSTANCE.bean();
                break;
            case CSV:
                s = DataSetEditorConstants.INSTANCE.csv();
                break;
            case SQL:
                s = DataSetEditorConstants.INSTANCE.sql();
                break;
            case ELASTICSEARCH:
                s = DataSetEditorConstants.INSTANCE.elasticSearch();
                break;
        }
        return s;
    }
    
    private void showTab(TabListItem tab) {
        tab.setVisible( true );
    }

    private void hideTab(TabListItem tab) {
        tab.setVisible( false );
    }
    
    private void removeNextButtonHandler() {
        if (nextButtonHandlerRegistration != null) nextButtonHandlerRegistration.removeHandler();
    }

    private void removeCancelButtonHandler() {
        if (cancelButtonHandlerRegistration != null) cancelButtonHandlerRegistration.removeHandler();
    }

    private void tabErrors(final TabListItem tab) {
        if (tab != null) {
            Node first = tab.getElement().getFirstChild();
            if (first.getNodeType() == Node.ELEMENT_NODE) {
                Element anchor =(Element)first;
                anchor.getStyle().setColor("red");
            }
        }
    }

    private void tabNoErrors(final TabListItem tab) {
        if (tab != null) {
            final boolean isActive = tab.isActive();
            Node first = tab.asWidget().getElement().getFirstChild();
            if (first.getNodeType() == Node.ELEMENT_NODE) { 
                Element anchor =(Element)first;
                anchor.getStyle().setColor("#0088CC");
            }
        }
    }
    
    private void resetTabErrors() {
        tabNoErrors(configurationTabItem);
        tabNoErrors(previewTabItem);
        tabNoErrors(advancedConfigurationTabItem);
    }

    
}
