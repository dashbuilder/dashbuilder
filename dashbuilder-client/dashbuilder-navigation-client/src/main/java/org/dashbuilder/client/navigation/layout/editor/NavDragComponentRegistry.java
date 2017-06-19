/*
 * Copyright 2017 JBoss, by Red Hat, Inc
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
package org.dashbuilder.client.navigation.layout.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.workbench.events.PerspectiveChange;

@ApplicationScoped
public class NavDragComponentRegistry {

    SyncBeanManager beanManager;
    List<NavDragComponent> dragComponentList = new ArrayList<>();

    @Inject
    public NavDragComponentRegistry(SyncBeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void checkIn(NavDragComponent dragComponent) {
        dragComponentList.add(dragComponent);
    }

    public void clearAll() {
        Iterator<NavDragComponent> it = dragComponentList.iterator();
        while (it.hasNext()) {
            NavDragComponent component = it.next();
            it.remove();
            component.dispose();
            beanManager.destroyBean(component);
        }
    }

    // Make sure the nav components are disposed when the current perspective changes

    public void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        clearAll();
    }
}
