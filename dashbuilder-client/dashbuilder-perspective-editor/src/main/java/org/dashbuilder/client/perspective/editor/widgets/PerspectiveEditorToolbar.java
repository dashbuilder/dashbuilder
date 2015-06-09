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
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
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
    protected Panel namePanel;

    @UiField
    protected TextBox nameBox;

    @UiField
    protected Button createButton;

    @UiField
    protected Button deleteButton;

    @UiField
    protected Button closeButton;

    @UiField
    protected Panel componentsPanel;

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @Inject
    protected NewPerspectiveForm newPerspectiveForm;

    protected boolean toolbarOn = false;
    protected List<PerspectiveEditorComponent> perspectiveComponentList;
    protected YesNoCancelPopup deletePerspectivePopup;

    @PostConstruct
    public void init() {
        setWidget(uiBinder.createAndBindUi(this));
        setButtonStyle(deleteButton);
        setButtonStyle(createButton);
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
            componentsPanel.add(button);
        }
    }

    protected void setButtonStyle(Button button) {
        button.getElement().getStyle().setMargin(1, Style.Unit.PX);
        button.getElement().getStyle().setWidth(150, Style.Unit.PX);
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
        toolbarDialog.center();
        toolbarDialog.show();
        updateView();
        toolbarOn = true;
    }

    public void close() {
        close(true);
    }

    private void close(final boolean fireEvent) {
        toolbarDialog.hide();

        if (fireEvent) perspectiveEditor.editOff();
        toolbarOn = false;
    }

    public boolean isToolbarOn() {
        return toolbarOn;
    }

    public void setToolbarOn(boolean toolbarOn) {
        this.toolbarOn = toolbarOn;
    }

    @UiHandler("nameBox")
    protected void onPerspectiveNameChanged(ValueChangeEvent<String> event) {
        String name = nameBox.getText();
        if (!StringUtils.isBlank(name)) {
            perspectiveEditor.changePerspectiveName(name);
        } else {
            nameBox.setText(perspectiveEditor.getPerspectiveName());
        }
    }

    @UiHandler("createButton")
    protected void onNewPerspective(ClickEvent event) {
        newPerspectiveForm.init(new NewPerspectiveForm.Listener() {

            public void onOk(String name) {
                perspectiveEditor.newEditablePerspective(name);
                nameBox.setText(name);
            }
            public void onCancel() {
            }
        });
    }

    @UiHandler("deleteButton")
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

    public void updateView() {
        namePanel.setVisible(perspectiveEditor.isEditable());
        deleteButton.setVisible(perspectiveEditor.isEditable());
        componentsPanel.setVisible(perspectiveEditor.isEditable());

        if (perspectiveEditor.isEditable()) {
            nameBox.setText(perspectiveEditor.getPerspectiveName());
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

    protected void onPerspectiveEditOn(@Observes final PerspectiveEditOnEvent event) {
        if (!toolbarOn) {
            show();
        }
    }

    protected void onPerspectiveEditOff(@Observes final PerspectiveEditOffEvent event) {
        if (toolbarOn) {
            close();
        }
    }
}
