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

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FileUpload;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.AbstractDataSetDefEditor;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.uuid.ClientUUIDGenerator;
import org.dashbuilder.dataset.client.validation.editors.CSVDataSetDefEditor;
import org.dashbuilder.dataset.uuid.UUIDGenerator;

import javax.enterprise.context.Dependent;
import java.util.List;

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
    ControlGroup filePathErrorPanel;
    
    @UiField
    @Ignore
    FormPanel csvFileFormPanel;
    
    @UiField
    Tooltip filePathErrorTooltip;
    
    @UiField(provided = true)
    FileUpload filePath;

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

        filePath = new FileUpload() {
            @Override
            public void showErrors(List<EditorError> errors) {
                super.showErrors(errors);
                if(errors != null && !errors.isEmpty()) {
                    for (EditorError error : errors) {
                        if(error.getEditor() == this) {
                            error.setConsumed(false);
                        }
                    }
                }
            }

            @Override
            protected void setErrorLabelText(String errorMessage) {
                filePathErrorTooltip.setText(errorMessage);
                filePathErrorTooltip.reconfigure();
            }
        };
        
        initWidget(uiBinder.createAndBindUi(this));

        // Configure file upload error displaying.
        filePath.setControlGroup(filePathErrorPanel);
        filePath.setErrorLabel(filePathErrorTooltip.asWidget());
        
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
        
        // File upload form.
        filePath.addChangeHandler(filePathChangeHandler);
        
        // By default use file URL
        showFileURL();
    }
    
    public void setSubmitCompleteHandler(FormPanel.SubmitCompleteHandler submitCompleteHandler) {
        csvFileFormPanel.addSubmitCompleteHandler(submitCompleteHandler);
    }

    private final ChangeHandler filePathChangeHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
            csvFileFormPanel.setAction(getUploadFormAction());
            // csvFileFormPanel.submit();
        }
    };
    
    private String getUploadFormAction() {
        final String uploadFileUrl = DataSetClientServices.get().getUploadServletUrl()+"?scheme=file&path=";
        final String filePath = DataSetClientServices.get().getTempFilePath(ClientUUIDGenerator.get().newUuid() + ".csv");
        return uploadFileUrl + filePath;
    }
    
    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
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
    
}
