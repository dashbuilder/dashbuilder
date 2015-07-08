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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.resources.i18n.DateIntervalTypeConstants;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Row;

import javax.enterprise.context.Dependent;
import java.util.Arrays;
import java.util.List;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing data set backend cache policy, client cache policy and the refresh policy settings.</p>
 *
 * <p>NOTE: The <code>refreshTime</code> is not bind directly to the data set definiton instance, as this editor uses two widgets for editing the quantity and the interval type..</p>
 * 
 * @since 0.3.0 
 */
@Dependent
public class DataSetAdvancedAttributesEditor extends AbstractDataSetDefEditor implements DataSetDefEditor {
    
    private static final double DEFAULT_REFRESH_QUANTITY = 1;
    private static final DateIntervalType DEFAULT_INTERVAL_TYPE = DateIntervalType.HOUR;

    private static List<DateIntervalType> ALLOWED_TYPES = Arrays.asList(
            DateIntervalType.SECOND,
            DateIntervalType.MINUTE,
            DateIntervalType.HOUR,
            DateIntervalType.DAY,
            DateIntervalType.MONTH,
            DateIntervalType.YEAR);

    interface DataSetAdvancedAttributesEditorBinder extends UiBinder<Widget, DataSetAdvancedAttributesEditor> {}
    private static DataSetAdvancedAttributesEditorBinder uiBinder = GWT.create(DataSetAdvancedAttributesEditorBinder.class);

    @UiField
    FlowPanel advancedAttributesPanel;
    
    /* **************** BACKEND CACHE *************** */
    
    @UiField
    Row backendCacheRow;
    
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
    @Ignore
    ValueBoxEditorDecorator<Integer> attributeRefreshInterval;

    @UiField
    @Ignore
    ListBox intervalType;

    @UiField
    @Ignore
    CheckBox onStaleCheckbox;

    final TriangleSlider backendCacheSlider = createSlider(10000, 200);
    final TriangleSlider clientCacheSlider = createSlider(4096, 200);
    private boolean isEditMode;

    public DataSetAdvancedAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        // Refresh interval quantity.
        attributeRefreshInterval.addValueChangeHandler(refreshTimeQuantityValueChangeHandler);
        
