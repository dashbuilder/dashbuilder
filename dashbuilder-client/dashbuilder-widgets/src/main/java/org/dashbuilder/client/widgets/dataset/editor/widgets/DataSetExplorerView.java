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
package org.dashbuilder.client.widgets.dataset.editor.widgets;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.event.ShowEvent;
import com.github.gwtbootstrap.client.ui.event.ShowHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.EditDataSetEvent;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.EditDataSetEventHandler;
import org.dashbuilder.client.widgets.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.common.client.widgets.SlidingPanel;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientImages;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;
import java.util.*;

/**
 * <p>Default view for DataSetExplorer presenter.</p> 
 * @since 0.3.0
 */
@Dependent
public class DataSetExplorerView extends Composite implements DataSetExplorer.View {

    private final static String WHITESPACE = " ";
    private final static String ICONS_SIZE = "16px";
    private final static String ESTIMATIONS_FORMAT = "#,###.0";
    
    private final static NumberFormat rowsFormat = NumberFormat.getFormat("##0");

    interface DataSetExplorerViewBinder extends UiBinder<Widget, DataSetExplorerView> {}
    private static DataSetExplorerViewBinder uiBinder = GWT.create(DataSetExplorerViewBinder.class);

    interface DataSetExplorerViewStyle extends CssResource {
        String columnsPanel();
        String statusPanel();
        String statusIcon();
        String statusText();
        String statusTextTitle();
        String estimationsPanel();
        String buttonsPanel();
        String button();
        String slidingPanel();
        String deleteText();
    }

    @UiField
    DataSetExplorerViewStyle style;

    @UiField
    Modal errorPanel;

    @UiField
    Button errorPanelButton;

    @UiField
    HTML errorType;

    @UiField
    HTML errorMessage;

    @UiField
    HTML errorCause;

    @UiField
    Row errorTypeRow;

    @UiField
    Row errorMessageRow;

    @UiField
    Row errorCauseRow;
    
    @UiField
    Accordion dataSetsAccordion;
    
    @UiField
    Label label;
    
    private Set<DataSetDef> dataSets;

