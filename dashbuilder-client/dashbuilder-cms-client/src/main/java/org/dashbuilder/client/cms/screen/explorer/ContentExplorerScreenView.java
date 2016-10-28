/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.cms.screen.explorer;

import javax.inject.Inject;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.ListItem;
import org.jboss.errai.common.client.dom.Node;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.UnorderedList;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.mvp.Command;

@Templated
public class ContentExplorerScreenView extends Composite implements ContentExplorerScreen.View {

    @Inject
    @DataField
    Div tabContent;

    @Inject
    @DataField
    ListItem perspectivesTab;

    @Inject
    @DataField
    Anchor perspectivesAnchor;

    @Inject
    @DataField
    ListItem menusTab;

    @Inject
    @DataField
    Anchor menusAnchor;

    @Inject
    @DataField
    Div createDiv;

    @Inject
    @DataField
    Span createText;

    @Inject
    @DataField
    UnorderedList createMenu;

    ContentExplorerScreen presenter;

    @Override
    public void init(ContentExplorerScreen presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setPerspectivesName(String name) {
        perspectivesAnchor.setTextContent(name);
    }

    @Override
    public void showPerspectives(IsWidget perspectivesExplorer) {
        perspectivesTab.setClassName("active");
        menusTab.setClassName("");
        DOMUtil.removeAllChildren(tabContent);
        tabContent.appendChild((Node) perspectivesExplorer.asWidget().getElement());
    }

    @Override
    public void showMenus(IsWidget menusExplorer) {
        menusTab.setClassName("active");
        perspectivesTab.setClassName("");
        DOMUtil.removeAllChildren(tabContent);
        tabContent.appendChild((Node) menusExplorer.asWidget().getElement());
    }

    @Override
    public void setMenusName(String name) {
        menusAnchor.setTextContent(name);
    }

    @Override
    public void setCreateName(String name) {
        createText.setTextContent(name);
    }

    @Override
    public void setCreateMenuVisible(boolean visible) {
        if (visible) {
            createDiv.getStyle().removeProperty("display");
        } else {
            createDiv.getStyle().setProperty("display", "none");
        }
    }

    @Override
    public void addCreateMenuEntry(String name, Command onClick) {
        AnchorElement anchor = Document.get().createAnchorElement();
        anchor.getStyle().setCursor(Style.Cursor.POINTER);
        anchor.setInnerText(name);

        LIElement li = Document.get().createLIElement();
        li.appendChild(anchor);
        createMenu.appendChild((Node) li);

        Event.sinkEvents(anchor, Event.ONCLICK);
        Event.setEventListener(anchor, event -> {
            if(Event.ONCLICK == event.getTypeInt()) {
                onClick.execute();
            }
        });
    }

    @EventHandler("perspectivesAnchor")
    public void onPerspectivesAnchorClick(final ClickEvent event) {
        presenter.gotoPerspectives();
    }

    @EventHandler("menusAnchor")
    public void onMenusAnchorClick(final ClickEvent event) {
        presenter.gotoMenus();
    }
}