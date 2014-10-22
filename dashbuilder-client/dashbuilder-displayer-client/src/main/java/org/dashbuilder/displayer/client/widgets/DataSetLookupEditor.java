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
package org.dashbuilder.displayer.client.widgets;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.common.client.api.RemoteCallback;

@Dependent
public class DataSetLookupEditor {

    public interface Listener {
    }

    public interface View extends IsWidget {
        void init(DataSetLookupEditor presenter);
        void notFound();
        void error(Exception e);
    }

    Listener listener;
    View view;

    DataSetLookup originalLookup = null;
    DataSetLookup currentLookup = null;

    public DataSetLookupEditor() {
        this.view = new DataSetLookupEditorView();
    }

    @Inject
    public DataSetLookupEditor(DataSetLookupEditorView view) {
        this.view = view;
    }

    public void init(DataSet dataSet, Listener listener) {
        view.notFound();
    }

    public void init(DataSetLookup dataSetLookup, Listener listener) {
        try {
            this.listener = listener;
            this.originalLookup = dataSetLookup;
            this.currentLookup = dataSetLookup.cloneInstance();
            view.init(this);

            view.error(null);

            DataSetClientServices.get().fetchMetadata(currentLookup, new DataSetMetadataCallback() {
                public void callback(DataSetMetadata metatada) {
                    DataSetDef dataSetDef = metatada.getDefinition();
                    if (dataSetDef == null) {

                    } else {

                    }
                }
                public void notFound() {
                    view.notFound();
                }
            });
        } catch (Exception e) {
            view.error(e);
        }
    }

    public View getView() {
        return view;
    }

    public void fetchAvailableDataSets() {
        DataSetClientServices.get().getRemoteSharedDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(List<DataSetDef> dataSetDefs) {

            }
        });
    }
}
