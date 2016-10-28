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
package org.dashbuilder.client.cms.widget;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.navigation.event.PerspectivePluginsChangedEvent;
import org.dashbuilder.client.cms.resources.i18n.ContentManagerI18n;
import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.plugin.model.Plugin;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;

@Dependent
public class PerspectivesExplorer implements IsWidget {

    public interface View extends UberView<PerspectivesExplorer> {

        void clear();

        void addPerspective(String name, Command onClicked);

        void showEmpty(String message);
    }

    View view;
    PerspectivePluginManager perspectivePluginManager;
    PlaceManager placeManager;
    ContentManagerI18n i18n;

    @Inject
    public PerspectivesExplorer(View view,
                                PerspectivePluginManager perspectivePluginManager,
                                PlaceManager placeManager,
                                ContentManagerI18n i18n) {
        this.view = view;
        this.perspectivePluginManager = perspectivePluginManager;
        this.placeManager = placeManager;
        this.i18n = i18n;
        this.view.init(this);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void show() {
        view.clear();

        List<Plugin> perspectivePlugins = perspectivePluginManager.getPerspectivePlugins();
        if (perspectivePlugins.isEmpty()) {
            view.showEmpty(i18n.getNoPerspectives());
        } else {
            perspectivePlugins.stream()
                    .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                    .forEach(p -> view.addPerspective(p.getName(), () -> onPerspectiveClick(p)));
        }
    }

    public void onPerspectiveClick(Plugin plugin) {
        PlaceRequest placeRequest = new PathPlaceRequest(plugin.getPath()).addParameter("name", plugin.getName());
        placeManager.goTo(placeRequest);
    }

    public void onPerspectivePluginsChanged(@Observes final PerspectivePluginsChangedEvent event) {
        show();
    }
}