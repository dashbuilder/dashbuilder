/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.client.navigation.widget;

import java.util.Stack;

import javax.inject.Inject;

import com.google.gwt.core.client.Duration;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import org.dashbuilder.client.navigation.resources.i18n.NavigationConstants;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Node;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class NavTreeEditorView extends Composite
        implements NavTreeEditor.View {

    @Inject
    @DataField
    Div treeDiv;

    @Inject
    @DataField
    Div alertDiv;

    @Inject
    @DataField
    Span alertMessage;

    @Inject
    @DataField
    Button saveButton;

    @Inject
    @DataField
    Button cancelButton;

    NavigationConstants i18n = NavigationConstants.INSTANCE;
    NavTreeEditor presenter;
    Stack<Node> itemStack = new Stack<>();
    Node currentItem = null;

    @Override
    public void init(NavTreeEditor presenter) {
        this.presenter = presenter;
        alertMessage.setTextContent(i18n.saveChanges());
        saveButton.setTextContent(i18n.save());
        cancelButton.setTextContent(i18n.cancel());
        clear();
    }

    @Override
    public void clear() {
        DOMUtil.removeAllChildren(treeDiv);
        currentItem = treeDiv;
        itemStack.clear();
        setChangedFlag(false);
    }

    @Override
    public void setChangedFlag(boolean on) {
        if (on) {
            alertDiv.getStyle().removeProperty("display");
        } else {
            alertDiv.getStyle().setProperty("display", "none");
        }
    }

    @Override
    public void goOneLevelDown() {
        itemStack.push(currentItem);
        currentItem = null;
    }

    @Override
    public void goOneLevelUp() {
        if (!itemStack.isEmpty()) {
            currentItem = itemStack.pop();
        }
    }

    @Override
    public void addItemEditor(NavItemEditor navItemEditor) {
        com.google.gwt.dom.client.Element el = navItemEditor.asWidget().getElement();
        int margin = itemStack.size() * 20;
        el.getStyle().setMarginLeft(margin, Style.Unit.PX);
        treeDiv.appendChild((Node) el);
    }

    @Override
    public String generateId() {
        return Double.toString(Duration.currentTimeMillis());
    }

    @EventHandler("saveButton")
    public void onSaveClicked(ClickEvent event) {
        presenter.onSaveClicked();
    }

    @EventHandler("cancelButton")
    public void onCancelClicked(ClickEvent event) {
        presenter.onCancelClicked();
    }
}

