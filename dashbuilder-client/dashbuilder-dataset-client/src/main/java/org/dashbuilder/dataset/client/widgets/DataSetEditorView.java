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

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.dataset.client.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataset.client.resources.i18n.DataSetEditorMessages;
import org.dashbuilder.dataset.client.validation.DataSetDefEditWorkflow;
import org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetColumnsAndFilterEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.dataset.client.widgets.editors.bean.BeanDataSetDefAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.csv.CSVDataSetDefAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.elasticsearch.ELDataSetDefAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.sql.SQLDataSetDefAttributesEditor;
import org.dashbuilder.dataset.def.*;

import javax.enterprise.context.Dependent;

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
    FlowPanel columnsAndFilterEditionViewPanel;

    @UiField
    DataSetColumnsAndFilterEditor dataSetColumnsAndFilterEditor;

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
    com.github.gwtbootstrap.client.ui.Button backButton;

    @UiField
    com.github.gwtbootstrap.client.ui.Button nextButton;

    private DataSetDef dataSetDef = null;
    
    private boolean isEditMode = true;
    private DataSetDefEditWorkflow workflow;
    
    private HandlerRegistration nextButtonHandlerRegistration = null;
    private HandlerRegistration backButtonHandlerRegistration = null;
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
        setEditorTitle("");
        
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
        
        // Remove current handler for view buttons.
        removeButtonsHandler();

        return this;
    }


    @Override
    public DataSetEditor.View showProviderSelectionView() {
        workflow.clear().edit(dataSetProviderTypeEditor, dataSetDef);

        // View title.
        setEditorTitle(DataSetEditorMessages.INSTANCE.newDataSet(""));
        
        providerSelectionViewPanel.setVisible(true);
        dataSetProviderTypeEditor.setEditMode(!isEditMode);

        return this;
    }


    @Override
    public DataSetEditor.View showBasicAttributesEditionView() {
        workflow.clear().edit(dataSetBasicAttributesEditor, dataSetDef);

        // View title.
        setEditorTitle(DataSetEditorMessages.INSTANCE.newDataSet(dataSetDef.getProvider().name()));
        
        basicAttributesEditionViewPanel.setVisible(true);
        dataSetBasicAttributesEditor.setEditMode(true);

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
    public DataSetEditor.View showColumnsAndFilterEditionView() {
        // TODO: workflow.clear().edit(dataSetBasicAttributesEditor, dataSetDef);

        dataSetColumnsAndFilterEditor.setVisible(true);
        dataSetColumnsAndFilterEditor.setEditMode(true);
        showTab(dataConfigurationTab);
        showTab(dataPreviewTab);
        tabViewPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showAdvancedAttributesEditionView() {
        workflow.clear().edit(dataSetAdvancedAttributesEditor, dataSetDef);
        
        advancedAttributesEditionViewPanel.setVisible(true);
        dataSetAdvancedAttributesEditor.setEditMode(true);
        showTab(dataConfigurationTab);
        showTab(dataPreviewTab);
        showTab(dataAdvancedConfigurationTab);
        tabViewPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showNextButton(final ClickHandler nextHandler) {
        nextButton.setVisible(nextHandler != null);
        if (nextHandler != null) nextButtonHandlerRegistration = nextButton.addClickHandler(nextHandler);
        buttonsPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showBackButton(final ClickHandler backHandler) {
        backButton.setVisible(backHandler != null);
        if (backHandler != null) backButtonHandlerRegistration = backButton.addClickHandler(backHandler);
        buttonsPanel.setVisible(true);
        return this;
    }

    @Override
    public DataSetEditor.View showCancelButton(final ClickHandler cancelHandler) {
        cancelButton.setVisible(cancelHandler!= null);
        if (cancelHandler != null) cancelButtonHandlerRegistration = cancelButton.addClickHandler(cancelHandler);
        buttonsPanel.setVisible(true);
        return this;
    }

    private void setEditorTitle(String  text) {
        title.setText(text);
    }

    @Override
    public DataSetEditor.View clear() {
        clearView();
        this.dataSetDef = null;
        this.workflow = null;
        return this;
    }
    
    private void clearView() {
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
        columnsAndFilterEditionViewPanel.setVisible(false);
        nextButton.setVisible(false);
        backButton.setVisible(false);
        cancelButton.setVisible(false);
        buttonsPanel.setVisible(false);
    }
    
    private void showTab(Tab tab) {
        tab.asWidget().setVisible(true);        
    }

    private void hideTab(Tab tab) {
        tab.asWidget().setVisible(false);
    }
    
    private void removeButtonsHandler() {
        removeNextButtonHandler();
        removeBackButtonHandler();
        removeCancelButtonHandler();
    }
    
    private void removeNextButtonHandler() {
        if (nextButtonHandlerRegistration != null) nextButtonHandlerRegistration.removeHandler();;
    }

    private void removeBackButtonHandler() {
        if (backButtonHandlerRegistration != null)         backButtonHandlerRegistration.removeHandler();;
    }

    private void removeCancelButtonHandler() {
        if (cancelButtonHandlerRegistration != null) cancelButtonHandlerRegistration.removeHandler();;
    }

}
