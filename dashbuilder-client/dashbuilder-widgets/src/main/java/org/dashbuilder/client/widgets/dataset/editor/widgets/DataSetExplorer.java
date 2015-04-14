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

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.DeleteDataSetEventHandler;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.EditDataSetEventHandler;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import java.util.List;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Dependent
public class DataSetExplorer implements IsWidget {

    public interface View extends IsWidget, HasHandlers {
        void set(List<DataSetDef> dataSetDefs);

        boolean add(DataSetDef dataSetDef);

        boolean remove(DataSetDef dataSetDef);

        boolean update(DataSetDef oldDataSetDef, DataSetDef newDataSetDef);

        void show();

        void clear();

        HandlerRegistration addEditDataSetEventHandler(EditDataSetEventHandler handler);

        HandlerRegistration addDeleteDataSetEventHandler(DeleteDataSetEventHandler handler);
    }

    View view;

    @Inject
    public DataSetExplorer() {
        view = new DataSetExplorerView();
        init();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    private void init() {
        DataSetClientServices.get().getRemoteSharedDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(List<DataSetDef> dataSetDefs) {
                view.set(dataSetDefs);
                view.show();
            }
        });
    }

    // Be aware of data set lifecycle events

    private void onDataSetDefRegisteredEvent(@Observes DataSetDefRegisteredEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getDataSetDef();
        if (def != null && def.isPublic()) {
            view.add(event.getDataSetDef());
            view.show();
        }
    }

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getNewDataSetDef();
        if (def != null && def.isPublic()) {
            view.update(event.getOldDataSetDef(), event.getNewDataSetDef());
            view.show();
        }
    }

    private void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        checkNotNull("event", event);

        view.remove(event.getDataSetDef());
        view.show();
    }

    // **************** EVENT HANDLER REGISTRATIONS ****************************

    public HandlerRegistration addEditDataSetEventHandler(EditDataSetEventHandler handler) {
        return view.addEditDataSetEventHandler(handler);
    }

    public HandlerRegistration addDeleteDataSetEventHandler(DeleteDataSetEventHandler handler) {
        return view.addDeleteDataSetEventHandler(handler);
    }
}
