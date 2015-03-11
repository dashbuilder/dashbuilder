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
package org.dashbuilder.dataset.client.widgets;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.dataset.client.ClientDataSetManager;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.widgets.events.DeleteDataSetEventHandler;
import org.dashbuilder.dataset.client.widgets.events.EditDataSetEvent;
import org.dashbuilder.dataset.client.widgets.events.EditDataSetEventHandler;
import org.dashbuilder.dataset.client.widgets.events.NewDataSetEvent;
import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import java.util.List;

@Dependent
public class DataSetEditor implements IsWidget {
    

    public interface View extends IsWidget, HasHandlers {
        void init(DataSetEditor presenter);
        void create(String uuid);
        void edit(DataSetDef dataSetDef);
        void clear();
    }

    View view;
    
    public DataSetEditor() {
        view = new DataSetEditorView();
        init();
    }
    
    void init() {
        view.init(DataSetEditor.this);
    }
    
    public void newDataSet(NewDataSetEvent event) {
        view.create(event.getUuid());
    }

    public void editDataSet(EditDataSetEvent event) {
        // TODO: view.edit();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }
}