        // Refresh interval type button values.
        for (final DateIntervalType dateInterval : ALLOWED_TYPES) {
            final String s = getIntervalTypeText(dateInterval);
            intervalType.addItem(s);
        }
        intervalType.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                switchRefresh(true);
            }
        });
        onStaleCheckbox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                switchRefresh(true);
            }
        });

        // Configure and add sliders.
        attributeMaxRowsSliderPanel.add(backendCacheSlider);
        backendCacheSlider.addBarValueChangedHandler(backendCacheSliderHandler);

        attributeMaxBytesSliderPanel.add(clientCacheSlider);
        clientCacheSlider.addBarValueChangedHandler(clientCacheSliderHandler);
        
        // Configure sliders value binding with editors.
        attributeMaxRows.addValueChangeHandler(attributeMaxRowsChangeHandler);
        attributeMaxBytes.addValueChangeHandler(attributeMaxBytesChangeHandler);

        // Enable or disable editors based on status editor.
        attributeClientCacheStatus.addValueChangeHandler(attributeClientCacheStatusHandler);
        attributeBackendCacheStatus.addValueChangeHandler(attributeBackendCacheStatusHandler);
        attributeRefreshStatus.addValueChangeHandler(refreshStatusHandler);
        
    }
    
    private void switchRefresh(final boolean on) {
        if (dataSetDef != null) {
            if (on) {
                Integer quantity = attributeRefreshInterval.asEditor().getValue();
                if (quantity != null) {
                    DateIntervalType type = getSelectedIntervalType();
                    String rTime = (int) quantity + type.name();
                    dataSetDef.setRefreshTime(rTime);
                }
                dataSetDef.setRefreshAlways(!onStaleCheckbox.getValue());
            } else {
                dataSetDef.setRefreshTime(null);
            }
        }
        setRefreshUIValues(on);
    }

    public boolean isShowBackendCache(final DataSetDef def) {
        return def != null && def.getProvider() != null
                && ( !DataSetProviderType.BEAN.equals(def.getProvider())
                && !DataSetProviderType.CSV.equals(def.getProvider() ));
    }

    private DateIntervalType getSelectedIntervalType() {
        return ALLOWED_TYPES.get(intervalType.getSelectedIndex());
    }

    private int getIntervalTypeIndex(DateIntervalType type) {
        for (int i=0; i<ALLOWED_TYPES.size(); i++) {
            if (ALLOWED_TYPES.get(i).equals(type)) {
                return i;
            }
        }
        return 0;
    }

    private final ValueChangeHandler<Integer> refreshTimeQuantityValueChangeHandler = new ValueChangeHandler<Integer>() {
        @Override
        public void onValueChange(ValueChangeEvent<Integer> event) {
            switchRefresh(true);
        }
    };
    
    private final ValueChangeHandler<Boolean> attributeClientCacheStatusHandler = new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            attributeMaxBytes.setEnabled(event.getValue());
        }
    };

    private final ValueChangeHandler<Boolean> attributeBackendCacheStatusHandler = new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            attributeMaxRows.setEnabled(event.getValue());
        }
    };

    private final ValueChangeHandler<Boolean> refreshStatusHandler = new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            switchRefresh(event.getValue());
        }
    };
    
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

        // Special handling for BEAN and CSV types, they do not support disabling the backend cache.
        if (isShowBackendCache(dataSetDef)) backendCacheRow.setVisible(true);
        else backendCacheRow.setVisible(false);
        
        // Values for boolean editors.
        attributeMaxRows.setEnabled(dataSetDef.isCacheEnabled());
        attributeMaxBytes.setEnabled(dataSetDef.isPushEnabled());
        final boolean isRefreshOn = dataSetDef.getRefreshTime() != null;
        attributeRefreshStatus.setValue(isRefreshOn);
        setRefreshUIValues(isRefreshOn);
    }
    
    private void setRefreshUIValues(final boolean refreshEnabled) {
        attributeRefreshInterval.setEnabled(refreshEnabled);
        intervalType.setEnabled(refreshEnabled);
        onStaleCheckbox.setEnabled(refreshEnabled);

        if (dataSetDef != null && refreshEnabled) {
            // Interval quantity and type drop down.
            if (dataSetDef.getRefreshTime() != null) {
                final double quantity = dataSetDef.getRefreshTimeAmount().getQuantity();
                final DateIntervalType dType = dataSetDef.getRefreshTimeAmount().getType();
                attributeRefreshInterval.asEditor().setValue((int) quantity);
                intervalType.setSelectedIndex(getIntervalTypeIndex(dType));
                onStaleCheckbox.setValue(!dataSetDef.isRefreshAlways());
            } else {
                attributeRefreshInterval.asEditor().setValue((int) DEFAULT_REFRESH_QUANTITY);
                intervalType.setSelectedIndex(getIntervalTypeIndex(DEFAULT_INTERVAL_TYPE));
                onStaleCheckbox.setValue(!dataSetDef.isRefreshAlways());
            }
        }
    }
    
    private String getIntervalTypeText(final DateIntervalType type) {
        if (type == null) return null;
        return DateIntervalTypeConstants.INSTANCE.getString(type.name());
    }

    private TriangleSlider createSlider(final int maxValue, final int width) {
        TriangleSlider slider = new TriangleSlider(maxValue, width, true);
        slider.drawMarks("white", 6);
        slider.setMinMarkStep(3);
        slider.setNotSelectedInFocus();

        return slider;
    }

    public void clear() {
        super.clear();
        attributeBackendCacheStatus.clear();
        attributeMaxRows.clear();
        attributeClientCacheStatus.clear();
        attributeMaxBytes.clear();
        attributeRefreshStatus.clear();
    }
}
