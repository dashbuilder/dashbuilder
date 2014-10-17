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
package org.dashbuilder.displayer.client;

import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerSettings;

@Dependent
public class DisplayerEditorView extends Composite {

    interface DisplayerEditorViewBinder extends
            UiBinder<Widget, DisplayerEditorView> {

    }
    private static DisplayerEditorViewBinder uiBinder = GWT.create(DisplayerEditorViewBinder.class);

    public DisplayerEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    private DisplayerEditorPresenter presenter;

    public void init(DisplayerEditorPresenter presenter) {
        this.presenter = presenter;
    }
}
