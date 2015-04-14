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

import com.github.gwtbootstrap.client.ui.Accordion;
import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.client.widgets.SlidingPanel;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.DeleteDataSetEvent;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.DeleteDataSetEventHandler;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.EditDataSetEvent;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.EditDataSetEventHandler;
import org.dashbuilder.client.widgets.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientImages;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.enterprise.context.Dependent;
import java.util.*;

@Dependent
public class DataSetExplorerView extends Composite implements DataSetExplorer.View {

    private final static String WHITESPACE = " ";
    
    interface DataSetExplorerViewBinder extends UiBinder<Widget, DataSetExplorerView> {}
    private static DataSetExplorerViewBinder uiBinder = GWT.create(DataSetExplorerViewBinder.class);

    interface DataSetExplorerViewStyle extends CssResource {
        String columnsPanel();
        String statusPanel();
        String statusIcon();
        String statusText();
        String estimationsPanel();
        String buttonsPanel();
        String button();
        String slidingPanel();
        String deleteText();
    }

    @UiField
    DataSetExplorerViewStyle style;
    
    @UiField
    Accordion dataSetsAccordion;
    
    @UiField
    Label label;
    
    private Set<DataSetDef> dataSets;
    
    public DataSetExplorerView() {
        initWidget(uiBinder.createAndBindUi(this));
        dataSets = new LinkedHashSet<DataSetDef>();
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
    public boolean remove(DataSetDef dataSetDef) {
        return dataSets.remove(dataSetDef);
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
    public void show() {
        clearView();

        if (!dataSets.isEmpty()) {
            label.setVisible(false);
            for (DataSetDef dataSetDef : dataSets) {
                final AccordionGroup accordionGroup = buildDataSetAccordionGroup(dataSetDef);
                dataSetsAccordion.add(accordionGroup);
            }
            
        } else {
            label.setText(DataSetExplorerConstants.INSTANCE.noDataSets());
            label.setVisible(true);
        }
        dataSetsAccordion.setVisible(true);
    }
    
    private AccordionGroup buildDataSetAccordionGroup(DataSetDef dataSetDef) {
        final AccordionGroup accordionGroup = new AccordionGroup();

        // Heading.
        accordionGroup.setHeading(dataSetDef.getName());
        // CollapseTrigger collapseTrigger = new CollapseTrigger();
        
        // Icon for provider type.
        final Image typeIcon = buildTypeIcon(dataSetDef);
        if (typeIcon != null) accordionGroup.addCustomTrigger(typeIcon);
        
        buildDescription(dataSetDef, accordionGroup);
        
        return accordionGroup;
    } 
    
    private Image buildTypeIcon(final DataSetDef dataSetDef) {
        Image typeIcon = null;
        switch (dataSetDef.getProvider()) {
            case BEAN:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().javaIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.bean());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.bean());
                break;
            case CSV:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().csvIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.csv());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.csv());
                break;
            case SQL:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().sqlIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.sql());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.sql());
                break;
            case ELASTICSEARCH:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().elIconLarge().getSafeUri());
                typeIcon.setAltText(DataSetExplorerConstants.INSTANCE.el());
                typeIcon.setTitle(DataSetExplorerConstants.INSTANCE.el());
                break;
        }
        typeIcon.setSize("15px", "15px");
        return typeIcon;
        
    }
    
    private void buildDescription(final DataSetDef dataSetDef, final Panel parent) {
        if (parent != null) {
            final DataSetClientImages images = DataSetClientResources.INSTANCE.images(); 
            
            // Caches and refresh.
            final boolean isCacheEnabled = dataSetDef.isCacheEnabled();
            final FlowPanel cachePanel = new FlowPanel();
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
            statusPanel.add(cachePanel);
            statusPanel.add(pushPanel);
            statusPanel.add(refreshPanel);

            // Estimated rows and size.
            final FlowPanel estimationsPanel = new FlowPanel();
            estimationsPanel.addStyleName(style.estimationsPanel());
            final DataSetMetadata metadata = dataSetDef.getDataSet().getMetadata();
            final int estimatedSize = metadata.getEstimatedSize();
            final int rowCount = metadata.getNumberOfRows();
            
            final HTML estimatedSizeText = new HTML(estimatedSize + WHITESPACE + DataSetExplorerConstants.INSTANCE.bytes());
            estimatedSizeText.addStyleName(style.statusText());
            final HTML estimatedRowsText = new HTML(rowCount + WHITESPACE + DataSetExplorerConstants.INSTANCE.rows());
            estimatedRowsText.addStyleName(style.statusText());
            
            // Add into parent container.
            estimationsPanel.add(estimatedRowsText);
            estimationsPanel.add(estimatedSizeText);

            final FlowPanel columnsPanel = new FlowPanel();
            columnsPanel.addStyleName(style.columnsPanel());
            columnsPanel.add(statusPanel);
            columnsPanel.add(estimationsPanel);
            parent.add(columnsPanel);

            // Edit, cancel and confirmation buttons.
            final SlidingPanel slidingPanel = new SlidingPanel();
            slidingPanel.addStyleName(style.slidingPanel());
            final FlowPanel buttonsPanel = new FlowPanel();
            final FlowPanel deleteConfirmPanel = new FlowPanel();

            final com.github.gwtbootstrap.client.ui.Button editButton = new Button(DataSetExplorerConstants.INSTANCE.edit());
            final com.github.gwtbootstrap.client.ui.Button deleteButton = new Button(DataSetExplorerConstants.INSTANCE.delete());
            final boolean isPublic = dataSetDef.isPublic();
            editButton.setEnabled(isPublic);
            editButton.addStyleName(style.button());
            deleteButton.setEnabled(isPublic);
            deleteButton.setType(ButtonType.DANGER);
            deleteButton.addStyleName(style.button());
            
            editButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    fireEvent(new EditDataSetEvent(dataSetDef.getUUID()));
                }
            });

            deleteButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    slidingPanel.setWidget(deleteConfirmPanel);
                }
            });

            buttonsPanel.addStyleName(style.buttonsPanel());
            buttonsPanel.add(editButton);
            buttonsPanel.add(deleteButton);
            slidingPanel.add(buttonsPanel);
            
            final HTML deleteText = new HTML(DataSetExplorerConstants.INSTANCE.areYouSure());
            deleteText.addStyleName(style.deleteText());
            final com.github.gwtbootstrap.client.ui.Button yesButton = new Button(DataSetExplorerConstants.INSTANCE.yes());
            yesButton.setType(ButtonType.SUCCESS);
            yesButton.addStyleName(style.button());
            final com.github.gwtbootstrap.client.ui.Button noButton = new Button(DataSetExplorerConstants.INSTANCE.no());
            noButton.setType(ButtonType.DANGER);
            noButton.addStyleName(style.button());
            
            yesButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    fireEvent(new DeleteDataSetEvent(dataSetDef.getUUID()));
                }
            });
            
            noButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    slidingPanel.setWidget(buttonsPanel);
                }
            });

            deleteConfirmPanel.addStyleName(style.buttonsPanel());
            deleteConfirmPanel.add(deleteText);
            deleteConfirmPanel.add(noButton);
            deleteConfirmPanel.add(yesButton);
            slidingPanel.add(deleteConfirmPanel);
            
            slidingPanel.setWidget(buttonsPanel);
            parent.add(slidingPanel);
        }
    }


    // **************** EVENT HANDLER REGISTRATIONS ****************************

    public HandlerRegistration addEditDataSetEventHandler(EditDataSetEventHandler handler)
    {
        return this.addHandler(handler, EditDataSetEvent.TYPE);
    }

    public HandlerRegistration addDeleteDataSetEventHandler(DeleteDataSetEventHandler handler)
    {
        return this.addHandler(handler, DeleteDataSetEvent.TYPE);
    }

}
