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
import java.util.List;

import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;

public class NavPointDescriptor {

    private NavDragComponent source = null;
    private List<NavDragComponent> subscribers = new ArrayList<>();

    public void setSource(NavDragComponent source) {
        subscribers.stream()
                .filter(s -> s == source)
                .findAny()
                .ifPresent(s -> {
                    throw new RuntimeException("The source is registered as a subscriber.");
                });

        this.source = source;
        if (source.getNavWidget() != null) {
            source.getNavWidget().setOnItemSelectedCommand(this::onSourceItemSelected);
        }
    }

    public NavDragComponent getSource() {
        return source;
    }

    public List<NavDragComponent> getSubscribers() {
        return subscribers;
    }

    public void addSubscriber(NavDragComponent subscriber) {
        if (source == subscriber) {
            throw new RuntimeException("The source and the subscriber are the same.");
        }
        NavItem navItem = source.getNavWidget().getItemSelected();
        if (navItem != null) {
            notifySubscriber(subscriber, navItem);
        }
        this.subscribers.add(subscriber);

    }

    private void onSourceItemSelected() {
        NavItem navItem = this.source.getNavWidget().getItemSelected();
        subscribers.forEach(subscriber -> notifySubscriber(subscriber, navItem));
    }

    public void notifySubscriber(NavDragComponent subscriber, NavItem navItem) {
        if (navItem instanceof NavGroup) {
            subscriber.getNavWidget().show((NavGroup) navItem);
        } else {
            subscriber.getNavWidget().hide();
        }
    }
}
