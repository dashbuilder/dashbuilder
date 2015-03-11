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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import java.util.List;

// TODO
@Dependent
public class DataSetExplorer implements IsWidget {

    public interface View extends IsWidget {
        void init(DataSetExplorer presenter);
        void set(List<DataSetDef> dataSetDefs);
        boolean add(DataSetDef dataSetDef);
        boolean remove(DataSetDef dataSetDef);
        void show();
        void clear();
    }

    View view;
    
    public DataSetExplorer() {
        view = new DataSetExplorerView();
        init();
    }
    
    void init() {
        DataSetClientServices.get().getRemoteSharedDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(List<DataSetDef> dataSetDefs) {
                view.set(dataSetDefs);

                view.init(DataSetExplorer.this);
            }
        });
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }
}
