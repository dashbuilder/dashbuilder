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
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.client.widgets.dataset.editor.DataSetDefEditWorkflow;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorMessages;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.*;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.bean.BeanDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.csv.CSVDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.elasticsearch.ELDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.sql.SQLDataSetDefAttributesEditor;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;

import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>Default view for DataSetEditor presenter view.</p> 
 */
@Dependent
public class DataSetEditorView extends Composite implements DataSetEditor.View {

    interface DataSetEditorViewBinder extends UiBinder<Widget, DataSetEditorView> {}
    private static DataSetEditorViewBinder uiBinder = GWT.create(DataSetEditorViewBinder.class);

    interface DataSetEditorViewStyle extends CssResource {
        String mainPanel();
        String dataSetCountLabel();
    }

    /*@UiField
    DataSetEditorViewStyle style;*/
    
    @UiField
    FlowPanel mainPanel;

    @UiField
    HTML title;
    
    @UiField
    FlowPanel initialViewPanel;

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
    Tab dataPreviewTab;

    @UiField
    Tab dataAdvancedConfigurationTab;
    
    @UiField
    FlowPanel basicAttributesEditionViewPanel;

    @UiField
    DataSetBasicAttributesEditor dataSetBasicAttributesEditor;

    @UiField
    FlowPanel sqlAttributesEditionViewPanel;

    @UiField
    SQLDataSetDefAttributesEditor sqlDataSetDefAttributesEditor;
    
    @UiField
    FlowPanel previewTableEditionViewPanel;

    @UiField
    DataSetPreviewEditor previewTableEditor;

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
    com.github.gwtbootstrap.client.ui.Button cancelButton;

    @UiField
    com.github.gwtbootstrap.client.ui.Button nextButton;

    private DataSetDef dataSetDef = null;
    
    private boolean isEditMode = true;
    private DataSetDefEditWorkflow workflow;
    
    private HandlerRegistration nextButtonHandlerRegistration = null;
    private HandlerRegistration cancelButtonHandlerRegistration = null;
    
    public DataSetEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public DataSetEditor.View setEditMode(final boolean editMode) {
        this.isEditMode = editMode;
        
        return this;
    }

    public DataSetEditor.View showInitialView(final ClickHandler newDataSetHandler) {
        clearView();
        
        // View title.
        showTitle();
        
        // TODO: Obtain data set count from a backend service.
        dataSetCountText.setText(DataSetEditorMessages.INSTANCE.dataSetCount(0));
        newDataSetLink.addClickHandler(newDataSetHandler);
        initialViewPanel.setVisible(true);

        return this;
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
        
        providerSelectionViewPanel.setVisible(true);
        dataSetProviderTypeEditor.setEditMode(!isEditMode);

        return this;
    }


    @Override
    public DataSetEditor.View showBasicAttributesEditionView() {
        workflow.edit(dataSetBasicAttributesEditor, dataSetDef);

        // View title.
        showTitle();
        
        basicAttributesEditionViewPanel.setVisible(true);
        dataSetBasicAttributesEditor.setEditMode(true);
        activeDataConfigurationTab();
        return this;
    }

