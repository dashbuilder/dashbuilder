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
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.BooleanSwitchEditor;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.common.client.widgets.slider.HorizontalSlider;
import org.dashbuilder.common.client.widgets.slider.TriangleSlider;
import org.dashbuilder.common.client.widgets.slider.event.BarValueChangedEvent;
import org.dashbuilder.common.client.widgets.slider.event.BarValueChangedHandler;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing data set backend cache policy, client cache policy and the refresh policy settings.</p>
 */
@Dependent
public class DataSetAdvancedAttributesEditor extends AbstractDataSetDefEditor implements DataSetDefEditor {
    
    private static final int DEFAULT_CACHE_MAX_ROWS = -1;
    private static final int DEFAULT_CACHE_MAX_BYTES = -1;
    private static final long DEFAULT_REFRESH_INTERVAL = -1;

    interface DataSetAdvancedAttributesEditorBinder extends UiBinder<Widget, DataSetAdvancedAttributesEditor> {}
    private static DataSetAdvancedAttributesEditorBinder uiBinder = GWT.create(DataSetAdvancedAttributesEditorBinder.class);

    @UiField
    FlowPanel advancedAttributesPanel;

    @UiField
    FlowPanel backendCachePanel;

    /* **************** BACKEND CACHE *************** */
    @UiField
    @Path("cacheEnabled")
    BooleanSwitchEditor attributeBackendCacheStatus;

    @UiField
    @Path("cacheMaxRows")
    ValueBoxEditorDecorator<Integer> attributeMaxRows;

    /* **************** CLIENT CACHE *************** */
    @UiField
    @Path("pushEnabled")
    BooleanSwitchEditor attributeClientCacheStatus;

    @UiField
    @Path("pushMaxSize")
    ValueBoxEditorDecorator<Integer> attributeMaxBytes;

    /* **************** REFRESH POLICY *************** */
    @UiField
    @Path("refreshAlways")
    BooleanSwitchEditor attributeRefreshStatus;

    @UiField
    @Path("refreshTime")
    ValueBoxEditorDecorator<String> attributeRefreshInterval;
    
    private boolean isEditMode;

    public DataSetAdvancedAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        TriangleSlider backendCacheSlider = createSlider(10000, "300px");
        backendCachePanel.add(backendCacheSlider);
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

    private TriangleSlider createSlider(final int maxValue, final String width) {
        TriangleSlider slider = new TriangleSlider(maxValue, width, true);
        slider.addBarValueChangedHandler(new BarValueChangedHandler() {
            @Override
            public void onBarValueChanged(BarValueChangedEvent event) {
                GWT.log("slider value = " + event.getValue());
            }
        });
        slider.drawMarks("white", 6);
        slider.setMinMarkStep(3);
        slider.setNotSelectedInFocus();

        return slider;
    }
}
