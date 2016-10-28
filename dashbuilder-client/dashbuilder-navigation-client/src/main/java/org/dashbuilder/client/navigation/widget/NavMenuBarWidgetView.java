/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.client.navigation.widget;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.UnorderedList;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class NavMenuBarWidgetView extends BaseNavWidgetView<NavMenuBarWidget>
        implements NavMenuBarWidget.View {

    @Inject
    @DataField
    UnorderedList navBar;

    NavMenuBarWidget presenter;

    @Override
    public void init(NavMenuBarWidget presenter) {
        this.presenter = presenter;
        super.navWidget = navBar;
    }

    @Override
    public void addDivider() {
        // Useless in a menu bar
    }

    @Override
    public void setActive(boolean active) {
        // Useless in a menu bar
    }
}
