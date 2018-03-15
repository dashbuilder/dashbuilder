/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.WithClassesToStub;
import org.dashbuilder.client.widgets.dataset.editor.workflow.edit.DataSetEditWorkflow;
import org.gwtbootstrap3.client.ui.Modal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.uberfire.ext.editor.commons.client.history.VersionRecordManager;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.events.NotificationEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(GwtMockitoTestRunner.class)
@WithClassesToStub({Modal.class})
public class DataSetDefEditorPresenterValidationTest {

    DataSetDefEditorPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new DataSetDefEditorPresenter() {
            {
                workflow = mock(DataSetEditWorkflow.class);
                versionRecordManager = mock(VersionRecordManager.class);
            }

            @Override
            protected void notifyValidationResult(NotificationEvent event) {

            }
        };
    }

    @Test
    public void commandIsCalled() throws Exception {

        final Command afterValidation = mock(Command.class);
        presenter.onValidate(afterValidation);
        verify(afterValidation).execute();
    }
}