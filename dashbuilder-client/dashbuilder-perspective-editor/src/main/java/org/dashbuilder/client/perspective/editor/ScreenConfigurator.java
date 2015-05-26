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
package org.dashbuilder.client.perspective.editor;

import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class ScreenConfigurator extends Composite {

    interface Binder extends UiBinder<Widget, ScreenConfigurator> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    protected Panel containerPanel;

    IsWidget screenWidget;

    public void init(IsWidget widget) {
        initWidget(uiBinder.createAndBindUi(this));
        this.screenWidget = widget;
        this.containerPanel.add(screenWidget);

        containerPanel.addDomHandler(new MouseOverHandler() {
            @Override public void onMouseOver(MouseOverEvent mouseOverEvent) {
                show();
            }
        }, MouseOverEvent.getType());

        containerPanel.addDomHandler(new MouseOutHandler() {
            @Override public void onMouseOut(MouseOutEvent mouseOutEvent) {
                hide();
            }
        }, MouseOutEvent.getType());
    }

    public Panel getContainer() {
        return containerPanel;
    }

    protected void show() {
        containerPanel.add(new Label("ON"));
    }

    protected void hide() {
        containerPanel.add(new Label("OFF"));
    }
}
