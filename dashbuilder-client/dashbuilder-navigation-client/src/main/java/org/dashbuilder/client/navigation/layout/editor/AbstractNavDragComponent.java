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
import org.dashbuilder.client.navigation.widget.HasDefaultNavItem;
import org.dashbuilder.client.navigation.widget.HasTargetDiv;
import org.dashbuilder.client.navigation.widget.NavComponentConfigModal;
import org.dashbuilder.client.navigation.widget.NavComponentConfigModalView;
import org.dashbuilder.client.navigation.widget.NavWidget;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavTree;
import org.gwtbootstrap3.client.ui.Modal;
import org.uberfire.ext.layout.editor.client.api.ModalConfigurationContext;
import org.uberfire.ext.layout.editor.client.api.RenderingContext;

public abstract class AbstractNavDragComponent implements NavDragComponent {

    NavigationManager navigationManager;
    NavComponentConfigModal navComponentConfigModal;
    NavWidget navWidget;
    String navGroupId = null;

    public static final String NAV_GROUP_ID = "navGroupId";
    public static final String NAV_DEFAULT_ID = "navDefaultId";
    public static final String TARGET_DIV_ID = "targetDivId";

    public AbstractNavDragComponent() {
    }

    public AbstractNavDragComponent(NavigationManager navigationManager,
                                    NavComponentConfigModal navComponentConfigModal,
                                    NavWidget navWidget) {
        this.navigationManager = navigationManager;
        this.navComponentConfigModal = navComponentConfigModal;
        this.navWidget = navWidget;
        this.navWidget.setOnStaleCommand(this::showNavWidget);
    }

    @Override
    public NavWidget getNavWidget() {
        return navWidget;
    }

    @Override
    public IsWidget getPreviewWidget(RenderingContext ctx) {
        return getShowWidget(ctx);
    }

    @Override
    public IsWidget getShowWidget(RenderingContext ctx) {
        Map<String, String> properties = ctx.getComponent().getProperties();
        navGroupId = properties.get(NAV_GROUP_ID);
        navWidget.setHideEmptyGroups(true);

        if (navWidget instanceof HasTargetDiv) {
           String targetDivId = properties.get(TARGET_DIV_ID);
            ((HasTargetDiv) navWidget).setTargetDivId(targetDivId);
        }
        if (navWidget instanceof HasDefaultNavItem) {
            String navItemId = properties.get(NAV_DEFAULT_ID);
            ((HasDefaultNavItem) navWidget).setDefaultNavItemId(navItemId);
        }
        this.showNavWidget();
        return navWidget;
    }

    @Override
    public Modal getConfigurationModal(ModalConfigurationContext ctx) {
        navComponentConfigModal.clear();

        NavTree navTree = navigationManager.getNavTree();
        String groupId = ctx.getComponentProperty(NAV_GROUP_ID);
        boolean supportsDefaultNavItem = navWidget instanceof HasDefaultNavItem;
        boolean supportsTargetDiv = navWidget instanceof HasTargetDiv;
        navComponentConfigModal.setLayoutTemplate(ctx.getCurrentLayoutTemplate());
        navComponentConfigModal.setTargetDivSupported(supportsTargetDiv);
        navComponentConfigModal.setDefaultNavItemSupported(supportsDefaultNavItem);
        navComponentConfigModal.setNavGroup(navTree.getRootItems(), groupId);

        if (supportsDefaultNavItem) {
            String navItemId = ctx.getComponentProperty(NAV_DEFAULT_ID);
            navComponentConfigModal.setDefaultNavItemId(navItemId);
        }
        if (supportsTargetDiv) {
            String targetDivId = ctx.getComponentProperty(TARGET_DIV_ID);
            navComponentConfigModal.setTargetDiv(targetDivId);
        }

        navComponentConfigModal.setNavGroupHelpHint(getDragComponentNavGroupHelp());
        navComponentConfigModal.setOnOk(() -> navConfigOk(ctx, supportsDefaultNavItem, supportsTargetDiv));
        navComponentConfigModal.setOnCancel(() -> navConfigCancel(ctx));
        navComponentConfigModal.show();
        return ((NavComponentConfigModalView) navComponentConfigModal.getView()).getModal();
    }

    @Override
    public void dispose() {
        navWidget.dispose();
    }

    protected void showNavWidget() {
        NavTree navTree = navigationManager.getNavTree();
        NavGroup navGroup = (NavGroup) navTree.getItemById(navGroupId);
        if (navGroup != null) {
            navWidget.show(navGroup);
        } else {
            navWidget.show(Collections.emptyList());
        }
    }

    protected void navConfigOk(ModalConfigurationContext ctx, boolean supportsDefaultNavItem, boolean supportsTargetDiv) {

        navGroupId = navComponentConfigModal.getGroupId();
        if (navGroupId != null) {
            ctx.setComponentProperty(NAV_GROUP_ID, navGroupId);
        } else {
            ctx.removeComponentProperty(NAV_GROUP_ID);
        }

        String defaultItemId = navComponentConfigModal.getDefaultItemId();
        if (supportsDefaultNavItem && defaultItemId != null) {
            ctx.setComponentProperty(NAV_DEFAULT_ID, defaultItemId);
        } else {
            ctx.removeComponentProperty(NAV_DEFAULT_ID);
        }

        String targetDivId = navComponentConfigModal.getTargetDivId();
        if (supportsTargetDiv && targetDivId != null) {
            ctx.setComponentProperty(TARGET_DIV_ID, targetDivId);
        } else {
            ctx.removeComponentProperty(TARGET_DIV_ID);
        }

        ctx.configurationFinished();
    }

    protected void navConfigCancel(ModalConfigurationContext ctx) {
        ctx.configurationCancelled();
    }
}
