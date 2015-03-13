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
package org.dashbuilder.dataset.client.widgets.editors;

import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.client.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataset.client.widgets.DataSetEditor;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing data set backend cache policy, client cache policy and the refresh policy settings.</p>
 */
@Dependent
public class DataSetAdvancedAttributesEditor extends Composite implements DataSetEditor.View {
    
    private static final int DEFAULT_CACHE_MAX_ROWS = -1;
    private static final int DEFAULT_CACHE_MAX_BYTES = -1;
    private static final long DEFAULT_REFRESH_INTERVAL = -1;

    interface DataSetAdvancedAttributesEditorBinder extends UiBinder<Widget, DataSetAdvancedAttributesEditor> {}
    private static DataSetAdvancedAttributesEditorBinder uiBinder = GWT.create(DataSetAdvancedAttributesEditorBinder.class);

    @UiField
    HorizontalPanel advancedAttributesPanel;

    /* **************** BACKEND CACHE *************** */
    @UiField
    Label attributeBackendCacheStatus;

    @UiField
    TextBox attributeMaxRows;

    /* **************** CLIENT CACHE *************** */
    @UiField
    Label attributeClientCacheStatus;

    @UiField
    TextBox attributeMaxBytes;

    /* **************** REFRESH POLICY *************** */
    @UiField
    Label attributeRefreshStatus;

    @UiField
    TextBox attributeRefreshInterval;

    private DataSetDef dataSetDef;
    private boolean isEditMode;

    public DataSetAdvancedAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void set(DataSetDef dataSetDef) {
        this.dataSetDef = dataSetDef;
    }

    @Override
    public Widget show(final boolean isEditMode) {
        this.isEditMode = isEditMode;

        // Clear the widget.
        clearScreen();

        // Backend cache.
        buildBackendCacheAttributes();

        // Client cache.
        buildClientCacheAttributes();
        
        // Refresh policy.
        buildRefreshPolicyAttributes();
        
        return asWidget();
    }
    
    private void buildBackendCacheAttributes() {
        boolean isCacheEnabled = false;
        int cacheMaxRows = DEFAULT_CACHE_MAX_ROWS;
        if (dataSetDef != null ) {
            isCacheEnabled = dataSetDef.isCacheEnabled();
            cacheMaxRows = dataSetDef.getCacheMaxRows();
        }

        if (isCacheEnabled) statusLabelON(attributeBackendCacheStatus);
        else statusLabelOFF(attributeBackendCacheStatus);
        if (isEditMode) {
            attributeBackendCacheStatus.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    statusLabelSwitchValue(attributeBackendCacheStatus);
                    final boolean isOn = isStatusLabelON(attributeBackendCacheStatus);
                    dataSetDef.setCacheEnabled(isOn);
                    attributeMaxRows.setEnabled(isOn);
                }
            });
        }
        attributeMaxRows.setEnabled(isEditMode && isCacheEnabled);
        attributeMaxRows.setValue(Integer.toString(cacheMaxRows));
    }

    private void buildClientCacheAttributes() {
        boolean isPushEnabled = false;
        int cacheMaxBytes = DEFAULT_CACHE_MAX_BYTES;
        if (dataSetDef != null ) {
            isPushEnabled = dataSetDef.isPushEnabled();
            cacheMaxBytes = dataSetDef.getPushMaxSize();
        }

        if (isPushEnabled) statusLabelON(attributeClientCacheStatus);
        else statusLabelOFF(attributeClientCacheStatus);
        if (isEditMode) {
            attributeClientCacheStatus.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    statusLabelSwitchValue(attributeClientCacheStatus);
                    final boolean isOn = isStatusLabelON(attributeClientCacheStatus);
                    dataSetDef.setPushEnabled(isOn);
                    attributeMaxBytes.setEnabled(isOn);
                }
            });
        }
        attributeMaxBytes.setEnabled(isEditMode && isPushEnabled);
        attributeMaxBytes.setValue(Integer.toString(cacheMaxBytes));
    }

    private void buildRefreshPolicyAttributes() {
        boolean isRefreshEnabled = false;
        long refreshInterval = DEFAULT_REFRESH_INTERVAL;
        if (dataSetDef != null ) {
            isRefreshEnabled = dataSetDef.isRefreshAlways();
            if (dataSetDef.getRefreshTimeAmount() != null) refreshInterval = dataSetDef.getRefreshTimeAmount().getQuantity();
        }

        if (isRefreshEnabled) statusLabelON(attributeRefreshStatus);
        else statusLabelOFF(attributeRefreshStatus);
        if (isEditMode) {
            attributeRefreshStatus.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    statusLabelSwitchValue(attributeRefreshStatus);
                    final boolean isOn = isStatusLabelON(attributeRefreshStatus);
                    dataSetDef.setRefreshAlways(isOn);
                    attributeRefreshInterval.setEnabled(isOn);

                }
            });
        }
        attributeRefreshInterval.setEnabled(isEditMode && isRefreshEnabled);
        attributeRefreshInterval.setValue(Double.toString(refreshInterval));
    }
    
    @Override
    public void hide() {
        advancedAttributesPanel.setVisible(false);
    }

    @Override
    public void clear() {
        clearScreen();
        clearStatus();
    }
    
    private void clearScreen() {
        // Backend cache.
        statusLabelOFF(attributeBackendCacheStatus);
        attributeMaxRows.setValue(Integer.toString(DEFAULT_CACHE_MAX_ROWS));

        // Client cache.
        statusLabelOFF(attributeClientCacheStatus);
        attributeMaxBytes.setValue(Integer.toString(DEFAULT_CACHE_MAX_BYTES));

        // Refresh policy.
        statusLabelOFF(attributeRefreshStatus);
        attributeRefreshInterval.setValue(Double.toString(DEFAULT_REFRESH_INTERVAL));
    }
    
    private void clearStatus() {
        this.dataSetDef = null;
    }

    private void statusLabelSwitchValue(final Label label) {
        if (isStatusLabelON(label)) statusLabelOFF(label);
        else statusLabelON(label);
    }

    private void statusLabelON(final Label label) {
        label.setText(DataSetEditorConstants.INSTANCE.on());
        label.setType(LabelType.SUCCESS);
    }

    private void statusLabelOFF(final Label label) {
        label.setText(DataSetEditorConstants.INSTANCE.off());
        label.setType(LabelType.WARNING);
    }

    private boolean isStatusLabelON(final Label label) {
        return DataSetEditorConstants.INSTANCE.on().equals(label.getText());
    }


}
