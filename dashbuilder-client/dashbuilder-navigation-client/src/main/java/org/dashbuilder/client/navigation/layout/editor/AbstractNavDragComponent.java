/*
 * Copyright 2017 JBoss, by Red Hat, Inc
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
package org.dashbuilder.client.navigation.layout.editor;

import java.util.Collections;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.widget.NavItemSelectionModal;
import org.dashbuilder.client.navigation.widget.NavItemSelectionModalView;
import org.dashbuilder.client.navigation.widget.NavWidget;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.NavTree;
import org.gwtbootstrap3.client.ui.Modal;
import org.uberfire.ext.layout.editor.client.api.ModalConfigurationContext;
import org.uberfire.ext.layout.editor.client.api.RenderingContext;

public abstract class AbstractNavDragComponent implements NavDragComponent {

    NavigationManager navigationManager;
    NavDragComponentRegistry navDragComponentRegistry;
    NavItemSelectionModal navItemSelectionModal;
    NavWidget navWidget;
    String navGroupId = null;

    public static final String NAV_GROUP_ID = "navGroupId";

    public AbstractNavDragComponent() {
    }

    public AbstractNavDragComponent(NavigationManager navigationManager,
                                    NavDragComponentRegistry navDragComponentRegistry,
                                    NavItemSelectionModal navItemSelectionModal,
                                    NavWidget navWidget) {
        this.navigationManager = navigationManager;
        this.navDragComponentRegistry = navDragComponentRegistry;
        this.navItemSelectionModal = navItemSelectionModal;
        this.navWidget = navWidget;
        this.navWidget.setOnStaleCommand(this::showNavWidget);
        this.navDragComponentRegistry.checkIn(this);
    }

    @Override
    public IsWidget getPreviewWidget(RenderingContext ctx) {
        return getShowWidget(ctx);
    }

    @Override
    public IsWidget getShowWidget(RenderingContext ctx) {
        Map<String, String> properties = ctx.getComponent().getProperties();
        navGroupId = properties.get(NAV_GROUP_ID);
        this.showNavWidget();
        return navWidget;
    }

    @Override
    public Modal getConfigurationModal(ModalConfigurationContext ctx) {
        navItemSelectionModal.setHelpHint(getDragComponentHelp());
        navItemSelectionModal.setOnlyGroups(true);
        navItemSelectionModal.setOnOk(() -> navGroupSelectionOk(ctx));
        navItemSelectionModal.setOnCancel(() -> navGroupSelectionCancel(ctx));

        NavTree navTree = navigationManager.getNavTree();
        String groupId = ctx.getComponentProperty(NAV_GROUP_ID);
        navItemSelectionModal.show(navTree.getRootItems(), groupId);
        return ((NavItemSelectionModalView) navItemSelectionModal.getView()).getModal();
    }

    @Override
    public void dispose() {
        navWidget.dispose();
    }

    protected NavGroup fetchNavGroup() {
        NavTree navTree = navigationManager.getNavTree();
        return (NavGroup) navTree.getItemById(navGroupId);
    }

    protected void showNavWidget() {
        NavGroup navGroup = fetchNavGroup();
        if (navGroup != null) {
            navWidget.show(navGroup);
        } else {
            navWidget.show(Collections.emptyList());
        }
    }

    protected void navGroupSelectionOk(ModalConfigurationContext ctx) {
        NavItem navItem = navItemSelectionModal.getSelectedItem();
        ctx.setComponentProperty(NAV_GROUP_ID, navItem.getId());
        ctx.configurationFinished();
    }

    protected void navGroupSelectionCancel(ModalConfigurationContext ctx) {
        ctx.configurationCancelled();
    }
}
