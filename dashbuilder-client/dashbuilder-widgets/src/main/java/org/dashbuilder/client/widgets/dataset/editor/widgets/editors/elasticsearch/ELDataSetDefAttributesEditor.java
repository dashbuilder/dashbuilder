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
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors.elasticsearch;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.validation.client.impl.ConstraintViolationImpl;
import com.google.gwt.validation.client.impl.PathImpl;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.AbstractDataSetDefEditor;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.validation.editors.ELDataSetDefEditor;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.validation.DataSetValidationMessages;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing ElasticSearch provider specific attributes.</p>
 * 
 * <p>NOTE:</p>
 * <p>The members <code>index</code> and <code>type</code> are considered an array, but currently the EL provider implementation just support one value for index and type.</p>
 * <p>So <code>index</code> and <code>type</code> are not bind directly with the editor driver, these fields are edited in a manual way.</p> 
 *
 * @since 0.3.0 
 */
@Dependent
public class ELDataSetDefAttributesEditor extends AbstractDataSetDefEditor implements ELDataSetDefEditor {
    
    interface ELDataSetDefAttributesEditorBinder extends UiBinder<Widget, ELDataSetDefAttributesEditor> {}
    private static ELDataSetDefAttributesEditorBinder uiBinder = GWT.create(ELDataSetDefAttributesEditorBinder.class);

    @UiField
    FlowPanel elAttributesPanel;

    @UiField
    ValueBoxEditorDecorator<String> serverURL;

    @UiField
    ValueBoxEditorDecorator<String> clusterName;

    @UiField
    @Ignore
    ValueBoxEditorDecorator<String> index;

    @UiField
    @Ignore
    ValueBoxEditorDecorator<String> type;

    private boolean isEditMode;

    public ELDataSetDefAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        // Handle index unique value.
        index.addValueChangeHandler(indexValueChangeHandler);

        // Handle type unique value.
        type.addValueChangeHandler(typeValueChangeHandler);
    }

    /**
     * <p>As <code>index</code> and <code>type</code> are not bind directly with the editor driver, this method is provided to validate and save those attributes.</p> 
     */
    public final void save() {
        saveIndex();
        saveType();
    }

    private final ValueChangeHandler<String> indexValueChangeHandler = new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
            String _index = event.getValue();
            saveIndex(_index);
        }
    };

    private final ValueChangeHandler<String> typeValueChangeHandler = new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
            String _type = event.getValue();
            saveType(_type);
        }
    };

    private final void saveIndex() {
        saveIndex(index.asEditor().getValue());    
    }
    
    private final void saveIndex(final String _index) {
        if (_index == null || _index.trim().length() == 0) {
            // Show errors.
            index.showErrors(createErrorList("index", DataSetValidationMessages.INSTANCE.dataSetApi_elDataSetDef_index_notNull(), index.asEditor()));
            // Add violation.
            createViolation("index", DataSetValidationMessages.INSTANCE.dataSetApi_elDataSetDef_index_notNull(), index.asEditor());
        } else {
            // Set index value.
            final List<String> l = new LinkedList<String>();
            l.add(_index);
            getDataSetDef().setIndex(l);
        }
        
    }
    
    private void createViolation(final String path, final String message, final Editor<?> editor) {
        final Collection<ConstraintViolation<?>> violations = getViolations();
        final ConstraintViolation<?> violation = ConstraintViolationImpl.builder().setPropertyPath(new PathImpl().append(path)).setMessage(message).build();
        violations.add(violation);
    }

    @Override
    public Collection<ConstraintViolation<?>> getViolations() {
        if (super.getViolations() == null) this.violations = new LinkedList<ConstraintViolation<?>>();
        return (Collection<ConstraintViolation<?>>) this.violations;
    }

    private final void saveType() {
        saveType(type.asEditor().getValue());
    }

    private final void saveType(final String _type) {
        if (_type == null || _type.trim().length() == 0) {
            // Show errors.
            type.showErrors(createErrorList("type", DataSetValidationMessages.INSTANCE.dataSetApi_elDataSetDef_type_notNull(), type.asEditor()));
            // Create the violation.
            createViolation("type", DataSetValidationMessages.INSTANCE.dataSetApi_elDataSetDef_type_notNull(), type.asEditor());
        } else {
            // Set index value.
            final List<String> l = new LinkedList<String>();
            l.add(_type);
            getDataSetDef().setType(l);
        }
    }
    
    private List<EditorError> createErrorList(final String path, final String message, final Editor<?> editor) {
        final EditorError editorError = createError(path, message, editor);
        final List<EditorError> result = new LinkedList<EditorError>();
        result.add(editorError);
        return result;
    }
    
    private EditorError createError(final String path, final String message, final Editor<?> editor) {
        return new EditorError() {
            
            private boolean consumed = false;
            
            @Override
            public String getAbsolutePath() {
                return path;
            }

            @Override
            public Editor<?> getEditor() {
                return editor;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public String getPath() {
                return null;
            }

            @Override
            public Object getUserData() {
                return null;
            }

            @Override
            public Object getValue() {
                return null;
            }

            @Override
            public boolean isConsumed() {
                return consumed;
            }

            @Override
            public void setConsumed(boolean consumed) {
                this.consumed = consumed;
            }
        };
    }
    
    private ElasticSearchDataSetDef getDataSetDef() {
        return (ElasticSearchDataSetDef) dataSetDef;        
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

    public void clear() {
        super.clear();
        serverURL.clear();
        clusterName.clear();
        index.clear();
        type.clear();
    }
}
