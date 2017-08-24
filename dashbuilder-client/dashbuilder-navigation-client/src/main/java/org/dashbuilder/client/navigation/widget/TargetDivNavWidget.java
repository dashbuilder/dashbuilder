/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.workbench.NavWorkbenchCtx;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.plugin.event.PluginSaved;
import org.uberfire.ext.plugin.model.Plugin;

/**
 * Base class for nav widgets that uses a target div to show a nav item's content once clicked.
 */
public abstract class TargetDivNavWidget extends BaseNavWidget implements HasTargetDiv, HasDefaultNavItem {

    public interface View<T extends TargetDivNavWidget> extends NavWidgetView<T> {

        void clearContent(String targetDivId);

        void showContent(String targetDivId, IsWidget content);

        void deadlockError(String targetDivId);
    }

    View view;
    PerspectivePluginManager pluginManager;
    PlaceManager placeManager;
    String targetDivId = null;
    String defaultNavItemId = null;

    @Inject
    public TargetDivNavWidget(View view,
                              PerspectivePluginManager pluginManager,
                              PlaceManager placeManager,
                              NavigationManager navigationManager) {
        super(view, navigationManager);
        this.view = view;
        this.pluginManager = pluginManager;
        this.placeManager = placeManager;
    }

    @Override
    public String getTargetDivId() {
        return targetDivId;
    }

    @Override
    public void setTargetDivId(String targetDivId) {
        this.targetDivId = targetDivId;
    }

    @Override
    public String getDefaultNavItemId() {
        return defaultNavItemId;
    }

    @Override
    public void setDefaultNavItemId(String defaultNavItemId) {
        this.defaultNavItemId = defaultNavItemId;
    }

    @Override
    public void show(List<NavItem> itemList) {
        super.show(itemList);
        gotoDefaultItem();
    }

    protected boolean gotoDefaultItem() {
        if (getDefaultNavItemId() != null) {
            if (setSelectedItem(getDefaultNavItemId())) {
                gotoNavItem(true);
                if (onItemSelectedCommand != null) {
                    onItemSelectedCommand.execute();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onItemClicked(NavItem navItem) {
        super.onItemClicked(navItem);
        this.gotoNavItem(false);
    }

    @Override
    public void onSubGroupItemClicked(NavWidget subGroup) {
        super.onSubGroupItemClicked(subGroup);
        this.gotoNavItem(false);
    }

    protected void gotoNavItem(boolean onlyRuntimePerspectives) {
        if (targetDivId != null) {
            NavWorkbenchCtx navCtx = NavWorkbenchCtx.get(getItemSelected());
            String resourceId = navCtx.getResourceId();
            String navRootId = navCtx.getNavGroupId();
            if (resourceId != null) {
                if (pluginManager.isRuntimePerspective(resourceId)) {
                    pluginManager.buildPerspectiveWidget(resourceId, navRootId,
                            w -> view.showContent(targetDivId, w),
                            () -> view.deadlockError(targetDivId));
                }
                else if (!onlyRuntimePerspectives) {
                    placeManager.goTo(resourceId);
                }
            } else {
                view.clearContent(targetDivId);
            }
        }
    }

    // Catch changes on runtime perspectives so as to display the most up to date changes

    private void onPluginSaved(@Observes PluginSaved event) {
        Plugin plugin = event.getPlugin();
        String pluginName = plugin.getName();
        String selectedPerspectiveId = pluginManager.getRuntimePerspectiveId(itemSelected);
        if (selectedPerspectiveId != null && selectedPerspectiveId.equals(pluginName)) {
            gotoNavItem(true);
        }
    }
}
