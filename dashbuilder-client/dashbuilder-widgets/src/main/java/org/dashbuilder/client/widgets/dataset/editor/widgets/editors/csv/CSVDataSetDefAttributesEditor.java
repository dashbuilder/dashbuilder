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
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors.csv;

import java.util.List;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.AbstractDataSetDefEditor;
import org.dashbuilder.common.client.validation.editors.FileUploadEditor;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.client.validation.editors.CSVDataSetDefEditor;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Row;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing CSV provider specific attributes.</p>
 *
 * @since 0.3.0 
 */
@Dependent
public class CSVDataSetDefAttributesEditor extends AbstractDataSetDefEditor implements CSVDataSetDefEditor {
    
    interface CSVDataSetDefAttributesEditorBinder extends UiBinder<Widget, CSVDataSetDefAttributesEditor> {}
    private static CSVDataSetDefAttributesEditorBinder uiBinder = GWT.create(CSVDataSetDefAttributesEditorBinder.class);

    @UiField
    FlowPanel csvAttributesPanel;
    
    @UiField
    Row filePathRow;

    @UiField
    FileUploadEditor filePath;

    @UiField
    Row fileURLRow;

    @UiField
    ValueBoxEditorDecorator<String> fileURL;
    
    @UiField
    Button useFilePathButton;

    @UiField
    Button useFileURLButton;
    
    @UiField
    ValueBoxEditorDecorator<Character> separatorChar;

    @UiField
    ValueBoxEditorDecorator<Character> quoteChar;

    @UiField
    ValueBoxEditorDecorator<Character> escapeChar;

    @UiField
    ValueBoxEditorDecorator<String> datePattern;

    @UiField
    ValueBoxEditorDecorator<String> numberPattern;

    private boolean isEditMode;

    public CSVDataSetDefAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        // Switch file or URL.
        final ClickHandler useFilePathButtonHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showFilePath();
            }
        };
        final ClickHandler useFileURLButtonHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showFileURL();
            }
        };
        useFilePathButton.addClickHandler(useFilePathButtonHandler);
        useFileURLButton.addClickHandler(useFileURLButtonHandler);

        // File editor.
        filePath.setLoadingImageUri(DataSetClientResources.INSTANCE.images().loadingIcon().getSafeUri());
        filePath.setCallback(new FileUploadEditor.FileUploadEditorCallback() {
            @Override
            public String getUploadFileName() {
                return dataSetDef.getUUID() + ".csv";
            }
            @Override
            public String getUploadFileUrl() {
                String csvPath = "default://master@datasets/tmp/" + dataSetDef.getUUID() + ".csv";
                return DataSetClientServices.get().getUploadFileUrl(csvPath);
            }
        });
        
        // By default use file path.
        showFilePath();
    }
    
    public HandlerRegistration addSubmitCompleteHandler(final FormPanel.SubmitCompleteHandler submitCompleteHandler) {
        return filePath.addSubmitCompleteHandler(submitCompleteHandler);
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    @Override
    public void set(DataSetDef dataSetDef) {
        super.set(dataSetDef);
        if (getDefinition() != null && getDefinition().getFileURL() != null) {
            showFileURL();
        } else {
            showFilePath();
        }
    }
    
    private CSVDataSetDef getDefinition() {
        if (dataSetDef instanceof CSVDataSetDef) {
            return (CSVDataSetDef) dataSetDef;
        }
        return null;
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        consumeErrors(errors);
    }
    
    private void showFilePath() {
        fileURLRow.setVisible(false);
        filePathRow.setVisible(true);
    }

    private void showFileURL() {
        fileURLRow.setVisible(true);
        filePathRow.setVisible(false);
    }
    
    public boolean isUsingFilePath() {
        return filePathRow.isVisible();   
    }

    public boolean isUsingFileURL() {
        return fileURLRow.isVisible();
    }
    
    public void clear() {
        super.clear();
        filePath.clear();
        fileURL.clear();
        separatorChar.clear();
        quoteChar.clear();
        escapeChar.clear();
        datePattern.clear();
        numberPattern.clear();
    }
    
}
