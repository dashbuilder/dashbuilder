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
import org.dashbuilder.client.displayer.DataViewerLocator;
import org.dashbuilder.client.google.GoogleRenderer;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.UberFirePreferences;
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
 * GWT's Entry-point for the Dashbuilder showcase
 */
@EntryPoint
public class ShowcaseEntryPoint {

    @Inject
    private SyncBeanManager manager;

    @Inject
    private WorkbenchMenuBar menubar;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private ActivityManager activityManager;

    @Inject
    private ClientMessageBus bus;

    @Inject
    private DataViewerLocator dataViewerLocator;

    @PostConstruct
    public void startApp() {
        UberFirePreferences.setProperty( "org.uberfire.client.workbench.clone.ou.mandatory.disable", true );
        //todo {porcelli} context button navigator style is broken, disabling for now
        UberFirePreferences.setProperty( "org.uberfire.client.workbench.widgets.listbar.context.disable", true );

        setDefaultRendererLibs();

        hideLoadingPopup();
    }

    private void setDefaultRendererLibs() {
        dataViewerLocator.setDefaultRenderer(DataDisplayerType.BARCHART, GoogleRenderer.UUID);
        dataViewerLocator.setDefaultRenderer(DataDisplayerType.PIECHART, GoogleRenderer.UUID);
        dataViewerLocator.setDefaultRenderer(DataDisplayerType.AREACHART, GoogleRenderer.UUID);
        dataViewerLocator.setDefaultRenderer(DataDisplayerType.LINECHART, GoogleRenderer.UUID);
        dataViewerLocator.setDefaultRenderer(DataDisplayerType.METERCHART, GoogleRenderer.UUID);
        dataViewerLocator.setDefaultRenderer(DataDisplayerType.MAP, GoogleRenderer.UUID);
        dataViewerLocator.setDefaultRenderer(DataDisplayerType.TABLE, GoogleRenderer.UUID);
    }

    private void setupMenu( @Observes final ApplicationReadyEvent event ) {
        final PerspectiveActivity defaultPerspective = getDefaultPerspectiveActivity();

        final Menus menus =
                newTopLevelMenu("KPI Gallery").respondsWith(new Command() {
                    public void execute() {
                        if (defaultPerspective != null) {
                            placeManager.goTo(new DefaultPlaceRequest(defaultPerspective.getIdentifier()));
                        } else {
                            Window.alert("Default perspective not found.");
                        }
                    }
                }).endMenu().
                newTopLevelMenu("Dashboards").withItems(getSampleDashboardMenuItems()).endMenu().
                build();

        menubar.addMenus( menus );
    }

    private List<? extends MenuItem> getSampleDashboardMenuItems() {
        final List<MenuItem> result = new ArrayList<MenuItem>( 1 );

        result.add(MenuFactory.newSimpleItem("Sales Dashboard").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo( new DefaultPlaceRequest( "Sales Dashboard" ) );
            }
        }).endMenu().build().getItems().get(0));

        result.add(MenuFactory.newSimpleItem("Table reports").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo( new DefaultPlaceRequest( "Sales Reports" ) );
            }
        }).endMenu().build().getItems().get(0));

        return result;
    }

    private PerspectiveActivity getDefaultPerspectiveActivity() {
        PerspectiveActivity defaultPerspective = null;
        final Collection<IOCBeanDef<PerspectiveActivity>> perspectives = manager.lookupBeans( PerspectiveActivity.class );
        final Iterator<IOCBeanDef<PerspectiveActivity>> perspectivesIterator = perspectives.iterator();

        while ( perspectivesIterator.hasNext() ) {
            final IOCBeanDef<PerspectiveActivity> perspective = perspectivesIterator.next();
            final PerspectiveActivity instance = perspective.getInstance();
            if ( instance.isDefault() ) {
                defaultPerspective = instance;
                break;
            } else {
                manager.destroyBean( instance );
            }
        }
        return defaultPerspective;
    }

    //Fade out the "Loading application" pop-up
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

    public static native void redirect( String url )/*-{
        $wnd.location = url;
    }-*/;

}