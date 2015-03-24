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

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.BooleanSwitchEditorDecorator;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
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

    /* **************** BACKEND CACHE *************** */
    @UiField
    @Path("cacheEnabled")
    BooleanSwitchEditorDecorator attributeBackendCacheStatus;

    @UiField
    @Path("cacheMaxRows")
    ValueBoxEditorDecorator<Integer> attributeMaxRows;

    /* **************** CLIENT CACHE *************** */
    @UiField
    @Path("pushEnabled")
    BooleanSwitchEditorDecorator attributeClientCacheStatus;

    @UiField
    @Path("pushMaxSize")
    ValueBoxEditorDecorator<Integer> attributeMaxBytes;

    /* **************** REFRESH POLICY *************** */
    @UiField
    @Path("refreshAlways")
    BooleanSwitchEditorDecorator attributeRefreshStatus;

    @UiField
    @Path("refreshTime")
    ValueBoxEditorDecorator<String> attributeRefreshInterval;
    
    private boolean isEditMode;

    public DataSetAdvancedAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));
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
