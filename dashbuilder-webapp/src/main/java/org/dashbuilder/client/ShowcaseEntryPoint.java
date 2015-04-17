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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.RootPanel;
import org.dashbuilder.client.resources.i18n.AppConstants;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ApplicationReadyEvent;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBar;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;

import static org.uberfire.workbench.model.menu.MenuFactory.*;

/**
 * Entry-point for the Dashbuilder showcase
 */
@EntryPoint
public class ShowcaseEntryPoint {

    @Inject
    private WorkbenchMenuBar menubar;

    @Inject
    private PlaceManager placeManager;

    @PostConstruct
    public void startApp() {
        hideLoadingPopup();
    }

    private void setupMenu( @Observes final ApplicationReadyEvent event ) {

        menubar.addMenus(
                newTopLevelMenu(AppConstants.INSTANCE.menu_home()).respondsWith(new Command() {
                    public void execute() {
                        placeManager.goTo("HomePerspective");
                    }
                }).endMenu().
                newTopLevelMenu(AppConstants.INSTANCE.menu_gallery()).respondsWith(new Command() {
                    public void execute() {
                        placeManager.goTo("DisplayerGalleryPerspective");
                    }
                }).endMenu().
                newTopLevelMenu(AppConstants.INSTANCE.menu_authoring())
                .withItems(getAuthoringMenuItems())
                .endMenu().
                newTopLevelMenu(AppConstants.INSTANCE.menu_dashboards())
                .withItems(getDashboardMenuItems())
                .endMenu().
                newTopLevelMenu(AppConstants.INSTANCE.menu_extensions())
                .withItems(getExtensionsMenuItems())
                .endMenu().
                build()
        );
    }

    private List<? extends MenuItem> getAuthoringMenuItems() {
        final List<MenuItem> result = new ArrayList<MenuItem>(2);

        result.add(MenuFactory.newSimpleItem("Data Set Authoring").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("DataSetAuthoringPerspective");
            }
        }).endMenu().build().getItems().get(0));

        return result;
    }
    
    private List<? extends MenuItem> getDashboardMenuItems() {
        final List<MenuItem> result = new ArrayList<MenuItem>(2);

        result.add(MenuFactory.newSimpleItem(AppConstants.INSTANCE.menu_dashboards_salesdb()).respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("SalesDashboardPerspective");
            }
        }).endMenu().build().getItems().get(0));

        result.add(MenuFactory.newSimpleItem(AppConstants.INSTANCE.menu_dashboards_salesreports()).respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("SalesReportsPerspective");
            }
        }).endMenu().build().getItems().get(0));

        result.add(MenuFactory.newSimpleItem(AppConstants.INSTANCE.menu_dashboards_new()).respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("DashboardDesignerPerspective");
            }
        }).endMenu().build().getItems().get(0));

        return result;
    }

    private List<? extends MenuItem> getExtensionsMenuItems() {
        final List<MenuItem> result = new ArrayList<MenuItem>(2);

        result.add(MenuFactory.newSimpleItem(AppConstants.INSTANCE.menu_extensions_plugins()).respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("PlugInAuthoringPerspective");
            }
        }).endMenu().build().getItems().get(0));

        result.add(MenuFactory.newSimpleItem(AppConstants.INSTANCE.menu_extensions_apps()).respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("AppsPerspective");
            }
        }).endMenu().build().getItems().get(0));

        return result;
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
}