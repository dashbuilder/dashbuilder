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
import org.dashbuilder.client.navigation.widget.HasIdentifier;
import org.dashbuilder.client.navigation.widget.HasMaxLevels;
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
    NavDragComponentRegistry navDragComponentRegistry;
    NavComponentConfigModal navComponentConfigModal;
    NavWidget navWidget;
    String navId = null;
    String navPoint = null;
    String navGroupId = null;

    public static final String NAV_ID = "navComponentId";
    public static final String NAV_POINT = "navPoint";
    public static final String NAV_GROUP_ID = "navGroupId";
    public static final String TARGET_DIV_ID = "targetDivId";
    public static final String NAV_ITEM_ID = "navItemId";
    public static final String MAX_LEVELS = "maxLevels";

    public AbstractNavDragComponent() {
    }

    public AbstractNavDragComponent(NavigationManager navigationManager,
                                    NavDragComponentRegistry navDragComponentRegistry,
                                    NavComponentConfigModal navComponentConfigModal,
                                    NavWidget navWidget) {
        this.navigationManager = navigationManager;
        this.navDragComponentRegistry = navDragComponentRegistry;
        this.navComponentConfigModal = navComponentConfigModal;
        this.navWidget = navWidget;
        this.navWidget.setOnStaleCommand(this::showNavWidget);
    }

    @Override
    public String getNavId() {
        return navId;
    }

    @Override
    public String getNavPoint() {
        return navPoint;
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
        navId = properties.get(NAV_ID);
        navPoint = properties.get(NAV_POINT);
        navGroupId = properties.get(NAV_GROUP_ID);

        if (navWidget instanceof HasTargetDiv) {
           String targetDivId = properties.get(TARGET_DIV_ID);
            ((HasTargetDiv) navWidget).setTargetDivId(targetDivId);
        }
        if (navWidget instanceof HasDefaultNavItem) {
            String navItemId = properties.get(NAV_ITEM_ID);
            ((HasDefaultNavItem) navWidget).setDefaultNavItemId(navItemId);
        }
        if (navWidget instanceof HasMaxLevels) {
            String maxLevels = properties.get(MAX_LEVELS);
            navWidget.setMaxLevels(maxLevels != null ? Integer.parseInt(maxLevels) : -1);
            navWidget.setHideEmptyGroups(false);
        }
        navDragComponentRegistry.checkIn(this);
        this.showNavWidget();
        return navWidget;
    }

    @Override
    public Modal getConfigurationModal(ModalConfigurationContext ctx) {
        navComponentConfigModal.clear();

        NavTree navTree = navigationManager.getNavTree();
        String navPoint = ctx.getComponentProperty(NAV_POINT);
        String groupId = ctx.getComponentProperty(NAV_GROUP_ID);
        boolean supportsIdenfitier = navWidget instanceof HasIdentifier;
        boolean supportsDefaultNavItem = navWidget instanceof HasDefaultNavItem;
        boolean supportsTargetDiv = navWidget instanceof HasTargetDiv;
        boolean supportsMaxLevels = navWidget instanceof HasMaxLevels;
        navComponentConfigModal.setLayoutTemplate(ctx.getCurrentLayoutTemplate());
        navComponentConfigModal.setNavIdSupported(supportsIdenfitier);
        navComponentConfigModal.setNavPoint(navPoint);
        navComponentConfigModal.setTargetDivSupported(supportsTargetDiv);
        navComponentConfigModal.setDefaultNavItemSupported(supportsDefaultNavItem);
        navComponentConfigModal.setMaxLevelsSupported(supportsMaxLevels);
        navComponentConfigModal.setNavGroup(navTree.getRootItems(), groupId);

        if (supportsIdenfitier) {
            String navId = ctx.getComponentProperty(NAV_ID);
            navComponentConfigModal.setNavId(navId);
        }
        if (supportsDefaultNavItem) {
            String navItemId = ctx.getComponentProperty(NAV_ITEM_ID);
            navComponentConfigModal.setDefaultNavItemId(navItemId);
        }
        if (supportsTargetDiv) {
            String targetDivId = ctx.getComponentProperty(TARGET_DIV_ID);
            navComponentConfigModal.setTargetDiv(targetDivId);
        }
        if (supportsMaxLevels) {
            String maxLevels = ctx.getComponentProperty(MAX_LEVELS);
            navComponentConfigModal.setMaxLevels(maxLevels != null ? Integer.parseInt(maxLevels) : -1);
        }

        navComponentConfigModal.setNavGroupHelpHint(getDragComponentNavGroupHelp());
        navComponentConfigModal.setOnOk(() -> navGroupSelectionOk(ctx,
                supportsDefaultNavItem,
                supportsTargetDiv,
                supportsMaxLevels));
        navComponentConfigModal.setOnCancel(() -> navGroupSelectionCancel(ctx));
        navComponentConfigModal.show();
        return ((NavComponentConfigModalView) navComponentConfigModal.getView()).getModal();
    }

    @Override
    public void dispose() {
        navWidget.dispose();
    }

    protected void showNavWidget() {
        if (navPoint == null) {
            NavTree navTree = navigationManager.getNavTree();
            NavGroup navGroup = (NavGroup) navTree.getItemById(navGroupId);
            if (navGroup != null) {
                navWidget.show(navGroup);
            } else {
                navWidget.show(Collections.emptyList());
            }
        }
    }

    protected void navGroupSelectionOk(ModalConfigurationContext ctx,
                                       boolean supportsDefaultNavItem,
                                       boolean supportsTargetDiv,
                                       boolean supportsNLevels) {

        String navId = navComponentConfigModal.getNavId();
        String navPoint = navComponentConfigModal.getNavPoint();
        String groupId = navComponentConfigModal.getGroupId();
        String defaultItemId = navComponentConfigModal.getDefaultItemId();
        String targetDivId = navComponentConfigModal.getTargetDivId();
        int maxLevels = navComponentConfigModal.getMaxLevels();

        if (navId != null) {
            ctx.setComponentProperty(NAV_ID, navId);
        } else {
            ctx.removeComponentProperty(NAV_ID);
        }

        if (navPoint != null) {
            ctx.setComponentProperty(NAV_POINT, navPoint);
            ctx.removeComponentProperty(NAV_GROUP_ID);
        } else {
            ctx.setComponentProperty(NAV_GROUP_ID, groupId);
            ctx.removeComponentProperty(NAV_POINT);
        }

        if (supportsDefaultNavItem && defaultItemId != null) {
            ctx.setComponentProperty(NAV_ITEM_ID, defaultItemId);
        } else {
            ctx.removeComponentProperty(NAV_ITEM_ID);
        }

        if (supportsTargetDiv && targetDivId != null) {
            ctx.setComponentProperty(TARGET_DIV_ID, targetDivId);
        } else {
            ctx.removeComponentProperty(TARGET_DIV_ID);
        }

        if (supportsNLevels && maxLevels > 0) {
            ctx.setComponentProperty(MAX_LEVELS, Integer.toString(maxLevels));
        } else {
            ctx.removeComponentProperty(MAX_LEVELS);
        }

        ctx.configurationFinished();
    }

    protected void navGroupSelectionCancel(ModalConfigurationContext ctx) {
        ctx.configurationCancelled();
    }
}
