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
package org.dashbuilder.client.dashboard;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.dashbuilder.shared.dashboard.events.DashboardCreatedEvent;
import org.dashbuilder.shared.dashboard.events.DashboardDeletedEvent;
import org.dashbuilder.displayer.client.PerspectiveCoordinator;
import org.dashbuilder.displayer.json.DisplayerSettingsJSONMarshaller;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ioc.client.container.SyncBeanManagerImpl;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.workbench.model.PerspectiveDefinition;

import static org.jboss.errai.ioc.client.QualifierUtil.*;

/**
 * Dashboard manager
 */
@ApplicationScoped
public class DashboardManager {

    private SyncBeanManager beanManager;
    private PlaceManager placeManager;
    private PerspectiveManager perspectiveManager;
    private PerspectiveCoordinator perspectiveCoordinator;
    private DisplayerSettingsJSONMarshaller jsonMarshaller;
    private ActivityBeansCache activityBeansCache;
    private Event<DashboardCreatedEvent> dashboardCreatedEvent;
    private Event<DashboardDeletedEvent> dashboardDeletedEvent;

    @Inject
    public DashboardManager(SyncBeanManager beanManager,
                            PlaceManager placeManager,
                            PerspectiveManager perspectiveManager,
                            PerspectiveCoordinator perspectiveCoordinator,
                            DisplayerSettingsJSONMarshaller jsonMarshaller,
                            ActivityBeansCache activityBeansCache,
                            Event<DashboardCreatedEvent> dashboardCreatedEvent,
                            Event<DashboardDeletedEvent> dashboardDeletedEvent) {

        this.beanManager = beanManager;
        this.placeManager = placeManager;
        this.perspectiveManager = perspectiveManager;
        this.perspectiveCoordinator = perspectiveCoordinator;
        this.jsonMarshaller = jsonMarshaller;
        this.activityBeansCache = activityBeansCache;
        this.dashboardCreatedEvent = dashboardCreatedEvent;
        this.dashboardDeletedEvent = dashboardDeletedEvent;
    }

    @AfterInitialization
    protected void init() {
        perspectiveManager.loadPerspectiveStates(new ParameterizedCommand<Set<PerspectiveDefinition>>() {
            public void execute(Set<PerspectiveDefinition> list) {
                for (PerspectiveDefinition p : list) {
                    String id = p.getName();
                    if (id.startsWith("dashboard-")) {
                        registerPerspective(id);
                    }
                }
            }
        });
    }

    protected DashboardPerspectiveActivity registerPerspective(String id) {
        DashboardPerspectiveActivity activity = new DashboardPerspectiveActivity(id, this,
                beanManager,
                perspectiveManager,
                placeManager,
                perspectiveCoordinator,
                jsonMarshaller);

        SyncBeanManagerImpl beanManager = (SyncBeanManagerImpl) IOC.getBeanManager();
        beanManager.addBean((Class) PerspectiveActivity.class, DashboardPerspectiveActivity.class, null, activity, DEFAULT_QUALIFIERS, id, true, null);
        activityBeansCache.addNewPerspectiveActivity(beanManager.lookupBeans(id).iterator().next());
        return activity;
    }

    public DashboardPerspectiveActivity newDashboard(final String name) {
        final DashboardPerspectiveActivity activity = registerPerspective("dashboard-" + name);
        perspectiveManager.savePerspectiveState(new Command() {
            public void execute() {
                dashboardCreatedEvent.fire(new DashboardCreatedEvent(activity.getIdentifier(), activity.getDisplayName()));
            }
        });
        return activity;
    }

    public DashboardPerspectiveActivity getDashboardById(String id) {
        for (DashboardPerspectiveActivity d : getDashboards()) {
            if (d.getIdentifier().equals(id)) return d;
        }
        return null;
    }

    public DashboardPerspectiveActivity getDashboardByName(String name) {
        for (DashboardPerspectiveActivity d : getDashboards()) {
            if (d.getDisplayName().equals(name)) return d;
        }
        return null;
    }

    public void removeDashboard(String id) {
        DashboardPerspectiveActivity activity = getDashboardById(id);
        if (activity != null) {
            activity.setPersistent(false);
            activityBeansCache.removeActivity(id);
            dashboardDeletedEvent.fire(new DashboardDeletedEvent(activity.getIdentifier(), activity.getDisplayName()));
        }
    }

    public Set<DashboardPerspectiveActivity> getDashboards() {
        Set<DashboardPerspectiveActivity> activities = new HashSet<DashboardPerspectiveActivity>();
        for (String activityId : activityBeansCache.getActivitiesById()) {

            IOCBeanDef<Activity> activityDef = activityBeansCache.getActivity(activityId);
            if (activityDef.getBeanClass().equals(DashboardPerspectiveActivity.class)) {
                activities.add((DashboardPerspectiveActivity) activityDef.getInstance());
            }
        }
        return activities;
    }
}
