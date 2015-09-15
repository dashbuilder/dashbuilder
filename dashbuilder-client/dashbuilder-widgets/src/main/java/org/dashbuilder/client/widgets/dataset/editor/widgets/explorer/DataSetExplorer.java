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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.common.ClientRuntimeErrorPopupPresenter;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.DataSetExploredErrorEvent;
import org.dashbuilder.common.client.error.ClientRuntimeError;
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
    
    @Inject
    ClientRuntimeErrorPopupPresenter errorPopupPresenter;
    
    @Inject
    Instance<DataSetPanel> panelInstances;

    @Inject
    DataSetClientServices clientServices;

    @Inject
    View view;
    
    private List<DataSetPanel> panels = new LinkedList<DataSetPanel>();

    public DataSetExplorer() {
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

    private void showError(final ClientRuntimeError error) {
        errorPopupPresenter.showMessage(error);
    }
    
    public void showError(final String message, final String cause) {
        final String m = cause != null && cause.trim().length() > 0 ? cause : message;
        errorPopupPresenter.showMessage(m);
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

    private void disableDataSetPanelActions(final String uuid) {
        DataSetPanel panel = getDataSetPanel(uuid);
        if (panel != null) {
            panel.disable();
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

    private void onDataSetDefRegisteredEvent(@Observes DataSetDefRegisteredEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getDataSetDef();
        if (def != null && def.isPublic()) {
            // GWT.log("Data Set Explorer - Data Set Def Registered");
            addDataSetDef(def);
        }
    }

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getNewDataSetDef();
        if (def != null && def.isPublic()) {
            // GWT.log("Data Set Explorer - Data Set Def Modified");
            updateDataSetDef(def);
        }
    }

    private void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        checkNotNull("event", event);
        final DataSetDef def = event.getDataSetDef();
        if (def != null && def.isPublic()) {
            // GWT.log("Data Set Explorer - Data Set Def Removed");
            // Reload the whole data set panels list.
            show();
        }
    }

    private void onDataSetExploredErrorEvent(@Observes DataSetExploredErrorEvent event) {
        checkNotNull("event", event);
        disableDataSetPanelActions(event.getUuid());
        if (event.getClientRuntimeError() != null) showError(event.getClientRuntimeError());
        else showError(event.getMessage(), event.getCause());
    }
    
}