    @Override
    public DataSetEditor.View showSQLAttributesEditorView() {
        workflow.edit(sqlDataSetDefAttributesEditor, (SQLDataSetDef) dataSetDef);
        sqlAttributesEditionViewPanel.setVisible(true);
        sqlDataSetDefAttributesEditor.setEditMode(true);
        showTab(dataConfigurationTab);
        tabViewPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showBeanAttributesEditorView() {
        workflow.edit(beanDataSetDefAttributesEditor, (BeanDataSetDef) dataSetDef);
        beanAttributesEditionViewPanel.setVisible(true);
        beanDataSetDefAttributesEditor.setEditMode(true);
        showTab(dataConfigurationTab);
        tabViewPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showCSVAttributesEditorView() {
        workflow.edit(csvDataSetDefAttributesEditor, (CSVDataSetDef) dataSetDef);
        csvAttributesEditionViewPanel.setVisible(true);
        csvDataSetDefAttributesEditor.setEditMode(true);
        showTab(dataConfigurationTab);
        tabViewPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showELAttributesEditorView() {
        workflow.edit(elDataSetDefAttributesEditor, (ElasticSearchDataSetDef) dataSetDef);
        elAttributesEditionViewPanel.setVisible(true);
        elDataSetDefAttributesEditor.setEditMode(true);
        showTab(dataConfigurationTab);
        tabViewPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showPreviewTableEditionView(final DisplayerListener tableListener) {
        // Table is not a data set editor component, just a preview data set widget.
        // So not necessary to use the editor workflow this instance.
        
        // View title.
        showTitle();
        
        // Configure tabs and visibility.
        previewTableEditor.setVisible(true);
        previewTableEditor.setEditMode(true);
        previewTableEditor.build(tableListener);
        showTab(dataConfigurationTab);
        showTab(dataPreviewTab);
        previewTableEditionViewPanel.setVisible(true);
        activeDataPreviewTab();
        tabViewPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showColumnsAndFilterEditionView(final DataSet dataSet, final DataSetColumnsEditor.ColumnsChangedEventHandler columnsChangedEventHandler) {
        // Columns editor is not a data set editor component, just a widget to handle DataColumnEditor instances.
        // So not necessary to use the editor workflow this instance.

        // Data Set Columns editor.
        columnsEditor.setVisible(true);
        columnsEditor.setEditMode(true);
        columnsEditor.build(dataSet.getColumns(), workflow);
        columnsEditor.addColumnsChangeHandler(columnsChangedEventHandler);
        
        // Data Set Filter editor.
        final DataSetFilterEditor filterEditor = new DataSetFilterEditor();
        filterEditor.init(dataSet.getMetadata(), dataSetDef.getDataSetFilter(), new DataSetFilterEditor.Listener() {
            @Override
            public void filterChanged(DataSetFilter filter) {
                // TODO
                GWT.log("Filter changed.");
            }
        });
        filterTab.add(filterEditor);

        // Panels and tab visibility.
        filterAndColumnsEditionViewPanel.setVisible(true);
        filterAndColumnsTabPanel.setVisible(true);
        activeDataPreviewTab();
        tabViewPanel.setVisible(true);
        
        return this;
    }

    @Override
    public DataSetEditor.View showAdvancedAttributesEditionView() {
        workflow.edit(dataSetAdvancedAttributesEditor, dataSetDef);

        // View title.
        showTitle();
        
        advancedAttributesEditionViewPanel.setVisible(true);
        dataSetAdvancedAttributesEditor.setEditMode(true);
        showTab(dataConfigurationTab);
        showTab(dataPreviewTab);
        showTab(dataAdvancedConfigurationTab);
        activeDataAdvancedConfigurationTab();
        tabViewPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showNextButton(final String title, final ClickHandler nextHandler) {
        nextButton.setVisible(nextHandler != null);
        if (title != null) {
            nextButton.setText(title);
            nextButton.setTitle(title);
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
                    if (hasViolations(beanDataSetDefAttributesEditor.getViolations())) tabErrors(dataConfigurationTab);
                    break;
                case CSV:
                    if (hasViolations(csvDataSetDefAttributesEditor.getViolations())) tabErrors(dataConfigurationTab);
                    break;
                case SQL:
                    if (hasViolations(sqlDataSetDefAttributesEditor.getViolations())) tabErrors(dataConfigurationTab);
                    break;
                case ELASTICSEARCH:
                    if (hasViolations(elDataSetDefAttributesEditor.getViolations())) tabErrors(dataConfigurationTab);
                    break;
            }
        }

        if (hasViolations(previewTableEditor.getViolations())) tabErrors(dataPreviewTab);
        if (hasViolations(dataSetAdvancedAttributesEditor.getViolations())) tabErrors(dataAdvancedConfigurationTab);
        if (hasViolations(columnsEditor.getViolations())) tabErrors(dataPreviewTab);

        return this;
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
        
        // Clear violations.
        clearEditorViolations();
        
        this.dataSetDef = null;
        this.workflow = null;
        return this;
    }
    
    private void activeDataConfigurationTab() {
        dataConfigurationTab.setActive(true);
        dataPreviewTab.setActive(false);
        dataAdvancedConfigurationTab.setActive(false);
    }

    private void activeDataPreviewTab() {
        dataConfigurationTab.setActive(false);
        dataPreviewTab.setActive(true);
        dataAdvancedConfigurationTab.setActive(false);
    }

    private void activeDataAdvancedConfigurationTab() {
        dataConfigurationTab.setActive(false);
        dataPreviewTab.setActive(false);
        dataAdvancedConfigurationTab.setActive(true);
    }
    
    private void clearView() {
        title.setVisible(false);
        initialViewPanel.setVisible(false);
        providerSelectionViewPanel.setVisible(false);
        tabViewPanel.setVisible(false);
        hideTab(dataConfigurationTab);
        hideTab(dataPreviewTab);
        hideTab(dataAdvancedConfigurationTab);
        basicAttributesEditionViewPanel.setVisible(false);
        advancedAttributesEditionViewPanel.setVisible(false);
        sqlAttributesEditionViewPanel.setVisible(false);
        csvAttributesEditionViewPanel.setVisible(false);
        beanAttributesEditionViewPanel.setVisible(false);
        elAttributesEditionViewPanel.setVisible(false);
        previewTableEditionViewPanel.setVisible(false);
        filterAndColumnsEditionViewPanel.setVisible(false);
        nextButton.setVisible(false);
        cancelButton.setVisible(false);
        buttonsPanel.setVisible(false);
    }
    
    private void clearEditorViolations() {
        dataSetProviderTypeEditor.setViolations(null);
        dataSetBasicAttributesEditor.setViolations(null);
        beanDataSetDefAttributesEditor.setViolations(null);
        csvDataSetDefAttributesEditor.setViolations(null);
        sqlDataSetDefAttributesEditor.setViolations(null);
        elDataSetDefAttributesEditor.setViolations(null);
        previewTableEditor.setViolations(null);
        dataSetAdvancedAttributesEditor.setViolations(null);
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

        } else {

            title.setVisible(false);

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
        if (nextButtonHandlerRegistration != null) nextButtonHandlerRegistration.removeHandler();;
    }

    private void removeCancelButtonHandler() {
        if (cancelButtonHandlerRegistration != null) cancelButtonHandlerRegistration.removeHandler();;
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
        tabNoErrors(dataPreviewTab);
        tabNoErrors(dataAdvancedConfigurationTab);
    }

}
