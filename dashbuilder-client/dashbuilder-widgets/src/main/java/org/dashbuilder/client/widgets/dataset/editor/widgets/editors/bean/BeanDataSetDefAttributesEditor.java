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
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors.bean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.codehaus.jackson.map.deser.BeanDeserializerFactory;
import org.dashbuilder.common.client.validation.editors.MapEditor;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.validation.editors.BeanDataSetDefEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.AbstractDataSetDefEditor;
import org.dashbuilder.dataset.def.BeanDataSetDef;

import javax.enterprise.context.Dependent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing bean provider specific attributes.</p>
 */
@Dependent
public class BeanDataSetDefAttributesEditor extends AbstractDataSetDefEditor implements BeanDataSetDefEditor {
    
    interface BeanDataSetDefAttributesEditorBinder extends UiBinder<Widget, BeanDataSetDefAttributesEditor> {}
    private static BeanDataSetDefAttributesEditorBinder uiBinder = GWT.create(BeanDataSetDefAttributesEditorBinder.class);

    @UiField
    FlowPanel beanAttributesPanel;

    @UiField
    ValueBoxEditorDecorator<String> generatorClass;

    @UiField
    MapEditor<String, String> paramaterMap;

    private boolean isEditMode;

    private final MapEditor.ValueAddEventHandler parameterMapAddHandler = new MapEditor.ValueAddEventHandler() {
        @Override
        public void onValueAdd(MapEditor.ValueAddEvent event) {
            final String key = event.getKey();
            final String value = event.getValue();
            Map<String, String> parameterMap = getDataSetDef().getParamaterMap();
            if (parameterMap == null) {
                parameterMap = new LinkedHashMap<String, String>();
                getDataSetDef().setParamaterMap(parameterMap);
            }
            // Update the parameter map.
            parameterMap.put(key, value);
            
            // Redraw the editor.
            BeanDataSetDefAttributesEditor.this.paramaterMap.redraw();
        }
    };

    private final MapEditor.KeyModifiedEventHandler parameterMapKeyModifiedHandler = new MapEditor.KeyModifiedEventHandler() {
        @Override
        public void onKeyModified(MapEditor.KeyModifiedEvent event) {
            final String last = event.getLast();
            final String value = event.getValue();
            final int index = event.getIndex();

            // If key has changed, remove old value pair.
            String newValue = "";
            if (last != null) {
                newValue = getDataSetDef().getParamaterMap().remove(last);
            }

            // Update the parameter map.
            getDataSetDef().getParamaterMap().put(value, newValue);

            // Redraw the editor.
            BeanDataSetDefAttributesEditor.this.paramaterMap.redraw();
        }
    };
    
    private final MapEditor.ValueModifiedEventHandler parameterMapValueModifiedHandler = new MapEditor.ValueModifiedEventHandler() {
        @Override
        public void onValueModified(MapEditor.ValueModifiedEvent event) {
            final String last = event.getLast();
            final String value = event.getValue();
            final int index = event.getIndex();
            
            // Look for the key object.
            final String key = getKeyParameter(index);
            
            // Update the parameter map.
            getDataSetDef().getParamaterMap().put(key, value);

            // Redraw the editor.
            BeanDataSetDefAttributesEditor.this.paramaterMap.redraw();
        }
    };
    
    private BeanDataSetDef getDataSetDef() {
        return (BeanDataSetDef) dataSetDef;
    }
    
    private String getKeyParameter(final int index) {
        if (getDataSetDef().getParamaterMap() != null && !getDataSetDef().getParamaterMap().isEmpty() && index > -1) {
            int x = 0;
            for (Map.Entry<String, String> entry : getDataSetDef().getParamaterMap().entrySet()) {
                if (index == x) return entry.getKey();
                x++;
            }
            
        }
        return null;        
    }
    
    public BeanDataSetDefAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        paramaterMap.addValueAddEventHandler(parameterMapAddHandler);
        paramaterMap.addKeyModifiedEventHandler(parameterMapKeyModifiedHandler);
        paramaterMap.addValueModifiedEventHandler(parameterMapValueModifiedHandler);
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
}
