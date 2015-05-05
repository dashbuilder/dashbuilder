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

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.constants.IconType;
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
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorMessages;
import org.dashbuilder.common.client.widgets.SlidingPanel;
import org.dashbuilder.common.client.widgets.TimeoutPopupPanel;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetExportReadyCallback;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;

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

    private static final String EXPORT_ICON_SIZE = "20px";
    private static final String LOADING_IMAGE_SIZE = "16px";
    private static final int LOADING_SCREEN_TIMEOUT = 60000;
    
    interface DataSetEditorViewBinder extends UiBinder<Widget, DataSetEditorView> {}
    private static DataSetEditorViewBinder uiBinder = GWT.create(DataSetEditorViewBinder.class);

    interface DataSetEditorViewStyle extends CssResource {
        String well_ghostwhite();
        String disabledBar();
        String slidingPanel();
        String columnsFilterDisclosurePanelHeaderOpen();
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
    HTML errorType;

    @UiField
    HTML errorMessage;

    @UiField
    HTML errorCause;

    @UiField
    Row errorTypeRow;

    @UiField
    Row errorMessageRow;

    @UiField
    Row errorCauseRow;
    
    @UiField
    TimeoutPopupPanel loadingPopupPanel;
    
    @UiField
    Image loadingImage;
    
    @UiField
    StackProgressBar progressBar;
    
    @UiField
    Bar providerBar;
    
    @UiField
    Bar columnsFilterBar;

    @UiField
    Bar advancedAttrsBar;
    
    @UiField
    HTMLPanel initialViewPanel;

    @UiField
    HTML dataSetCountText;

    @UiField
    Hyperlink newDataSetLink;

    @UiField
    FlowPanel providerSelectionViewPanel;

    @UiField
    DataSetProviderTypeEditor dataSetProviderTypeEditor;

    @UiField
    com.github.gwtbootstrap.client.ui.TabPanel tabPanel;
    
    @UiField
    FlowPanel tabViewPanel;
    
    @UiField
    Tab dataConfigurationTab;

    @UiField
    Tab dataAdvancedConfigurationTab;
    
    @UiField
    FlowPanel basicAttributesEditionViewPanel;

    @UiField
    DataSetBasicAttributesEditor dataSetBasicAttributesEditor;

    SlidingPanel slidingPanel;
    
    @UiField
    FlowPanel specificProviderAttributesPanel;
    
    @UiField
    FlowPanel sqlAttributesEditionViewPanel;

    @UiField
    SQLDataSetDefAttributesEditor sqlDataSetDefAttributesEditor;
    
    @UiField
    FlowPanel testButtonPanel;
    
    @UiField
    Button testButton;
    
    @UiField
    FlowPanel filterColumnsPreviewTablePanel;
    
    @UiField
    FlowPanel backToSpecificAttrsButtonPanel;
    
    @UiField
    Button backToSpecificAttrsEditionButton;
    
    @UiField
    FlowPanel exportButtonsPanel;
    
    @UiField
    Image exportToExcelButton;

    @UiField
    Image exportToCSVButton;

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
    com.github.gwtbootstrap.client.ui.TabPanel filterAndColumnsTabPanel;

    @UiField
    Tab columnsTab;

    @UiField
    Tab filterTab;
    
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
    FlowPanel buttonsPanel;

    @UiField
    Button cancelButton;

    @UiField
    Button nextButton;
    
    @UiField
    Popover nextButtonPopover;
    
    private DataSetDef dataSetDef = null;
    private DataSetLookup lastDataSetLookup = null;
    
    private boolean isEditMode = true;
    private DataSetDefEditWorkflow workflow;
    
    private HandlerRegistration nextButtonHandlerRegistration = null;
    private HandlerRegistration cancelButtonHandlerRegistration = null;
    private HandlerRegistration testButtonHandlerRegistration = null;

    private final ClickHandler backToSpecificAttrsEditionButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            showSpecificProviderAttrsEditionView(null);
        }
    };

    private final DataSetExportReadyCallback exportReadyCallback = new DataSetExportReadyCallback() {
        @Override
        public void exportReady(String exportFilePath) {
            final String s = DataSetClientServices.get().getExportServletUrl();
            final String u = DataSetClientServices.get().getDownloadFileUrl(s, exportFilePath);
            GWT.log("Export URL: " + u);
            Window.open(u,
                    "downloading",
                    "resizable=no,scrollbars=yes,status=no");
        }
    };
    
    private final ClickHandler exportToExcelButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            try {
                DataSetClientServices.get().exportDataSetExcel(lastDataSetLookup, exportReadyCallback);
            } catch (Exception e) {
                showError(e);
            }
        }
    };

    private final ClickHandler exportToCSVButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            try {
                DataSetClientServices.get().exportDataSetCSV(lastDataSetLookup, exportReadyCallback);
            } catch (Exception e) {
                showError(e);
            }
        }
    };
    

    public DataSetEditorView() {
        initWidget(uiBinder.createAndBindUi(this));

        // Create the sliding panel for data configuration tab.
        slidingPanel = new SlidingPanel();
        slidingPanel.addStyleName(style.slidingPanel());
        slidingPanel.add(specificProviderAttributesPanel);
        slidingPanel.add(filterColumnsPreviewTablePanel);
        dataConfigurationTab.clear();
        dataConfigurationTab.add(slidingPanel);

        filterAndColumnsEditionDisclosurePanel.addOpenHandler(openColumnsFilterPanelHandler);

        filterAndColumnsEditionDisclosurePanel.addCloseHandler(closeColumnsFilterPanelHandler);
        
        // Configure buttons' click handlers.
        backToSpecificAttrsEditionButton.addClickHandler(backToSpecificAttrsEditionButtonHandler);
        exportToExcelButton.setUrl(DataSetClientResources.INSTANCE.images().excelIcon().getSafeUri());
        exportToExcelButton.setSize(EXPORT_ICON_SIZE, EXPORT_ICON_SIZE);
        exportToExcelButton.addClickHandler(exportToExcelButtonHandler);
        exportToCSVButton.setUrl(DataSetClientResources.INSTANCE.images().csvIcon32().getSafeUri());
        exportToCSVButton.setSize(EXPORT_ICON_SIZE, EXPORT_ICON_SIZE);
        exportToCSVButton.addClickHandler(exportToCSVButtonHandler);
        
        // Hide loading popup at startup.
        loadingPopupPanel.setTimeout(LOADING_SCREEN_TIMEOUT);
        loadingImage.setUrl(DataSetClientResources.INSTANCE.images().loadingIcon().getSafeUri());
        loadingImage.setSize(LOADING_IMAGE_SIZE, LOADING_IMAGE_SIZE);
        hideLoadingView();

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
        }
    };

    private final CloseHandler<DisclosurePanel> closeColumnsFilterPanelHandler = new CloseHandler<DisclosurePanel>() {
        @Override
        public void onClose(CloseEvent<DisclosurePanel> event) {
            columnsFilterDisclosurePanelHeader.setTitle(DataSetEditorConstants.INSTANCE.showColumnsAndFilter());
            columnsFilterDisclosurePanelHeader.removeStyleName(style.columnsFilterDisclosurePanelHeaderOpen());
            columnsFilterDisclosurePanelButton.setType(IconType.STEP_FORWARD);
            columnsFilterDisclosurePanelButton.setTitle(DataSetEditorConstants.INSTANCE.showColumnsAndFilter());
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
    
    public DataSetEditor.View showHomeView(final int dsetCount, final ClickHandler newDataSetHandler) {
        clearView();
        
        // View title.
        showTitle();
        
        dataSetCountText.setText(DataSetEditorMessages.INSTANCE.dataSetCount(dsetCount));
        newDataSetLink.addClickHandler(newDataSetHandler);
        initialViewPanel.setVisible(true);
        mainPanel.addStyleName(style.well_ghostwhite());

        return this;
    }
    
    private boolean isHomeViewVisible() {
        return initialViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View edit(final DataSetDef dataSetDef, final DataSetDefEditWorkflow workflow) {
        this.dataSetDef = dataSetDef;
        this.workflow = workflow;

        // Reset current view.
        clearView();
        mainPanel.removeStyleName(style.well_ghostwhite());

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
        
        // Progress bar.
        progressStep1();

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

        // View title.
        showTitle();

        // Progress bar.
        progressStep1();

        basicAttributesEditionViewPanel.setVisible(true);
        dataSetBasicAttributesEditor.setEditMode(true);
        activeDataConfigurationTab();
        return this;
    }

    private boolean isBasicAttributesEditionViewVisible() {
        return basicAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showSQLAttributesEditorView(final ClickHandler testHandler) {
        workflow.edit(sqlDataSetDefAttributesEditor, (SQLDataSetDef) dataSetDef);
        sqlAttributesEditionViewPanel.setVisible(true);
        sqlDataSetDefAttributesEditor.setEditMode(true);
        showSpecificProviderAttrsEditionView(testHandler);
        return this;
    }

    private boolean isSQLAttributesEditorViewVisible() {
        return sqlAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showBeanAttributesEditorView(final ClickHandler testHandler) {
        workflow.edit(beanDataSetDefAttributesEditor, (BeanDataSetDef) dataSetDef);
        beanAttributesEditionViewPanel.setVisible(true);
        beanDataSetDefAttributesEditor.setEditMode(true);
        showSpecificProviderAttrsEditionView(testHandler);
        return this;
    }

    private boolean isBeanAttributesEditorViewVisible() {
        return beanAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showCSVAttributesEditorView(final FormPanel.SubmitCompleteHandler submitCompleteHandler, final ClickHandler testHandler) {
        workflow.edit(csvDataSetDefAttributesEditor, (CSVDataSetDef) dataSetDef);
        csvAttributesEditionViewPanel.setVisible(true);
        csvDataSetDefAttributesEditor.setEditMode(true);
        csvDataSetDefAttributesEditor.addSubmitCompleteHandler(submitCompleteHandler);
        showSpecificProviderAttrsEditionView(testHandler);
        return this;
    }

    private boolean isCSVAttributesEditorViewVisible() {
        return csvAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showELAttributesEditorView(final ClickHandler testHandler) {
        workflow.edit(elDataSetDefAttributesEditor, (ElasticSearchDataSetDef) dataSetDef);
        elAttributesEditionViewPanel.setVisible(true);
        elDataSetDefAttributesEditor.setEditMode(true);
        showSpecificProviderAttrsEditionView(testHandler);
        return this;
    }

    private boolean isELAttributesEditorViewVisible() {
        return elAttributesEditionViewPanel.isVisible();
    }
    
    private void addTestButtonHandler(final ClickHandler testHandler) {
        if (testHandler != null)
        {
            removetestButtonHandler();
            testButtonHandlerRegistration = testButton.addClickHandler(testHandler);
        }
    }
    
    private void showSpecificProviderAttrsEditionView(final ClickHandler testHandler) 
    {
        showTab(dataConfigurationTab);
        tabViewPanel.setVisible(true);
        addTestButtonHandler(testHandler);
        slidingPanel.setWidget(specificProviderAttributesPanel);
        // animation.run(ANIMATION_DURATION);
    }

    @Override
    public DataSetEditor.View showPreviewTableEditionView(final Displayer tableDisplayer) {
        // Table is not a data set editor component, just a preview data set widget.
        // So not necessary to use the editor workflow this instance.
        this.lastDataSetLookup = tableDisplayer.getDisplayerSettings().getDataSetLookup();

        // View title.
        hideLoadingView();
        showTitle();

        // Progress bar.
        progressStep2();
        
        // Configure tabs and visibility.
        previewTableEditor.setVisible(true);
        previewTableEditor.setEditMode(true);
        previewTableEditor.build(tableDisplayer);
        showTab(dataConfigurationTab);
        previewTableEditionViewPanel.setVisible(true);
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
        columnsEditor.setEditMode(true);
        columnsEditor.build(columns, dataSet, workflow);
        columnsEditor.addColumnsChangeHandler(columnsChangedEventHandler);

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
        filterTab.clear();

        hideLoadingView();
        
        // Data Set Filter editor.
        final DataSetFilterEditor filterEditor = new DataSetFilterEditor();
        filterEditor.init(dataSet.getMetadata(), dataSetDef.getDataSetFilter(), filterListener);
        filterTab.add(filterEditor);

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
        activeDataConfigurationTab();
        tabViewPanel.setVisible(true);
        slidingPanel.setWidget(filterColumnsPreviewTablePanel);
    }
    
    @Override
    public DataSetEditor.View showAdvancedAttributesEditionView() {
        workflow.edit(dataSetAdvancedAttributesEditor, dataSetDef);

        // View title.
        showTitle();

        // Progress bar.
        progressStep3();

        advancedAttributesEditionViewPanel.setVisible(true);
        dataSetAdvancedAttributesEditor.setEditMode(true);
        showTab(dataConfigurationTab);
        showTab(dataAdvancedConfigurationTab);
        activeDataAdvancedConfigurationTab();
        tabViewPanel.setVisible(true);
        return this;
    }

    private boolean isAdvancedAttributesEditionViewVisible() {
        return advancedAttributesEditionViewPanel.isVisible();
    }

    @Override
    public DataSetEditor.View showNextButton(final String title, final String helpText, final ClickHandler nextHandler) {
        nextButton.setVisible(nextHandler != null);
        if (title != null) {
            nextButton.setText(title);
            nextButton.setTitle(title);
            nextButtonPopover.setHeading(title);
            nextButtonPopover.setText(helpText != null ? helpText : "");
        }
        if (nextHandler != null) {
            removeNextButtonHandler();
            nextButtonHandlerRegistration = nextButton.addClickHandler(nextHandler);
        }
        buttonsPanel.setVisible(true);
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
        
        // Update title if necessary.
        showTitle();
        
        // Check editor errors. If any, mark error in parent Tab.
        resetTabErrors();
        DataSetProviderType type = dataSetDef.getProvider();
        if (type != null) {
            switch (type) {
                case BEAN:
                    if (isBeanAttributesEditorViewVisible() && hasViolations(beanDataSetDefAttributesEditor.getViolations())) tabErrors(dataConfigurationTab);
                    break;
                case CSV:
                    if (isCSVAttributesEditorViewVisible() && hasViolations(csvDataSetDefAttributesEditor.getViolations())) tabErrors(dataConfigurationTab);
                    break;
                case SQL:
                    if (isSQLAttributesEditorViewVisible() && hasViolations(sqlDataSetDefAttributesEditor.getViolations())) tabErrors(dataConfigurationTab);
                    break;
                case ELASTICSEARCH:
                    if (isELAttributesEditorViewVisible()) {
                        // Save attributes not handled by editor framework.
                        elDataSetDefAttributesEditor.save();

                        // Check violations.
                        if (hasViolations(elDataSetDefAttributesEditor.getViolations())) tabErrors(dataConfigurationTab);
                    }
                    break;
            }
        }

        if (hasViolations(previewTableEditor.getViolations())) tabErrors(dataAdvancedConfigurationTab);
        if (hasViolations(dataSetAdvancedAttributesEditor.getViolations())) tabErrors(dataAdvancedConfigurationTab);
        if (hasViolations(columnsEditor.getViolations())) tabErrors(dataAdvancedConfigurationTab);

        return this;
    }

    @Override
    public DataSetEditor.View showLoadingView() {
        loadingPopupPanel.center();
        loadingPopupPanel.setVisible(true);
        loadingPopupPanel.show();
        return this;
    }
    
    private void hideLoadingView() {
        loadingPopupPanel.setVisible(false);
        loadingPopupPanel.hide();
    }

    private void showError(final Exception e) {
        if (e != null) {
            String type = null;
            String message = null;
            String cause = null;
            type = e.getClass().getName();
            if (e.getMessage() != null) message = e.getMessage();
            if (e.getCause() != null) cause = e.getCause().getMessage();
            showError(type, message, cause);
        }
    }
    
    @Override
    public DataSetEditor.View showError(final String type, final String message, final String cause) {
        errorType.setText(type != null ? type : "");
        errorTypeRow.setVisible(type != null);
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
        this.lastDataSetLookup = null;
        this.workflow = null;
        return this;
    }
    
    private void activeDataConfigurationTab() {
        dataConfigurationTab.setActive(true);
        dataAdvancedConfigurationTab.setActive(false);
    }

    private void activeDataAdvancedConfigurationTab() {
        dataConfigurationTab.setActive(false);
        dataAdvancedConfigurationTab.setActive(true);
    }
    
    private void progressStep1() {
        providerBar.removeStyleName(style.disabledBar());
        columnsFilterBar.addStyleName(style.disabledBar());
        advancedAttrsBar.addStyleName(style.disabledBar());
    }

    private void progressStep2() {
        providerBar.removeStyleName(style.disabledBar());
        columnsFilterBar.removeStyleName(style.disabledBar());
        advancedAttrsBar.addStyleName(style.disabledBar());
    }


    private void progressStep3() {
        providerBar.removeStyleName(style.disabledBar());
        columnsFilterBar.removeStyleName(style.disabledBar());
        advancedAttrsBar.removeStyleName(style.disabledBar());
    }


    private void clearView() {
        titlePanel.setVisible(false);
        title.setVisible(false);
        progressBar.setVisible(false);
        initialViewPanel.setVisible(false);
        providerSelectionViewPanel.setVisible(false);
        tabViewPanel.setVisible(false);
        hideTab(dataConfigurationTab);
        hideTab(dataAdvancedConfigurationTab);
        basicAttributesEditionViewPanel.setVisible(false);
        advancedAttributesEditionViewPanel.setVisible(false);
        sqlAttributesEditionViewPanel.setVisible(false);
        csvAttributesEditionViewPanel.setVisible(false);
        beanAttributesEditionViewPanel.setVisible(false);
        elAttributesEditionViewPanel.setVisible(false);
        previewTableEditionViewPanel.setVisible(false);
        nextButton.setVisible(false);
        nextButtonPopover.setHeading("");
        nextButtonPopover.setText("");
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
                title.setText(DataSetEditorMessages.INSTANCE.newDataSet(""));
            } else if (_provider != null && _name == null) {
                title.setText(DataSetEditorMessages.INSTANCE.newDataSet(getProviderName(_provider)));
            } else if (_provider == null) {
                title.setText(_name);
            } else {
                title.setText(_name + " (" + getProviderName(_provider) + ")");
            }
            
            title.setVisible(true);
            titlePanel.setVisible(true);
            if (!isEditMode) progressBar.setVisible(true);

        } else {

            title.setVisible(false);
            titlePanel.setVisible(false);
        }
        
    }
    
    private static String getProviderName(DataSetProviderType type) {
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
    
    private void showTab(Tab tab) {
        tab.asWidget().setVisible(true);        
    }

    private void hideTab(Tab tab) {
        tab.asWidget().setVisible(false);
    }
    
    private void removeNextButtonHandler() {
        if (nextButtonHandlerRegistration != null) nextButtonHandlerRegistration.removeHandler();
    }

    private void removeCancelButtonHandler() {
        if (cancelButtonHandlerRegistration != null) cancelButtonHandlerRegistration.removeHandler();
    }

    private void removetestButtonHandler() {
        if (testButtonHandlerRegistration != null) testButtonHandlerRegistration.removeHandler();
    }
    
    private void tabErrors(final Tab tab) {
        if (tab != null) {
            Node first = tab.asWidget().getElement().getFirstChild();
            if (first.getNodeType() == Node.ELEMENT_NODE) {
                Element anchor =(Element)first;
                anchor.getStyle().setColor("red");
            }
        }
    }

    private void tabNoErrors(final Tab tab) {
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
        tabNoErrors(dataConfigurationTab);
        tabNoErrors(dataAdvancedConfigurationTab);
    }

}
