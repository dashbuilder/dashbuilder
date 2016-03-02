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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.mvp.UberView;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

/**
 * <p>Data Set Explorer widget.</p>
 * 
 * @since 0.3.0 
 */
@Dependent
public class DataSetExplorer implements IsWidget {

    public interface View extends UberView<DataSetExplorer> {
        
        View addPanel(final DataSetPanel.View panelView);
        
        View clear();
    }
    
    Instance<DataSetPanel> panelInstances;
    DataSetClientServices clientServices;
    View view;
    
    List<DataSetPanel> panels = new LinkedList<DataSetPanel>();

    @Inject
    public DataSetExplorer(final Instance<DataSetPanel> panelInstances,
                           final DataSetClientServices clientServices,
                           final View view) {
        this.panelInstances = panelInstances;
        this.clientServices = clientServices;
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void show() {
        clear();
        
        clientServices.getPublicDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(final List<DataSetDef> dataSetDefs) {
                
                if (dataSetDefs != null && !dataSetDefs.isEmpty()) {
                    for (final DataSetDef def : dataSetDefs) {
                        addDataSetDef(def);
                    }
                }
            }
        });
    }

    private void addDataSetDef(final DataSetDef def) {
        // Check panel for the given data set does not exists yet.
        if (getDataSetPanel(def.getUUID()) == null) {
            final DataSetPanel panel = panelInstances.get();
            panels.add(panel);
            panel.show(def, "dataSetsExplorerPanelGroup");
            view.addPanel(panel.view);
        }
    }

    private void updateDataSetDef(final DataSetDef def) {
        DataSetPanel panel = getDataSetPanel(def.getUUID()); 
        if (panel != null) {
            panel.show(def, "dataSetsExplorerPanelGroup");
            panel.close();
        }
    }

    private DataSetPanel getDataSetPanel(final String uuid) {
        if (uuid != null) {
            for (final DataSetPanel panel : panels) {
                if (panel.getDataSetDef().getUUID().equals(uuid)) {
                    return panel;
                }
            }
        }
        return null;
    }
    
    private void clear() {
        panels.clear();
        view.clear();
    }
    
    // Be aware of data set lifecycle events

    void onDataSetDefRegisteredEvent(@Observes DataSetDefRegisteredEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getDataSetDef();
        if (def != null && def.isPublic()) {
            // GWT.log("Data Set Explorer - Data Set Def Registered");
            addDataSetDef(def);
        }
    }

    void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getNewDataSetDef();
        if (def != null && def.isPublic()) {
            // GWT.log("Data Set Explorer - Data Set Def Modified");
            updateDataSetDef(def);
        }
    }

    void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        checkNotNull("event", event);
        final DataSetDef def = event.getDataSetDef();
        if (def != null && def.isPublic()) {
            // GWT.log("Data Set Explorer - Data Set Def Removed");
            // Reload the whole data set panels list.
            show();
        }
    }
    
}
