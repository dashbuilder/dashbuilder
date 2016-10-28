/*
 * Copyright 2016 JBoss, by Red Hat, Inc
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.dashbuilder.navigation.NavItem;
import org.uberfire.ext.plugin.event.PluginSaved;
import org.uberfire.ext.plugin.model.Plugin;

@Dependent
public class NavCarouselWidget extends BaseNavWidget {

    public interface View extends NavWidgetView<NavCarouselWidget> {

        void addContentSlide(IsWidget widget);

        void recursivityError();
    }

    View view;
    PerspectivePluginManager perspectivePluginManager;
    List<String> perspectiveIds = new ArrayList<>();

    @Inject
    public NavCarouselWidget(View view, NavigationManager navigationManager, PerspectivePluginManager perspectivePluginManager) {
        super(view, navigationManager);
        this.view = view;
        this.perspectivePluginManager = perspectivePluginManager;
        super.setMaxLevels(1);
    }

    @Override
    public boolean areSubGroupsSupported() {
        return false;
    }

    @Override
    public void show(List<NavItem> itemList) {
        // Discard everything but runtime perspectives
        List<NavItem> itemsFiltered = itemList.stream()
                .filter(perspectivePluginManager::isRuntimePerspective)
                .collect(Collectors.toList());

        perspectiveIds.clear();
        super.show(itemsFiltered);
    }

    @Override
    protected void showItem(NavItem navItem) {
        // Only runtime perspectives can be displayed
        String perspectiveId = perspectivePluginManager.getRuntimePerspectiveId(navItem);
        if (perspectiveId != null) {
            perspectiveIds.add(perspectiveId);
            perspectivePluginManager.buildPerspectiveWidget(perspectiveId, this::showWidget, this::recursivityError);
        }
    }

    public void showWidget(IsWidget widget) {
        view.addContentSlide(widget);
    }

    private void recursivityError() {
        view.recursivityError();
    }

    // Catch changes on runtime perspectives so as to display the most up to date changes

    private void onPerspectiveChanged(@Observes PluginSaved event) {
        Plugin plugin = event.getPlugin();
        String pluginName = plugin.getName();
        if (perspectiveIds.contains(pluginName)) {
            super.refresh();
        }
    }
}