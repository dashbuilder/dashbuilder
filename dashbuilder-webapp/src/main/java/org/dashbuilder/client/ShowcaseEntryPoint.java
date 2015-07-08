/*
 * Copyright 2012 JBoss Inc
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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.dashboard.DashboardManager;
import org.dashbuilder.client.dashboard.DashboardPerspectiveActivity;
import org.dashbuilder.client.dashboard.widgets.NewDashboardForm;
import org.dashbuilder.client.resources.AppResource;
import org.dashbuilder.client.resources.i18n.AppConstants;
import org.dashbuilder.shared.dashboard.events.DashboardCreatedEvent;
import org.dashbuilder.shared.dashboard.events.DashboardDeletedEvent;
import org.gwtbootstrap3.client.ui.Image;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.views.pfly.menu.MainBrand;
import org.uberfire.client.workbench.events.ApplicationReadyEvent;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBar;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.uberfire.workbench.events.NotificationEvent.NotificationType.INFO;
import static org.uberfire.workbench.model.menu.MenuFactory.newTopLevelMenu;

/**
 * Entry-point for the Dashbuilder showcase
 */
@EntryPoint
public class ShowcaseEntryPoint {

    @Inject
    private WorkbenchMenuBar menubar;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private DashboardManager dashboardManager;

    @Inject
    private NewDashboardForm newDashboardForm;

    @Inject
    private Event<NotificationEvent> workbenchNotification;

    @PostConstruct
    public void startApp() {
        hideLoadingPopup();
    }

    private void setupMenu( @Observes final ApplicationReadyEvent event ) {
        menubar.addMenus(createMenuBar());
    }

    private Menus createMenuBar() {
        return newTopLevelMenu(AppConstants.INSTANCE.menu_home()).respondsWith(new Command() {
                public void execute() {
                    placeManager.goTo("HomePerspective");
                }}).endMenu().
                newTopLevelMenu(AppConstants.INSTANCE.menu_gallery()).respondsWith(new Command() {
                public void execute() {
                    placeManager.goTo("DisplayerGalleryPerspective");
                }}).endMenu().
                newTopLevelMenu(AppConstants.INSTANCE.menu_authoring())
                .withItems(getAuthoringMenuItems())
                .endMenu().
                newTopLevelMenu(AppConstants.INSTANCE.menu_dashboards())
                .withItems(getDashboardMenuItems())
                .endMenu().
                newTopLevelMenu(AppConstants.INSTANCE.menu_extensions())
                .withItems(getExtensionsMenuItems())
                .endMenu().
                build();
    }

    private List<? extends MenuItem> getAuthoringMenuItems() {
        final List<MenuItem> result = new ArrayList<MenuItem>(2);
        result.add(newMenuItem(AppConstants.INSTANCE.menu_dataset_authoring(), "DataSetAuthoringPerspective"));
        return result;
    }

    private List<? extends MenuItem> getDashboardMenuItems() {
        final List<MenuItem> result = new ArrayList<MenuItem>(2);

        // Add the new dashboard creation link
        result.add(MenuFactory.newSimpleItem(AppConstants.INSTANCE.menu_dashboards_new())
                .respondsWith(getNewDashboardCommand())
                .endMenu().build().getItems().get(0));

        // Add hard-coded dashboard samples
        result.add(newMenuItem(AppConstants.INSTANCE.menu_dashboards_salesdb(), "SalesDashboardPerspective"));
        result.add(newMenuItem(AppConstants.INSTANCE.menu_dashboards_salesreports(), "SalesReportsPerspective"));

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
        menubar.clear();
        menubar.addMenus(createMenuBar());
        //workbenchNotification.fire(new NotificationEvent(AppConstants.INSTANCE.notification_dashboard_created(event.getDashboardName()), INFO));
    }

    private void onDashboardDeletedEvent(@Observes DashboardDeletedEvent event) {
        menubar.clear();
        menubar.addMenus(createMenuBar());
        workbenchNotification.fire(new NotificationEvent(AppConstants.INSTANCE.notification_dashboard_deleted(event.getDashboardName()), INFO));
    }

    private List<? extends MenuItem> getExtensionsMenuItems() {
        final List<MenuItem> result = new ArrayList<MenuItem>(2);
        result.add(newMenuItem(AppConstants.INSTANCE.menu_extensions_plugins(), "PlugInAuthoringPerspective"));
        result.add(newMenuItem(AppConstants.INSTANCE.menu_extensions_apps(), "AppsPerspective"));
        return result;
    }
    
    private MenuItem newMenuItem(String caption, final String activityId) {
        return newMenuItem(caption, activityId, false, false);
    }

    private MenuItem newMenuItem(String caption, final String activityId, final boolean showSubMenu, final boolean showLogo) {
        return MenuFactory.newSimpleItem(caption).respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(activityId);
            }
        }).endMenu().build().getItems().get(0);
    }

    // Fade out the "Loading application" pop-up
    private void hideLoadingPopup() {
        final Element e = RootPanel.get(AppConstants.INSTANCE.loading()).getElement();

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

    @Produces
    @ApplicationScoped
    public MainBrand createBrandLogo() {
        return new MainBrand() {
            @Override
            public Widget asWidget() {
                final Image logo = new Image( AppResource.INSTANCE.images().ufUserLogo() );
                logo.setSize("200px", "60px");
                return logo;
            }
        };
    }
}