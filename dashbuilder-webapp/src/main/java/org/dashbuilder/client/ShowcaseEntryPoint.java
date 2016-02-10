/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dashbuilder.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.RootPanel;
import org.dashbuilder.client.dashboard.DashboardManager;
import org.dashbuilder.client.dashboard.DashboardPerspectiveActivity;
import org.dashbuilder.client.dashboard.widgets.NewDashboardForm;
import org.dashbuilder.client.resources.i18n.AppConstants;
import org.dashbuilder.shared.dashboard.events.DashboardCreatedEvent;
import org.dashbuilder.shared.dashboard.events.DashboardDeletedEvent;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.views.pfly.menu.UserMenu;
import org.uberfire.client.workbench.widgets.menu.UtilityMenuBar;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBar;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.uberfire.workbench.events.NotificationEvent.NotificationType.INFO;
import static org.uberfire.workbench.model.menu.MenuFactory.newTopLevelMenu;

/**
 * Entry-point for the Dashbuilder showcase
 */
@EntryPoint
public class ShowcaseEntryPoint {

    private AppConstants constants = AppConstants.INSTANCE;

    @Inject
    private WorkbenchMenuBar menubar;

    @Inject
    private PlaceManager placeManager;

    @Inject
    public User identity;

    @Inject
    private UserMenu userMenu;

    @Inject
    private UtilityMenuBar utilityMenuBar;

    @Inject
    private DashboardManager dashboardManager;

    @Inject
    private NewDashboardForm newDashboardForm;

    @Inject
    private Caller<AuthenticationService> authService;

    @Inject
    private Event<NotificationEvent> workbenchNotification;

    @AfterInitialization
    public void startApp() {
        dashboardManager.loadDashboards(new ParameterizedCommand<Set<DashboardPerspectiveActivity>>() {
            public void execute(Set<DashboardPerspectiveActivity> parameter) {
                setupMenus();
                hideLoadingPopup();
            }
        });
    }

    private void setupMenus() {
        for (Menus roleMenus : getRoles()) {
            userMenu.addMenus(roleMenus);
        }
        refreshMenus();
    }

    private void refreshMenus() {
        menubar.clear();
        menubar.addMenus(createMenuBar());

        final Menus utilityMenus =
                MenuFactory.newTopLevelCustomMenu(userMenu)
                        .endMenu()
                        .build();

        utilityMenuBar.addMenus(utilityMenus);
    }

    private Menus createMenuBar() {
        return newTopLevelMenu(constants.menu_home())
                .place(new DefaultPlaceRequest("HomePerspective"))
                .endMenu().
                newTopLevelMenu(constants.menu_gallery())
                .place(new DefaultPlaceRequest("DisplayerGalleryPerspective")).endMenu().
                newTopLevelMenu(constants.menu_authoring())
                .withItems(getAuthoringMenuItems())
                .endMenu().
                newTopLevelMenu(constants.menu_dashboards())
                .withItems(getDashboardMenuItems())
                .endMenu().
                newTopLevelMenu(constants.menu_extensions())
                .withItems(getExtensionsMenuItems())
                .endMenu().
                build();
    }

    private List<Menus> getRoles() {
        final List<Menus> result = new ArrayList<Menus>(identity.getRoles().size());
        result.add(MenuFactory.newSimpleItem(constants.logOut()).respondsWith(new LogoutCommand()).endMenu().build());
        for (Role role : identity.getRoles()) {
            if (!role.getName().equals( "IS_REMEMBER_ME")) {
                result.add(MenuFactory.newSimpleItem(constants.role() + ": " + role.getName() ).endMenu().build());
            }
        }
        return result;
    }

    private List<? extends MenuItem> getAuthoringMenuItems() {
        final List<MenuItem> result = new ArrayList(2);
        result.add(newMenuItem(constants.menu_dataset_authoring(), "DataSetAuthoringPerspective"));
        return result;
    }

    private List<? extends MenuItem> getDashboardMenuItems() {
        final List<MenuItem> result = new ArrayList<>(2);

        // Add the new dashboard creation link
        result.add(MenuFactory.newSimpleItem(constants.menu_dashboards_new())
                .respondsWith(getNewDashboardCommand())
                .endMenu().build().getItems().get(0));

        // Add hard-coded dashboard samples
        result.add(newMenuItem(constants.menu_dashboards_salesdb(), "SalesDashboardPerspective"));
        result.add(newMenuItem(constants.menu_dashboards_salesreports(), "SalesReportsPerspective"));

        // Add dashboards created in runtime
        for (DashboardPerspectiveActivity activity : dashboardManager.getDashboards()) {
            result.add(newMenuItem(activity.getDisplayName(), activity.getIdentifier(), true, false));
        }

        return result;
    }

    private Command getNewDashboardCommand() {
        return new Command() {
            public void execute() {
                newDashboardForm.init(new NewDashboardForm.Listener() {

                    public void onOk(String name) {
                        dashboardManager.newDashboard(name);
                    }
                    public void onCancel() {
                    }
                });
            }
        };
    }

    private void onDashboardCreatedEvent(@Observes DashboardCreatedEvent event) {
        refreshMenus();

        // Navigate to the activity after rebuilding the menu bar entries, if not, the activity perspective menus are override by uf and they do not appear.
        placeManager.goTo(new DefaultPlaceRequest(event.getDashboardId()));
        workbenchNotification.fire(new NotificationEvent(constants.notification_dashboard_created(event.getDashboardName()), INFO));
    }

    private void onDashboardDeletedEvent(@Observes DashboardDeletedEvent event) {
        refreshMenus();
        workbenchNotification.fire(new NotificationEvent(constants.notification_dashboard_deleted(event.getDashboardName()), INFO));
    }

    private List<? extends MenuItem> getExtensionsMenuItems() {
        final List<MenuItem> result = new ArrayList<>(2);
        result.add(newMenuItem(constants.menu_extensions_plugins(), "PlugInAuthoringPerspective"));
        result.add(newMenuItem(constants.menu_extensions_apps(), "AppsPerspective"));
        return result;
    }
    
    private MenuItem newMenuItem(String caption, final String activityId) {
        return newMenuItem(caption, activityId, false, false);
    }

    private MenuItem newMenuItem(String caption, final String activityId, final boolean showSubMenu, final boolean showLogo) {
        return MenuFactory.newSimpleItem(caption)
                .place(new DefaultPlaceRequest(activityId))
                .endMenu().build().getItems().get(0);
    }

    // Fade out the "Loading application" pop-up
    private void hideLoadingPopup() {
        final Element e = RootPanel.get("loading").getElement();

        new Animation() {

            @Override
            protected void onUpdate( double progress ) {
                e.getStyle().setOpacity( 1.0 - progress );
            }

            @Override
            protected void onComplete() {
                e.getStyle().setVisibility( Style.Visibility.HIDDEN );
            }
        }.run( 500 );
    }

    private class LogoutCommand implements Command {

        @Override
        public void execute() {
            authService.call(new RemoteCallback<Void>() {
                @Override
                public void callback(Void response) {
                    final String location = GWT.getModuleBaseURL().replaceFirst( "/" + GWT.getModuleName() + "/", "/logout.jsp" );
                    redirect( location );
                }
            }).logout();
        }
    }

    public static native void redirect( String url )/*-{
        $wnd.location = url;
    }-*/;
}