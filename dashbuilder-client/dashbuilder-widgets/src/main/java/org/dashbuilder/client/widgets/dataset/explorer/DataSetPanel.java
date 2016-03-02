/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.client.widgets.dataset.explorer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.event.EditDataSetEvent;
import org.dashbuilder.client.widgets.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.dashbuilder.dataset.def.DataSetDef;
import org.uberfire.client.mvp.UberView;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * <p>Data Set Panel widget with a collapsed by default summary.</p>
 * <p>It displays a panel with a header and a collape panel body.</p>
 * 
 * @since 0.3.0 
 */
@Dependent
public class DataSetPanel implements IsWidget {

    public interface View extends UberView<DataSetPanel> {

        
        View configure(final DataSetSummary.View summaryView);

        /**
         * <p>Displays the summary header's.</p>
         * @param uuid The data set unique identifier.
         * @param parentCollapseId The identifier of the parent collage widget to refer for data toggle features.
         * @param dataSetTypeImageUri The uri for the data set type image.
         * @param dataSetTypeImageTitle The title for the data set type image.
         * @param dataSetTitle The title for the data set.
         * @return The view instance.
         */
        View showHeader(final String uuid, final String parentCollapseId, final SafeUri dataSetTypeImageUri,
                        final String dataSetTypeImageTitle, final String dataSetTitle);

        
        View showSummary();
        
        View hideSummary();
        
        View enableActionButton(final String buttonTitle, final ClickHandler clickHandler);

        View disableActionButton();
        
    }

    DataSetSummary dataSetSummary;
    Event<EditDataSetEvent> editDataSetEvent;
    View view;
    
    DataSetDef def;

    @Inject
    public DataSetPanel(final DataSetSummary dataSetSummary, 
                        final Event<EditDataSetEvent> editDataSetEvent, 
                        final View view) {
        this.dataSetSummary = dataSetSummary;
        this.editDataSetEvent = editDataSetEvent;
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        view.configure(dataSetSummary.view);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void show(final DataSetDef def, final String parentPanelId) {
        this.def = def;
        if (def != null) {
            final String name = def.getName();
            final SafeUri typeIconUri = getTypeIconUri(def);
            final String typeName = getTypeIconTitle(def);
            view.showHeader(def.getUUID(), parentPanelId, typeIconUri, typeName, name);
        }
    }
    
    public void close() {
        view.hideSummary();
    }

    public void disable() {
        view.disableActionButton();
    }

    public DataSetDef getDataSetDef() {
        return def;
    }

    void open() {
        dataSetSummary.show(def);
        view.showSummary();

        view.enableActionButton(DataSetExplorerConstants.INSTANCE.edit(), new ClickHandler() {
            @Override
            public void onClick(final ClickEvent clickEvent) {
                editDataSetEvent.fire(new EditDataSetEvent(def));
            }
        });;
    }


    SafeUri getTypeIconUri(final DataSetDef dataSetDef) {
        switch (dataSetDef.getProvider()) {
            case BEAN:
                return DataSetClientResources.INSTANCE.images().javaIcon32().getSafeUri();
            case CSV:
                return DataSetClientResources.INSTANCE.images().csvIcon32().getSafeUri();
            case SQL:
                return DataSetClientResources.INSTANCE.images().sqlIcon32().getSafeUri();
            case ELASTICSEARCH:
                return DataSetClientResources.INSTANCE.images().elIcon32().getSafeUri();
        }
        return null;
    }

    String getTypeIconTitle(final DataSetDef dataSetDef) {
        switch (dataSetDef.getProvider()) {
            case BEAN:
                return DataSetExplorerConstants.INSTANCE.bean();
            case CSV:
                return DataSetExplorerConstants.INSTANCE.csv();
            case SQL:
                return DataSetExplorerConstants.INSTANCE.sql();
            case ELASTICSEARCH:
                return DataSetExplorerConstants.INSTANCE.el();
        }
        return null;
    }

}
