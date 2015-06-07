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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.perspective.editor.resources.i18n.PerspectiveEditorConstants;
import org.dashbuilder.client.widgets.animations.AlphaAnimation;
import org.uberfire.client.workbench.Header;

@ApplicationScoped
public class PerspectiveEditorHeader extends Composite implements Header {

    private static final int ALPHA_ANIMATION_DURATION = 500;

    @Override
    public String getId() {
        return "perspective-editor-header";
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    interface Binder extends UiBinder<Widget, PerspectiveEditorHeader> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    protected Panel headerPanel;
    
    @UiField
    Panel editButtonPanel;
    
    @UiField
    Button editButton;

    @Inject
    protected PerspectiveEditorToolbar editorToolbar;
    
    private boolean isEditVisible = false;

    @PostConstruct
    protected void init() {
        initWidget(uiBinder.createAndBindUi(this));

        // Show when moving the mouse over
        headerPanel.addDomHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent mouseOverEvent) {
                showEditButtonPanel();
            }
        }, MouseOverEvent.getType());
        
        headerPanel.addDomHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent mouseOverEvent) {
                hideEditButtonPanel();
            }
        }, MouseOutEvent.getType());

    }
    
    private void showEditButtonPanel() {
        if (!isEditVisible) {
            isEditVisible = true;
            editButtonPanel.setVisible(false);
            final AlphaAnimation alphaAnimation = new AlphaAnimation(editButtonPanel);
            alphaAnimation.show(ALPHA_ANIMATION_DURATION);
        }
    }

    private void hideEditButtonPanel() {
        if (isEditVisible) {
            isEditVisible = false;
            final AlphaAnimation alphaAnimation = new AlphaAnimation(editButtonPanel);
            alphaAnimation.hide(ALPHA_ANIMATION_DURATION);
        }
    }

    @UiHandler(value = "editButton")
    public void onEdit(final ClickEvent clickEvent) {
        if (editorToolbar.isToolbarOn()) {
            editorToolbar.close();
            editButton.setIcon(IconType.PENCIL);
            editButton.setTitle(PerspectiveEditorConstants.INSTANCE.enableEdit());
        } else {
            editorToolbar.show();
            editButton.setIcon(IconType.EYE_OPEN);
            editButton.setTitle(PerspectiveEditorConstants.INSTANCE.disableEdit());
        }
    }


}
