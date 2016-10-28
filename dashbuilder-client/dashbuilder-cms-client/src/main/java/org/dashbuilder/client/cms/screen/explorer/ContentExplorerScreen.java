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
package org.dashbuilder.client.cms.screen.explorer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.cms.resources.i18n.ContentManagerI18n;
import org.dashbuilder.client.cms.widget.PerspectivesExplorer;
import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.widget.NavTreeEditor;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.plugin.client.security.PluginController;
import org.uberfire.ext.plugin.client.widget.popup.NewPluginPopUp;
import org.uberfire.ext.plugin.model.PluginType;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.events.NotificationEvent;

@ApplicationScoped
@WorkbenchScreen(identifier = ContentExplorerScreen.SCREEN_ID)
public class ContentExplorerScreen {

    public static final String SCREEN_ID = "ContentExplorerScreen";

    public interface View extends UberView<ContentExplorerScreen> {

        void showPerspectives(IsWidget perspectivesExplorer);

        void showMenus(IsWidget menusExplorer);

        void setPerspectivesName(String name);

        void setMenusName(String name);

        void setCreateName(String name);

        void setCreateMenuVisible(boolean visible);

        void addCreateMenuEntry(String name, Command onClick);
    }

    View view;
    NavigationManager navigationManager;
    PerspectivesExplorer perspectiveExplorer;
    NavTreeEditor navTreeEditor;
    NewPluginPopUp newPluginPopUp;
    PluginController pluginController;
    ContentManagerI18n i18n;
    Event<NotificationEvent> workbenchNotification;

    public ContentExplorerScreen() {
    }

    @Inject
    public ContentExplorerScreen(View view,
                                 NavigationManager navigationManager,
                                 PerspectivesExplorer perspectiveExplorer,
                                 NavTreeEditor navTreeEditor,
                                 NewPluginPopUp newPluginPopUp,
                                 PluginController pluginController,
                                 ContentManagerI18n i18n,
                                 Event<NotificationEvent> workbenchNotification) {
        this.view = view;
        this.navigationManager = navigationManager;
        this.perspectiveExplorer = perspectiveExplorer;
        this.navTreeEditor = navTreeEditor;
        this.newPluginPopUp = newPluginPopUp;
        this.pluginController = pluginController;
        this.i18n = i18n;
        this.workbenchNotification = workbenchNotification;
        this.view.init(this);
    }

    @AfterInitialization
    private void init() {
        navTreeEditor.setLiteralPerspective(i18n.capitalizeFirst(i18n.getPerspectiveResourceName()));
        view.setPerspectivesName(i18n.capitalizeFirst(i18n.getPerspectivesResourceName()));
        view.setMenusName(i18n.getContentExplorerMenus());
        view.setCreateName(i18n.getContentExplorerNew());
        view.setCreateMenuVisible(pluginController.canCreatePerspectives());
        view.addCreateMenuEntry(i18n.capitalizeFirst(i18n.getPerspectiveResourceName()), this::createNewPerspective);
        gotoPerspectives();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return i18n.getContentExplorer();
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return view;
    }

    public void createNewPerspective() {
        // TODO: customize new perpsective popup title
        // newPluginPopUp.show(PluginType.PERSPECTIVE_LAYOUT, i18n.getContentManagerHomeNewPerspectiveButton());
        newPluginPopUp.show(PluginType.PERSPECTIVE_LAYOUT);
    }

    public void gotoPerspectives() {
        perspectiveExplorer.show();
        view.showPerspectives(perspectiveExplorer);
    }

    public void gotoMenus() {
        navTreeEditor.setNewGroupEnabled(true);
        navTreeEditor.setNewDividerEnabled(true);
        navTreeEditor.setNewPerspectiveEnabled(true);
        navTreeEditor.setMaxLevels(-1);
        navTreeEditor.setOnlyRuntimePerspectives(false);
        navTreeEditor.setGotoPerspectiveEnabled(true);
        navTreeEditor.setOnChangeCommand(this::onNavTreeChanged);
        navTreeEditor.edit(navigationManager.getNavTree());
        view.showMenus(navTreeEditor);
    }

    private void onNavTreeChanged() {
        navigationManager.saveNavTree(navTreeEditor.getNavTree(), () -> {
            workbenchNotification.fire(new NotificationEvent(i18n.getContentManagerNavigationChanged(), NotificationEvent.NotificationType.SUCCESS));
        });
    }
}