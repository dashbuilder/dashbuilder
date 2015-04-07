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
import com.github.gwtbootstrap.client.ui.base.StyleHelper;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.validation.editors.CSVDataSetDefEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.AbstractDataSetDefEditor;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing CSV provider specific attributes.</p>
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
        
        // By default use file URL
        showFileURL();
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
