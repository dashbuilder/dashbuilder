/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.navigation.resources.i18n.NavigationConstants;
import org.dashbuilder.common.client.widgets.AlertBox;
import org.jboss.errai.common.client.dom.CSSStyleDeclaration;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Element;
import org.jboss.errai.common.client.dom.Window;

public abstract class TargetDivNavWidgetView<T extends TargetDivNavWidget> extends BaseNavWidgetView<T>
        implements TargetDivNavWidget.View<T> {

    AlertBox alertBox;

    public TargetDivNavWidgetView(AlertBox alertBox) {
        this.alertBox = alertBox;
        alertBox.setLevel(AlertBox.Level.WARNING);
        alertBox.setCloseEnabled(false);
        CSSStyleDeclaration style = alertBox.getElement().getStyle();
        style.setProperty("width", "30%");
        style.setProperty("margin", "10px");
    }

    @Override
    public void clearContent(String targetDivId) {
        Element targetDiv = Window.getDocument().getElementById(targetDivId);
        if (targetDiv != null) {
            DOMUtil.removeAllChildren(targetDiv);
        }
    }

    @Override
    public void showContent(String targetDivId, IsWidget content) {
        Element targetDiv = Window.getDocument().getElementById(targetDivId);
        if (targetDiv != null) {
            DOMUtil.removeAllChildren(targetDiv);
            Div container = (Div) Window.getDocument().createElement("div");
            targetDiv.appendChild(container);
            super.appendWidgetToElement(container, content);
        }
    }

    @Override
    public void deadlockError(String targetDivId) {
        Element targetDiv = Window.getDocument().getElementById(targetDivId);
        if (targetDiv != null) {
            DOMUtil.removeAllChildren(targetDiv);
            alertBox.setMessage(NavigationConstants.INSTANCE.targetDivIdPerspectiveDeadlockError());
            targetDiv.appendChild(alertBox.getElement());
        }
    }
}
