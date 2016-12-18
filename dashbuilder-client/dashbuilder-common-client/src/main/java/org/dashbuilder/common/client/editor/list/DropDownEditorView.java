/**
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
package org.dashbuilder.common.client.editor.list;

import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.editor.ValueBoxEditor;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchDropDown;

@Dependent
public class DropDownEditorView extends Composite implements DropDownEditor.View {

    interface Binder extends UiBinder<Widget, DropDownEditorView> {
        Binder BINDER = GWT.create(Binder.class);
    }

    interface DropDownEditorViewStyle extends CssResource {
        String errorPanel();
        String errorPanelWithError();
    }

    @UiField
    DropDownEditorViewStyle style;

    @UiField
    @Editor.Ignore
    HTMLPanel errorPanel;

    @UiField
    @Editor.Ignore
    FlowPanel helpPanel;

    @UiField
    @Editor.Ignore
    Tooltip errorTooltip;

    LiveSearchDropDown dropDown;
    DropDownEditor presenter;

    @Override
    public void init(final DropDownEditor presenter) {
        this.presenter = presenter;
    }

    @UiConstructor
    public DropDownEditorView() {
        initWidget(Binder.BINDER.createAndBindUi(this));
    }

    @Override
    public DropDownEditorView setDropDown(LiveSearchDropDown dropDown) {
        this.dropDown = dropDown;
        helpPanel.add(dropDown);
        return this;
    }

    @Override
    public DropDownEditorView addHelpContent(String title, String content, Placement placement) {
        final Popover popover = new Popover(dropDown.asWidget());
        popover.setContainer("body");
        popover.setShowDelayMs(1000);
        popover.setPlacement(placement);
        popover.setTitle(title);
        popover.setContent(content);
        helpPanel.add(popover);
        return this;
    }

    @Override
    public DropDownEditorView showError(SafeHtml message) {
        errorTooltip.setTitle(message.asString());
        errorTooltip.reconfigure();
        errorPanel.removeStyleName(style.errorPanel());
        errorPanel.addStyleName(style.errorPanelWithError());
        return this;
    }

    @Override
    public DropDownEditorView clearError() {
        errorTooltip.setTitle("");
        errorTooltip.reconfigure();
        errorPanel.removeStyleName(style.errorPanelWithError());
        errorPanel.addStyleName(style.errorPanel());
        return this;
    }
}
