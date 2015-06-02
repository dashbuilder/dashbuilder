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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.PerspectiveEditorComponent;
import org.dashbuilder.common.client.StringUtils;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.uberfire.client.workbench.events.PerspectiveChange;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.mvp.Command;

@ApplicationScoped
public class PerspectiveEditorToolbar extends DialogBox {

    interface Binder extends UiBinder<Widget, PerspectiveEditorToolbar> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    protected DialogBox toolbarDialog;

    @UiField
    protected TextBox perspectiveNameBox;

    @UiField
    protected Button newPerspectiveButton;

    @UiField
    protected Button deletePerspectiveButton;

    @UiField
    protected Button closeButton;

    @UiField
    protected Panel newComponentsPanel;

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @Inject
    protected NewPerspectiveForm newPerspectiveForm;

    protected boolean toolbarOn = false;
    protected List<PerspectiveEditorComponent> perspectiveComponentList;
    protected YesNoCancelPopup deletePerspectivePopup;

    @PostConstruct
    protected void init() {
        setWidget(uiBinder.createAndBindUi(this));
        setButtonStyle(deletePerspectiveButton);
        setButtonStyle(newPerspectiveButton);
        setButtonStyle(closeButton);

        // Register the perspective editor components
        perspectiveComponentList = lookupPerspectiveComponents();
        for (final PerspectiveEditorComponent component : perspectiveComponentList) {
            Button button = new Button("New " + component.getComponentName());
            button.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent clickEvent) {
                    component.createNewInstance();
                }
            });
            setButtonStyle(button);
            newComponentsPanel.add(button);
        }

        // Show when moving the mouse over
        toolbarDialog.addDomHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent mouseOverEvent) {
                show();
            }
        }, MouseOverEvent.getType());
    }

    protected void setButtonStyle(Button button) {
        button.getElement().getStyle().setWidth(170, Style.Unit.PX);
        button.getElement().getStyle().setTextAlign(Style.TextAlign.LEFT);
    }
    protected List<PerspectiveEditorComponent> lookupPerspectiveComponents() {
        List<PerspectiveEditorComponent> result = new ArrayList<PerspectiveEditorComponent>();
        Collection<IOCBeanDef<PerspectiveEditorComponent>> beanDefs = IOC.getBeanManager().lookupBeans(PerspectiveEditorComponent.class);
        for (IOCBeanDef<PerspectiveEditorComponent> beanDef : beanDefs) {
            PerspectiveEditorComponent perspectiveComponent = beanDef.getInstance();
            result.add(perspectiveComponent);
        }
        return result;
    }

    public void show() {
        toolbarDialog.show();
        updateView();
    }

    public void close() {
        toolbarDialog.hide();

        perspectiveEditor.editOff();
        toolbarOn = false;
    }

    @UiHandler("perspectiveNameBox")
    protected void onPerspectiveNameChanged(ValueChangeEvent<String> event) {
        String name = perspectiveNameBox.getText();
        if (!StringUtils.isBlank(name)) {
            perspectiveEditor.changePerspectiveName(name);
        } else {
            perspectiveNameBox.setText(perspectiveEditor.getPerspectiveName());
        }
    }

    @UiHandler("newPerspectiveButton")
    protected void onNewPerspective(ClickEvent event) {
        newPerspectiveForm.init(new NewPerspectiveForm.Listener() {

            public void onOk(String name) {
                perspectiveEditor.newEditablePerspective(name);
                perspectiveNameBox.setText(name);
            }
            public void onCancel() {
            }
        });
    }

    @UiHandler("deletePerspectiveButton")
    protected void onDeletePerspective(ClickEvent event) {
        deletePerspectivePopup = YesNoCancelPopup.newYesNoCancelPopup(
                "Delete perspective",
                "Are you sure?",
                getDoDeleteCommand(),
                getCancelDeleteCommand(),
                null);
        deletePerspectivePopup.show();
    }

    protected Command getCancelDeleteCommand() {
        return new org.uberfire.mvp.Command() {
            public void execute() {
                deletePerspectivePopup.hide();
            }
        };
    }

    protected Command getDoDeleteCommand() {
        return new org.uberfire.mvp.Command() {
            public void execute() {
                deletePerspectivePopup.hide();
                perspectiveEditor.deleteCurrentPerspective();
            }
        };
    }

    @UiHandler("closeButton")
    protected void onCloseToolbar(ClickEvent event) {
        close();
    }

    protected void updateView() {
        perspectiveNameBox.setVisible(perspectiveEditor.isEditable());
        deletePerspectiveButton.setVisible(perspectiveEditor.isEditable());
        newComponentsPanel.setVisible(perspectiveEditor.isEditable());

        if (perspectiveEditor.isEditable()) {
            perspectiveNameBox.setText(perspectiveEditor.getPerspectiveName());
            perspectiveEditor.editOn();
        }
    }

    /**
     * Close the toolbar when the perspective changes
     */
    protected void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        if (toolbarOn) {
            updateView();
        }
    }
}
