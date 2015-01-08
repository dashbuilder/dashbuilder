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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.dashbuilder.displayer.client.ClientSettings;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ApplicationReadyEvent;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBar;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

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
                newTopLevelMenu("Gallery").respondsWith(new Command() {
                    public void execute() {
                        placeManager.goTo("DisplayerGalleryPerspective");
                    }
                }).endMenu().
                newTopLevelMenu("Plugins").respondsWith(new Command() {
                    public void execute() {
                        placeManager.goTo("PlugInAuthoringPerspective");
                    }
                }).endMenu().
                newTopLevelMenu("Apps").respondsWith(new Command() {
                    public void execute() {
                        placeManager.goTo("AppsPerspective");
                    }
                }).endMenu().
                newTopLevelMenu("Examples")
                        .withItems(getSampleDashboardMenuItems())
                        .endMenu().
                build()
        );
    }

    private List<? extends MenuItem> getSampleDashboardMenuItems() {
        final List<MenuItem> result = new ArrayList<MenuItem>(2);

        result.add(MenuFactory.newSimpleItem("Sales Dashboard").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("SalesDashboardPerspective");
            }
        }).endMenu().build().getItems().get(0));

        result.add(MenuFactory.newSimpleItem("Table reports").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("SalesReportsPerspective");
            }
        }).endMenu().build().getItems().get(0));

        result.add(MenuFactory.newSimpleItem("Ad-hoc").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo("DashboardDesignerPerspective");
            }
        }).endMenu().build().getItems().get(0));

        return result;
    }

    // Fade out the "Loading application" pop-up
    private void hideLoadingPopup() {
        final Element e = RootPanel.get( "loading" ).getElement();

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