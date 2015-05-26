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
package org.dashbuilder.displayer.client;

import javax.enterprise.context.Dependent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import org.dasbuilder.client.perspective.editor.ScreenConfigurator;

@Dependent
public class DisplayerScreenConfigurator extends ScreenConfigurator {

    @Override
    public void init(IsWidget widget) {
        super.init(widget);
    }

    protected void show() {
        getContainer().add(new Label("DON"));
    }

    protected void hide() {
        getContainer().add(new Label("DOFF"));
    }

}
