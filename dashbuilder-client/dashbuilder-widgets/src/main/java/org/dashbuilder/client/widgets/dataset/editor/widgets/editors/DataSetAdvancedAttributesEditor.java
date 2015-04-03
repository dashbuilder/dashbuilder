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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.BooleanSwitchEditor;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.common.client.widgets.slider.TriangleSlider;
import org.dashbuilder.common.client.widgets.slider.event.BarValueChangedEvent;
import org.dashbuilder.common.client.widgets.slider.event.BarValueChangedHandler;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.group.DateIntervalType;

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
    
    /* **************** BACKEND CACHE *************** */
    @UiField
    @Path("cacheEnabled")
    BooleanSwitchEditor attributeBackendCacheStatus;

    @UiField
    @Path("cacheMaxRows")
    ValueBoxEditorDecorator<Integer> attributeMaxRows;

    @UiField
    FlowPanel attributeMaxRowsSliderPanel;

    /* **************** CLIENT CACHE *************** */
    @UiField
    @Path("pushEnabled")
    BooleanSwitchEditor attributeClientCacheStatus;

    @UiField
    @Path("pushMaxSize")
    ValueBoxEditorDecorator<Integer> attributeMaxBytes;

    @UiField
    FlowPanel attributeMaxBytesSliderPanel;

    /* **************** REFRESH POLICY *************** */
    @UiField
    @Ignore
    BooleanSwitchEditor attributeRefreshStatus;

    @UiField
    @Path("refreshTime")
    ValueBoxEditorDecorator<String> attributeRefreshInterval;

    @UiField
    @Ignore
    DropdownButton intervalType;
    
    @UiField
    CheckBox refreshAlways;

    final TriangleSlider backendCacheSlider = createSlider(10000, "200px");
    final TriangleSlider clientCacheSlider = createSlider(4096, "200px");
    private boolean isEditMode;

    public DataSetAdvancedAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        // Refresh interval type button values.
        final DateIntervalType[] dateIntervals = DateIntervalType.values();
        for (DateIntervalType dateInterval : dateIntervals) {
            final NavLink link = new NavLink(dateInterval.name());
            intervalType.add(link);
        }
                
        // Configure and add sliders.
        attributeMaxRowsSliderPanel.add(backendCacheSlider);
        backendCacheSlider.addBarValueChangedHandler(backendCacheSliderHandler);

        attributeMaxBytesSliderPanel.add(clientCacheSlider);
        clientCacheSlider.addBarValueChangedHandler(clientCacheSliderHandler);
        
        // Configure sliders value binding with editors.
        attributeMaxRows.addValueChangeHandler(attributeMaxRowsChangeHandler);
        attributeMaxBytes.addValueChangeHandler(attributeMaxBytesChangeHandler);

        // Enable or disable editors based on status editor.
        attributeClientCacheStatus.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                // TODO: not working.. and move as a private final class member.
                attributeMaxBytes.setEnabled(event.getValue());
            }
        });
        
    }
    
    private final BarValueChangedHandler backendCacheSliderHandler = new BarValueChangedHandler() {
        @Override
        public void onBarValueChanged(BarValueChangedEvent event) {
            attributeMaxRows.asEditor().setValue(event.getValue());
        }
    };

    private final BarValueChangedHandler clientCacheSliderHandler = new BarValueChangedHandler() {
        @Override
        public void onBarValueChanged(BarValueChangedEvent event) {
            attributeMaxBytes.asEditor().setValue(event.getValue());
        }
    };
    
    private final ValueChangeHandler<Integer> attributeMaxRowsChangeHandler = new ValueChangeHandler<Integer>() {
        @Override
        public void onValueChange(ValueChangeEvent<Integer> event) {
            // Set slider values manually, as sliders are not editor components.
            backendCacheSlider.setValue(event.getValue());
        }
    };

    private final ValueChangeHandler<Integer> attributeMaxBytesChangeHandler = new ValueChangeHandler<Integer>() {
        @Override
        public void onValueChange(ValueChangeEvent<Integer> event) {
            // Set slider values manually, as sliders are not editor components.
            clientCacheSlider.setValue(event.getValue());
        }
    };

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

    @Override
    public void set(DataSetDef dataSetDef) {
        super.set(dataSetDef);
        init();
    }
    
    private void init() {
        // Set slider values manually, as sliders are not editor components.
        backendCacheSlider.setValue(dataSetDef.getCacheMaxRows());
        clientCacheSlider.setValue(dataSetDef.getPushMaxSize());

        attributeMaxBytes.setEnabled(dataSetDef.isPushEnabled());
    }

    private TriangleSlider createSlider(final int maxValue, final String width) {
        TriangleSlider slider = new TriangleSlider(maxValue, width, true);
        slider.drawMarks("white", 6);
        slider.setMinMarkStep(3);
        slider.setNotSelectedInFocus();

        return slider;
    }
}
