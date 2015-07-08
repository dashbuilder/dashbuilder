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
package org.dashbuilder.displayer.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class DisplayerEditorPopup extends BaseModal {

    interface Binder extends UiBinder<ModalBody, DisplayerEditorPopup> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField(provided = true)
    DisplayerEditor editor;

    private DisplayerSettings settings;
    private DisplayerEditor.Listener editorListener;
    private HandlerRegistration showHandlerRegistration;

    @Inject
    public DisplayerEditorPopup(DisplayerEditor editor) {
        this.editor = editor;
        add(uiBinder.createAndBindUi(this));
        ModalFooterOKCancelButtons footer = new ModalFooterOKCancelButtons(okCommand, cancelCommand);
        footer.enableCancelButton(true);
        footer.enableOkButton(true);
        add(footer);
        setWidth(950+"px");
    }

    public void init(final DisplayerSettings settings, final DisplayerEditor.Listener editorListener) {
        this.settings = settings;
        this.editorListener = editorListener;
        this.showHandlerRegistration = this.addShownHandler(shownHandler);
        show();
    }

    /**
     * <p>The popup must be visible in order that the table can display the different row's values. So after popup is shown, initialize the editor.</p>
     */
    private final ModalShownHandler shownHandler = new ModalShownHandler() {
        @Override
        public void onShown(ModalShownEvent modalShownEvent) {
            editor.init(settings, editorListener);
            setTitle(CommonConstants.INSTANCE.displayer_editor_title());
            if (editor.isBrandNewDisplayer()) setTitle(CommonConstants.INSTANCE.displayer_editor_new());
            removeShownHandler();
        }
    };

    private void removeShownHandler() {
        if (this.showHandlerRegistration != null) {
            this.showHandlerRegistration.removeHandler();
            this.showHandlerRegistration = null;
        }
    }

    private final Command cancelCommand = new Command() {
        @Override
        public void execute() {
            hide();
            editor.close();
        }
    };

    private final Command okCommand = new Command() {
        @Override
        public void execute() {
            hide();
            editor.save();
        }
    };

}
