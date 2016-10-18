/**
 * Copyright (C) 2015 JBoss Inc
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
package org.dashbuilder.dataset.editor.client.screens;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.editor.client.resources.i18n.DataSetAuthoringConstants;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.lifecycle.OnStartup;

@WorkbenchScreen(identifier = "DataSetAuthoringHome")
@Dependent
public class DataSetAuthoringHomePresenter {

    public interface View extends UberView<DataSetAuthoringHomePresenter> {
        void setDataSetCount(int n);
    }

    @Inject
    View view;

    @Inject
    PlaceManager placeManager;

    @Inject
    DataSetClientServices clientServices;

    @OnStartup
    public void init() {
        clientServices.getPublicDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            @Override public void callback(List<DataSetDef> dataSetDefs) {
                view.setDataSetCount(dataSetDefs.size());
            }
        });
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return DataSetAuthoringConstants.INSTANCE.homeTitle();
    }

    @WorkbenchPartView
    public UberView<DataSetAuthoringHomePresenter> getView() {
        return view;
    }

    public void newDataSet() {
        placeManager.goTo("DataSetDefWizard");
    }
}
