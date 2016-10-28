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

import javax.inject.Inject;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.navigation.resources.i18n.NavigationConstants;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.Node;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.UnorderedList;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.mvp.Command;

@Templated
public class NavItemEditorView extends Composite
        implements NavItemEditor.View {

    @Inject
    @DataField
    Span itemIcon;

    @Inject
    @DataField
    Span itemName;

    @Inject
    @DataField
    Input itemNameInput;

    @Inject
    @DataField
    Div extraDiv;

    @Inject
    @DataField
    UnorderedList commandMenu;

    NavigationConstants i18n = NavigationConstants.INSTANCE;
    NavItemEditor presenter;

    @Override
    public void init(NavItemEditor presenter) {
        this.presenter = presenter;
        itemIcon.setTitle(i18n.itemMenuTitle());
        itemNameInput.getStyle().setProperty("display", "none");
        extraDiv.getStyle().setProperty("display", "none");
    }

    @Override
    public void setItemName(String name) {
        itemName.setTextContent(name);
        itemNameInput.setValue(name);
    }

    @Override
    public void setItemDescription(String description) {
        itemName.setTitle(description);
    }

    @Override
    public void setItemType(NavItemEditor.ItemType type) {
        if (type == NavItemEditor.ItemType.GROUP) {
            itemIcon.setClassName("pficon-folder-open");
        }
        else if (type == NavItemEditor.ItemType.DIVIDER) {
            itemIcon.setClassName("fa fa-minus");
        }
        else if (type == NavItemEditor.ItemType.PERSPECTIVE) {
            itemIcon.setClassName("pficon-screen");
        }
        else if (type == NavItemEditor.ItemType.RUNTIME_PERSPECTIVE) {
            itemIcon.setClassName("pficon-virtual-machine");
        }
    }

    @Override
    public void addCommand(String name, Command command) {
        AnchorElement anchor = Document.get().createAnchorElement();
        anchor.setInnerText(name);

        LIElement li = Document.get().createLIElement();
        li.getStyle().setCursor(Style.Cursor.POINTER);
        li.appendChild(anchor);
        commandMenu.appendChild((Node) li);

        Event.sinkEvents(anchor, Event.ONCLICK);
        Event.setEventListener(anchor, event -> {
            if(Event.ONCLICK == event.getTypeInt()) {
                command.execute();
            }
        });
    }

    @Override
    public void addCommandDivider() {
        LIElement li = Document.get().createLIElement();
        li.setClassName("divider");
        commandMenu.appendChild((Node) li);
    }

    @Override
    public void setCommandsEnabled(boolean enabled) {
        if (enabled) {
            commandMenu.getStyle().removeProperty("display");
        } else {
            commandMenu.getStyle().setProperty("display", "none");
        }
    }

    @Override
    public void startItemEdition() {
        itemName.getStyle().setProperty("display", "none");
        itemNameInput.getStyle().removeProperty("display");
        extraDiv.getStyle().removeProperty("display");
        itemNameInput.focus();
    }

    @Override
    public void finishItemEdition() {
        itemName.getStyle().removeProperty("display");
        itemNameInput.getStyle().setProperty("display", "none");
        extraDiv.getStyle().setProperty("display", "none");
    }

    @Override
    public void setContextWidget(IsWidget widget) {
        DOMUtil.removeAllChildren(extraDiv);
        Element el = widget.asWidget().getElement();
        el.getStyle().setWidth(150, Style.Unit.PX);
        extraDiv.appendChild((Node) el);
    }

    @Override
    public void setItemEditable(boolean editable) {
        if (editable) {
            itemName.getStyle().setProperty("cursor", "pointer");
        } else {
            itemName.getStyle().removeProperty("cursor");
        }
    }

    @Override
    public String i18nNewItem(String item) {
        return i18n.newItem(item);
    }

    @Override
    public String i18nGotoItem(String item) {
        return i18n.gotoItem(item);
    }

    @Override
    public String i18nDeleteItem() {
        return i18n.deleteItem();
    }

    @Override
    public String i18nMoveUp() {
        return i18n.moveUp();
    }

    @Override
    public String i18nMoveDown() {
        return i18n.moveDown();
    }

    @Override
    public String i18nMoveFirst() {
        return i18n.moveFirst();
    }

    @Override
    public String i18nMoveLast() {
        return i18n.moveLast();
    }

    @EventHandler("itemName")
    public void onItemNameClick(ClickEvent event) {
        presenter.onItemClick();
    }

    @EventHandler("itemNameInput")
    public void onItemNameInputOver(MouseOverEvent event) {
        itemNameInput.focus();
    }

    @EventHandler("itemNameInput")
    public void onItemNameChanged(ChangeEvent event) {
        presenter.onItemNameChanged(itemNameInput.getValue());
    }
}

