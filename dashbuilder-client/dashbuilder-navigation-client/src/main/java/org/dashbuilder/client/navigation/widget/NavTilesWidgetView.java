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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.navigation.resources.i18n.NavigationConstants;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Node;
import org.jboss.errai.common.client.dom.OrderedList;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.mvp.Command;

@Dependent
@Templated
public class NavTilesWidgetView extends BaseNavWidgetView<NavTilesWidget>
    implements NavTilesWidget.View {

    @Inject
    @DataField
    Div mainDiv;

    @Inject
    @DataField
    Div contentDiv;

    @Inject
    @DataField
    Div tilesDiv;

    @Inject
    @DataField("breadcrumb")
    OrderedList breadcrumb;

    NavTilesWidget presenter;

    @Override
    public void init(NavTilesWidget presenter) {
        this.presenter = presenter;
        super.navWidget = tilesDiv;
    }

    @Override
    public void setActive(boolean active) {
        // Useless in tile navigator
    }

    @Override
    public void addDivider() {
        // Useless in tile navigator
    }

    @Override
    public void addTileWidget(IsElement tileWidget) {
        DOMUtil.removeAllChildren(mainDiv);
        mainDiv.appendChild(contentDiv);
        tilesDiv.appendChild(tileWidget.getElement());
    }

    @Override
    public void showTileContent(IsWidget tileContent) {
        DOMUtil.removeAllChildren(tilesDiv);
        tilesDiv.appendChild((Node) tileContent.asWidget().getElement());
    }

    @Override
    public void errorNavItemsEmpty() {
        DOMUtil.removeAllChildren(mainDiv);
        Element errorEl = super.createErrorWidget(NavigationConstants.INSTANCE.navTilesDragComponentEmptyError());
        mainDiv.appendChild((Node) errorEl);
    }

    @Override
    public void recursivityError() {
        DOMUtil.removeAllChildren(tilesDiv);
        Element errorEl = super.createErrorWidget(NavigationConstants.INSTANCE.navTilesDragComponentRecursivityError());
        tilesDiv.appendChild((Node) errorEl);
    }

    @Override
    public void clearBreadcrumb() {
        DOMUtil.removeAllChildren(breadcrumb);
    }

    @Override
    public void addBreadcrumbItem(String navItemName) {
        addBreadcrumbItem(navItemName, null);
    }

    @Override
    public void addBreadcrumbItem(String navItemName, Command onClicked) {
        LIElement li = Document.get().createLIElement();
        breadcrumb.appendChild((Node) li);

        if (onClicked != null) {
            AnchorElement anchor = Document.get().createAnchorElement();
            anchor.setInnerText(navItemName);
            li.appendChild(anchor);
            li.getStyle().setCursor(Style.Cursor.POINTER);

            Event.sinkEvents(anchor, Event.ONCLICK);
            Event.setEventListener(anchor, event -> {
                if (Event.ONCLICK == event.getTypeInt()) {
                    onClicked.execute();
                }
            });
        } else {
            ((Node) li).setTextContent(navItemName);
            li.setClassName("active");
        }
    }
}
