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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import org.dashbuilder.dataset.client.widgets.events.EditDataSetEvent;
import org.dashbuilder.dataset.client.widgets.events.NewDataSetEvent;

import javax.enterprise.context.Dependent;

// TODO
@Dependent
public class DataSetEditor extends Composite {

    private FlowPanel container = new FlowPanel();
    
    public DataSetEditor() {
        initWidget(container);
        container.add(new Label("Data Set Editor Widget"));
    }
    
    public void newDataSet(NewDataSetEvent event) {
        container.clear();
        container.add(new Label("New Data Set: " + event.getUuid()));
    }

    public void editDataSet(EditDataSetEvent event) {
        container.clear();
        container.add(new Label("Edit Data Set: " + event.getUuid()));
    }

}
