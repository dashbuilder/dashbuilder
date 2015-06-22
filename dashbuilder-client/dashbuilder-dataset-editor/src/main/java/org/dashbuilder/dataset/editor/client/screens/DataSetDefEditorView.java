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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.DataSetDefValidationCallback;
import org.dashbuilder.client.widgets.dataset.editor.widgets.DataSetEditor;
import org.dashbuilder.dataset.backend.EditDataSetDef;
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.uberfire.ext.editor.commons.client.BaseEditorViewImpl;

@Dependent
public class DataSetDefEditorView extends BaseEditorViewImpl implements DataSetDefEditorPresenter.View {

    @Inject
    DataSetEditor dataSetEditor;

    @Override
    public Widget asWidget() {
        return dataSetEditor.asWidget();
    }

    @Override
    public void startEdit(EditDataSetDef dataSetDef) {
        dataSetEditor.editDataSetDef(dataSetDef);
    }

    @Override
    public void checkValid(DataSetDefValidationCallback callback) {
        dataSetEditor.checkValid(callback);
    }

    @Override
    public void showError(String message) {
        dataSetEditor.showError(message);
    }

    @Override
    public void showError(DataSetClientServiceError error) {
        dataSetEditor.showError(error);
    }
}