    private final ClickHandler errorPanelButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            hideError();
        }
    };
    
    public DataSetExplorerView() {
        initWidget(uiBinder.createAndBindUi(this));
        dataSets = new LinkedHashSet<DataSetDef>();
        errorPanelButton.addClickHandler(errorPanelButtonHandler);
    }

    @Override
    public void set(Collection<DataSetDef> dataSetDefs) {
        this.dataSets = new LinkedHashSet<DataSetDef>(dataSetDefs);
    }

    @Override
    public boolean add(DataSetDef dataSetDef) {
        return dataSets.add(dataSetDef);
    }

    @Override
    public void remove(DataSetDef dataSetDef) {
        if (dataSets != null && dataSetDef != null && dataSetDef.getUUID() != null) {
            remove(dataSetDef.getUUID());
        }
    }
    
    @Override
    public boolean update(final DataSetDef oldDataSetDef, final DataSetDef newDataSetDef) {
        remove(oldDataSetDef.getUUID());
        return dataSets.add(newDataSetDef);
    }
    
    private void remove(final String uuid) {
        if (dataSets != null )
        {
            final Iterator<DataSetDef> it = dataSets.iterator();
            while (it.hasNext())
            {
                DataSetDef def = it.next();
                if (def.getUUID().equals(uuid)) it.remove();
            }
        }
    }

    @Override
    public void clear() {
        dataSets.clear();
        clearView();
    }
    
    private void clearView() {
        label.setText("");
        label.setVisible(false);
        dataSetsAccordion.clear();
    }

    @Override
    public void show(final DataSetExplorer.ShowDataSetDefCallback callback) {
        clearView();

        if (!dataSets.isEmpty()) {
            label.setVisible(false);
            for (DataSetDef dataSetDef : dataSets) {
                final AccordionGroup accordionGroup = buildDataSetAccordionGroup(dataSetDef, callback);
                dataSetsAccordion.add(accordionGroup);
            }
            
        } else {
            label.setText(DataSetExplorerConstants.INSTANCE.noDataSets());
            label.setVisible(true);
        }
        dataSetsAccordion.setVisible(true);
    }

    private AccordionGroup buildDataSetAccordionGroup(final DataSetDef dataSetDef, final DataSetExplorer.ShowDataSetDefCallback callback) {
        final AccordionGroup accordionGroup = new AccordionGroup();

        // Heading.
        accordionGroup.setHeading(dataSetDef.getName());
        // CollapseTrigger collapseTrigger = new CollapseTrigger();
        
        // Icon for provider type.
        final Image typeIcon = buildTypeIcon(dataSetDef);
        if (typeIcon != null) accordionGroup.addCustomTrigger(typeIcon);
        
        accordionGroup.addShowHandler(new ShowHandler() {
            @Override
            public void onShow(ShowEvent showEvent) {
                buildDescription(dataSetDef, accordionGroup, callback);
            }
        });
        
        return accordionGroup;
    } 
    
    private Image buildTypeIcon(final DataSetDef dataSetDef) {
        Image typeIcon = null;
        switch (dataSetDef.getProvider()) {
            case BEAN:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().javaIcon32().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.bean());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.bean());
                break;
            case CSV:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().csvIcon32().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.csv());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.csv());
                break;
            case SQL:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().sqlIcon32().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.sql());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.sql());
                break;
            case ELASTICSEARCH:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().elIcon32().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.el());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.el());
                break;
        }
        typeIcon.setSize(ICONS_SIZE, ICONS_SIZE);
        return typeIcon;
        
    }
    
    private void buildDescription(final DataSetDef dataSetDef, final Panel parent, final DataSetExplorer.ShowDataSetDefCallback callback) {
        if (parent != null) {
            // Clear current details.
            parent.clear();
            
            final DataSetClientImages images = DataSetClientResources.INSTANCE.images(); 
            
            final HTML statusText = new HTML(DataSetExplorerConstants.INSTANCE.currentStatus());
            statusText.addStyleName(style.statusTextTitle());
            
            // Caches and refresh.
            
            final boolean isCacheEnabled = dataSetDef.isCacheEnabled();
            FlowPanel cachePanel = null;
            
            if (callback.isShowBackendCache(dataSetDef)) {
                cachePanel = new FlowPanel();
                final com.github.gwtbootstrap.client.ui.Image cacheEnabled = new com.github.gwtbootstrap.client.ui.Image(
                        isCacheEnabled ? images.okIconSmall() : images.cancelIconSmall());
                final String _cache = isCacheEnabled ? DataSetExplorerConstants.INSTANCE.enabled() : DataSetExplorerConstants.INSTANCE.disabled();
                cacheEnabled.setTitle(_cache);
                cacheEnabled.setAltText(_cache);
                cacheEnabled.addStyleName(style.statusIcon());
                final HTML cacheText = new HTML(DataSetExplorerConstants.INSTANCE.cache());
                cacheText.addStyleName(style.statusText());
                cachePanel.add(cacheEnabled);
                cachePanel.add(cacheText);
            }

            
            final boolean isPushEnabled = dataSetDef.isPushEnabled();
            final FlowPanel pushPanel = new FlowPanel();
            final com.github.gwtbootstrap.client.ui.Image pushEnabled = new com.github.gwtbootstrap.client.ui.Image(
                    isPushEnabled ? images.okIconSmall() : images.cancelIconSmall());
            final String _push = isPushEnabled ? DataSetExplorerConstants.INSTANCE.enabled() : DataSetExplorerConstants.INSTANCE.disabled();
            pushEnabled.setTitle(_push);
            pushEnabled.setAltText(_push);
            pushEnabled.addStyleName(style.statusIcon());
            final HTML pushText = new HTML(DataSetExplorerConstants.INSTANCE.push());
            pushText.addStyleName(style.statusText());
            pushPanel.add(pushEnabled);
            pushPanel.add(pushText);

            final boolean isRefreshEnabled = dataSetDef.getRefreshTime() != null;
            final FlowPanel refreshPanel = new FlowPanel();
            final com.github.gwtbootstrap.client.ui.Image refreshEnabled = new com.github.gwtbootstrap.client.ui.Image(
                    isRefreshEnabled ? images.okIconSmall() : images.cancelIconSmall());
            final String _refresh = isRefreshEnabled ? DataSetExplorerConstants.INSTANCE.enabled() : DataSetExplorerConstants.INSTANCE.disabled();
            refreshEnabled.setTitle(_refresh);
            refreshEnabled.setAltText(_refresh);
            refreshEnabled.addStyleName(style.statusIcon());
            final HTML refreshText = new HTML(DataSetExplorerConstants.INSTANCE.refresh());
            refreshText.addStyleName(style.statusText());
            refreshPanel.add(refreshEnabled);
            refreshPanel.add(refreshText);
        
            final FlowPanel statusPanel = new FlowPanel();
            statusPanel.addStyleName(style.statusPanel());
            statusPanel.add(statusText);
            if (cachePanel != null) statusPanel.add(cachePanel);
            statusPanel.add(pushPanel);
            statusPanel.add(refreshPanel);

            // Estimated rows and size.
            final FlowPanel estimationsPanel = new FlowPanel();
            estimationsPanel.addStyleName(style.estimationsPanel());
            
            // Add current size title.
            final HTML currentSizeText = new HTML(DataSetExplorerConstants.INSTANCE.currentSize());
            currentSizeText.addStyleName(style.statusTextTitle());
            estimationsPanel.add(currentSizeText);
            
            // Add the loading icon while performing the backend call to fetch metadata.
            final com.github.gwtbootstrap.client.ui.Image loadingIcon = new com.github.gwtbootstrap.client.ui.Image(DataSetClientResources.INSTANCE.images().loadingIcon().getSafeUri());
            loadingIcon.setTitle(DataSetExplorerConstants.INSTANCE.loading());
            loadingIcon.setAltText(DataSetExplorerConstants.INSTANCE.loading());
            estimationsPanel.add(loadingIcon);
            
            // Perform the backend call to fetch data set metadata.
            callback.getMetadata(dataSetDef, new DataSetMetadataCallback() {

                @Override
                public void callback(DataSetMetadata metadata) {
                    // Clear the loading texxt.
                    estimationsPanel.clear();
                    
                    // Show estimations.
                    final int estimatedSize = metadata.getEstimatedSize();
                    final int rowCount = metadata.getNumberOfRows();

                    // Add current size title.
                    final HTML currentSizeText = new HTML(DataSetExplorerConstants.INSTANCE.currentSize());
                    currentSizeText.addStyleName(style.statusTextTitle());
                    
                    // Add estimation values.
                    final HTML estimatedSizeText = new HTML(humanReadableByteCount(estimatedSize));
                    estimatedSizeText.addStyleName(style.statusText());
                    final HTML estimatedRowsText = new HTML(humanReadableRowCount(rowCount) + WHITESPACE + DataSetExplorerConstants.INSTANCE.rows());
                    estimatedRowsText.addStyleName(style.statusText());

                    // Add into parent container.
                    estimationsPanel.add(currentSizeText);
                    estimationsPanel.add(estimatedRowsText);
                    estimationsPanel.add(estimatedSizeText);
                }

                @Override
                public void notFound() {
                    error();
                    showError(DataSetExplorerConstants.INSTANCE.notFound());
                }

                @Override
                public boolean onError(DataSetClientServiceError error) {
                    error();
                    showError(error);
                    return false;
                }
                
                private void error() {
                    loadingIcon.setUrl(DataSetClientResources.INSTANCE.images().cancelIconSmall().getSafeUri());
                    loadingIcon.setTitle(DataSetExplorerConstants.INSTANCE.error());
                    loadingIcon.setAltText(DataSetExplorerConstants.INSTANCE.error());
                }
            });

            final FlowPanel columnsPanel = new FlowPanel();
            columnsPanel.addStyleName(style.columnsPanel());
            columnsPanel.add(statusPanel);
            columnsPanel.add(estimationsPanel);
            parent.add(columnsPanel);

            // Buttons
            final FlowPanel buttonsPanel = new FlowPanel();

            final com.github.gwtbootstrap.client.ui.Button editButton = new Button(DataSetExplorerConstants.INSTANCE.edit());
            final boolean isPublic = dataSetDef.isPublic();
            editButton.setEnabled(isPublic);
            editButton.addStyleName(style.button());

            editButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    fireEvent(new EditDataSetEvent(dataSetDef));
                }
            });

            buttonsPanel.addStyleName(style.buttonsPanel());
            buttonsPanel.add(editButton);
            parent.add(buttonsPanel);
        }
    }

    private void showError(final String message) {
        showError(null, message, null);
    }
    
    private void showError(final Exception e) {
        showError(null, e.getMessage(), null);
    }
    
    private void showError(final DataSetClientServiceError error) {
        final String type = error.getThrowable() != null ? error.getThrowable().getClass().getName() : null;
        final String message = error.getThrowable() != null ? error.getThrowable().getMessage() : error.getMessage().toString();
        final String cause = error.getThrowable() != null && error.getThrowable().getCause() != null ? error.getThrowable().getCause().getMessage() : null;
        showError(type, message, cause);
    }

    @Override
    public void showError(String type, String message, String cause) {
        if (type != null) GWT.log("Error type: " + type);
        if (message != null) GWT.log("Error message: " + message);
        if (cause != null) GWT.log("Error cause: " + cause);
        
        errorType.setText(type != null ? type : "");
        errorTypeRow.setVisible(type != null);
        errorMessage.setText(message != null ? message : "");
        errorMessageRow.setVisible(message != null);
        errorCause.setText(cause != null ? cause : "");
        errorCauseRow.setVisible(cause != null);
        errorPanel.show();
    }

    private void hideError() {
        errorPanel.hide();
    }

    public String humanReadableByteCount(long bytes) {
        final String _b = " " + DataSetExplorerConstants.INSTANCE.bytes();
        int unit = 1024;
        if (bytes < unit) return Long.toString(bytes) + _b;
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + _b;
        return NumberFormat.getFormat(ESTIMATIONS_FORMAT).format(bytes / Math.pow(unit, exp)) + pre;
    }

    public String humanReadableRowCount(long rows) {
        int unit = 1000;
        if (rows < unit) return Long.toString(rows);
        int exp = (int) (Math.log(rows) / Math.log(unit));
        String pre = ("KMGTPE" ).charAt(exp-1) + ("");
        return NumberFormat.getFormat(ESTIMATIONS_FORMAT).format(rows / Math.pow(unit, exp)) + pre;
    }

    // **************** EVENT HANDLER REGISTRATIONS ****************************

    public HandlerRegistration addEditDataSetEventHandler(EditDataSetEventHandler handler)
    {
        return this.addHandler(handler, EditDataSetEvent.TYPE);
    }
}
