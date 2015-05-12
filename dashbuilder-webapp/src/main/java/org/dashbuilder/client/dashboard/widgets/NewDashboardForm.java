/*
 * Copyright 2012 JBoss Inc
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

package org.dashbuilder.client.dashboard.widgets;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpInline;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.dashboard.DashboardManager;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;

@Dependent
public class NewDashboardForm extends BaseModal {

    public interface Listener {
        void onCancel();
        void onOk(String name);
    }

    interface Binder extends UiBinder<Widget, NewDashboardForm> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    ControlGroup nameGroup;

    @UiField
    TextBox nameTextBox;

    @UiField
    HelpInline nameHelpInline;

    @Inject
    DashboardManager dashboardManager;

    Listener listener;

    public NewDashboardForm() {
        add(uiBinder.createAndBindUi(this));

        ModalFooterOKCancelButtons footer = new ModalFooterOKCancelButtons(
                new Command() {
                    public void execute() {
                        onOKButtonClick();
                    }
                },
                new Command() {
                    public void execute() {
                        onCancelButtonClick();
                    }
                } );

        setTitle("Create new Dashboard");
        footer.enableOkButton(true);
        add(footer);
    }

    public void init(Listener listener) {
        this.listener = listener;

        nameTextBox.setText("");
        nameGroup.setType(ControlGroupType.NONE);
        nameHelpInline.setText("");
        super.show();
    }

    private void onCancelButtonClick() {
        super.hide();
        listener.onCancel();
    }

    private void onOKButtonClick() {
        String name = nameTextBox.getText();

        if (name == null || name.trim().isEmpty()) {
            nameGroup.setType(ControlGroupType.ERROR);
            nameHelpInline.setText("Name is mandatory");
        }
        else if (dashboardManager.getDashboard(name) != null) {
            nameGroup.setType(ControlGroupType.ERROR);
            nameHelpInline.setText("Already exists");
        }
        else {
            super.hide();
            listener.onOk(name);
        }
    }
}
