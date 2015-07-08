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
package org.dashbuilder.client.widgets.dataset.editor.widgets.explorer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import org.dashbuilder.client.widgets.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientImages;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;

import javax.enterprise.context.Dependent;

/**
 * <p>Default view for DataSetSummary presenter.</p>
 * 
 * @since 0.3.0
 */
@Dependent
public class DataSetSummaryView extends Composite implements DataSetSummary.View {

    interface DataSetSummaryViewBinder extends UiBinder<Row, DataSetSummaryView> {}
    private static DataSetSummaryViewBinder uiBinder = GWT.create(DataSetSummaryViewBinder.class);

    interface DataSetSummaryViewStyle extends CssResource {
    }

    @UiField
    DataSetSummaryViewStyle style;
    
    @UiField
    Span backendStatus;
    
    @UiField
    Icon backendStatusIcon;

    @UiField
    Span pushStatus;

    @UiField
    Icon pushEnabledIcon;

    @UiField
    Span refreshStatus;

    @UiField
    Icon refreshEnabledIcon;
    
    @UiField
    Image loadingSizeImage;
    
    @UiField
    Span sizePanelSpan;
    
    @UiField
    HTML estimatedSizeText;

    @UiField
    HTML estimatedRowsText;
    
    private DataSetSummary presenter;

    public DataSetSummaryView() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void init(final DataSetSummary presenter) {
        this.presenter = presenter;
    }

    @Override
    public DataSetSummary.View showStatusPanel(final Boolean backendCacheStatus, final Boolean pushEnabled, final Boolean _refreshStatus) {
        if (backendCacheStatus != null) {
            configureStatusIcon(backendStatusIcon, backendCacheStatus);
            backendStatus.setVisible(true);
        } else {
            backendStatus.setVisible(false);
        }
        if (pushEnabled != null) {
            configureStatusIcon(pushEnabledIcon, pushEnabled);
            pushStatus.setVisible(true);
        } else {
            pushStatus.setVisible(false);
        }
        if (_refreshStatus != null) {
            configureStatusIcon(refreshEnabledIcon, _refreshStatus);
            refreshStatus.setVisible(true);
        } else {
            refreshStatus.setVisible(false);
        }
        return this;
    }
    
    private void configureStatusIcon(final Icon icon, final boolean value) {
        final String t = value ? DataSetExplorerConstants.INSTANCE.enabled() : DataSetExplorerConstants.INSTANCE.disabled();
        icon.setType(value ? IconType.CHECK : IconType.REMOVE);
        icon.setTitle(t);
    }

    @Override
    public DataSetSummary.View showSizeLoadingPanel() {
        loadingSizeImage.setUrl(DataSetClientResources.INSTANCE.images().loadingIcon().getSafeUri());
        loadingSizeImage.setVisible(true);
        sizePanelSpan.setVisible(false);
        return this;
    }

    @Override
    public DataSetSummary.View showSizePanel(final String backendSizeRow, final String clientSizeKb) {
        estimatedRowsText.setText(backendSizeRow);
        estimatedSizeText.setText(clientSizeKb);
        loadingSizeImage.setVisible(false);
        sizePanelSpan.setVisible(true);
        return this;
    }

}
