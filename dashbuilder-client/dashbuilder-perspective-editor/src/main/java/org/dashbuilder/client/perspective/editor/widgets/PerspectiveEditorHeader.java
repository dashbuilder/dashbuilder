/*
 * Copyright 2015 JBoss Inc
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
package org.dashbuilder.client.perspective.editor.widgets;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.client.workbench.Header;

@ApplicationScoped
public class PerspectiveEditorHeader extends Composite implements Header {

    @Override
    public String getId() {
        return "perspective-editor-header";
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    interface Binder extends UiBinder<Widget, PerspectiveEditorHeader> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    protected Panel headerPanel;

    @Inject
    protected PerspectiveEditorToolbar editorToolbar;

    @PostConstruct
    protected void init() {
        initWidget(uiBinder.createAndBindUi(this));

        // Show when moving the mouse over
        headerPanel.addDomHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent mouseOverEvent) {
                editorToolbar.show();
            }
        }, MouseOverEvent.getType());
    }
}
