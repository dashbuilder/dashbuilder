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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
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
    }
    
    @UiField
    FlowPanel mainPanel;

    @UiField
    FlowPanel initialViewPanel;

    @UiField
    Hyperlink newDataSetLink;

    @UiField
    VerticalPanel basicAttributesEditionViewPanel;

    @UiField
    DataSetBasicAttributesEditor dataSetBasicAttributesEditor;

    @UiField
    DataSetProviderTypeEditor dataSetProviderTypeEditor;

    @UiField
    VerticalPanel advancedAttributesEditionView;

    @UiField
    DataSetAdvancedAttributesEditor dataSetAdvancedAttributesEditor;
    
    @UiField
    VerticalPanel sqlAttributesEditionView;

    @UiField
    SQLDataSetDefAttributesEditor sqlDataSetDefAttributesEditor;
    
    @UiField
    VerticalPanel columnsAndFilterEditionViewPanel;

    @UiField
    DataSetColumnsAndFilterEditor dataSetColumnsAndFilterEditor;

    @UiField
    VerticalPanel csvAttributesEditionView;

    @UiField
    CSVDataSetDefAttributesEditor csvDataSetDefAttributesEditor;

    @UiField
    VerticalPanel beanAttributesEditionView;

    @UiField
    BeanDataSetDefAttributesEditor beanDataSetDefAttributesEditor;

    @UiField
    VerticalPanel elAttributesEditionView;

    @UiField
    ELDataSetDefAttributesEditor elDataSetDefAttributesEditor;
    
    @UiField
    HorizontalPanel buttonsPanel;

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
    public DataSetEditor.View edit(DataSetDef dataSetDef, DataSetDefEditWorkflow workflow) {
        this.dataSetDef = dataSetDef;
        this.workflow = workflow;
        
        return this;
    }

    @Override
    public DataSetEditor.View setEditMode(final boolean editMode) {
        this.isEditMode = editMode;
        
        return this;
    }

    public DataSetEditor.View showInitialView() {
        resetViews();
        initialViewPanel.setVisible(true);

        return this;
    }

    @Override
    public DataSetEditor.View showBasicAttributesEditionView(final ClickHandler nextHandler, final ClickHandler backHandler, ClickHandler cancelHandler) {
        workflow.clear().edit(dataSetBasicAttributesEditor, dataSetDef).edit(dataSetProviderTypeEditor, dataSetDef);
        resetViews();
        basicAttributesEditionViewPanel.setVisible(true);
        dataSetBasicAttributesEditor.setEditMode(true);
        dataSetProviderTypeEditor.setEditMode(!isEditMode);
        showButtons(nextHandler, backHandler, cancelHandler);

        return this;
    }

    @Override
    public DataSetEditor.View showAdvancedAttributesEditionView(final ClickHandler nextHandler, final ClickHandler backHandler, ClickHandler cancelHandler) {
        workflow.clear().edit(dataSetAdvancedAttributesEditor, dataSetDef);
        resetViews();
        advancedAttributesEditionView.setVisible(true);
        dataSetAdvancedAttributesEditor.setEditMode(true);
        showButtons(nextHandler, backHandler, cancelHandler);

        return this;
    }

    @Override
    public DataSetEditor.View showSQLAttributesEditorView() {
        workflow.edit(sqlDataSetDefAttributesEditor, (SQLDataSetDef) dataSetDef);
        sqlAttributesEditionView.setVisible(true);
        sqlDataSetDefAttributesEditor.setEditMode(true);
        return this;
    }

    @Override
    public DataSetEditor.View showBeanAttributesEditorView() {
        workflow.edit(beanDataSetDefAttributesEditor, (BeanDataSetDef) dataSetDef);
        beanAttributesEditionView.setVisible(true);
        beanDataSetDefAttributesEditor.setEditMode(true);
        return this;
    }

    @Override
    public DataSetEditor.View showCSVAttributesEditorView() {
        workflow.edit(csvDataSetDefAttributesEditor, (CSVDataSetDef) dataSetDef);
        csvAttributesEditionView.setVisible(true);
        csvDataSetDefAttributesEditor.setEditMode(true);
        return this;
    }

    @Override
    public DataSetEditor.View showELAttributesEditorView() {
        workflow.edit(elDataSetDefAttributesEditor, (ElasticSearchDataSetDef) dataSetDef);
        elAttributesEditionView.setVisible(true);
        elDataSetDefAttributesEditor.setEditMode(true);
        return this;
    }

    @Override
    public DataSetEditor.View showColumnsAndFilterEditionView(final ClickHandler nextHandler, final ClickHandler backHandler, ClickHandler cancelHandler) {
        initialViewPanel.setVisible(false);
        basicAttributesEditionViewPanel.setVisible(false);
        advancedAttributesEditionView.setVisible(false);
        sqlAttributesEditionView.setVisible(false);
        columnsAndFilterEditionViewPanel.setVisible(true);
        dataSetColumnsAndFilterEditor.setEditMode(true);
        showButtons(nextHandler, backHandler, cancelHandler);

        return this;
    }

    @Override
    public DataSetEditor.View clear() {
        showInitialView();
        return this;
    }
    
    private void resetViews() {
        initialViewPanel.setVisible(false);
        basicAttributesEditionViewPanel.setVisible(false);
        advancedAttributesEditionView.setVisible(false);
        sqlAttributesEditionView.setVisible(false);
        csvAttributesEditionView.setVisible(false);
        beanAttributesEditionView.setVisible(false);
        elAttributesEditionView.setVisible(false);
        columnsAndFilterEditionViewPanel.setVisible(false);
        buttonsPanel.setVisible(false);
    }
    
    private void showButtons(ClickHandler nextHandler, final ClickHandler backHandler, ClickHandler cancelHandler) {
        nextButton.setVisible(nextHandler != null);
        if (nextHandler != null) nextButtonHandlerRegistration = nextButton.addClickHandler(nextHandler);
        backButton.setVisible(backHandler != null);
        if (backHandler != null) backButtonHandlerRegistration = backButton.addClickHandler(backHandler);
        cancelButton.setVisible(cancelHandler!= null);
        if (cancelHandler != null) cancelButtonHandlerRegistration = cancelButton.addClickHandler(cancelHandler);
        buttonsPanel.setVisible(true);
    }

    public DataSetEditor.View removeButtonsHandler() {
        removeNextButtonHandler();
        removeBackButtonHandler();
        removeCancelButtonHandler();
        
        return this;
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
