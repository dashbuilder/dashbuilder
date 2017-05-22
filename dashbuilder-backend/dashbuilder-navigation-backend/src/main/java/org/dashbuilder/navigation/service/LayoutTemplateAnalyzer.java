/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.navigation.service;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.workbench.NavWorkbenchCtx;
import org.uberfire.ext.layout.editor.api.editor.LayoutColumn;
import org.uberfire.ext.layout.editor.api.editor.LayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutRow;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;

@ApplicationScoped
public class LayoutTemplateAnalyzer {

    private PerspectivePluginServices pluginServices;
    private NavigationServices navigationServices;

    public LayoutTemplateAnalyzer() {
    }

    @Inject
    public LayoutTemplateAnalyzer(PerspectivePluginServices pluginServices, NavigationServices navigationServices) {
        this.pluginServices = pluginServices;
        this.navigationServices = navigationServices;
    }

    public boolean hasDeadlock(LayoutTemplate layoutTemplate, Set<String> ancestorSet) {
        NavTree navTree = navigationServices.loadNavTree();

        for (LayoutRow row : layoutTemplate.getRows()) {
            for (LayoutColumn column : row.getLayoutColumns()) {
                for (LayoutComponent component : column.getLayoutComponents()) {

                    // Any layout component linked to a nav group can potentially lead to a deadlock scenario.
                    String navGroupId = component.getProperties().get("navGroupId");
                    if (navGroupId != null) {
                        NavGroup navGroup = (NavGroup) navTree.getItemById(navGroupId);
                        boolean deadlock = hasDeadlock(navGroup, ancestorSet);
                        if (deadlock) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasDeadlock(NavGroup navGroup, Set<String> ancestorSet) {
        if (navGroup == null) {
            return false;
        }
        for (NavItem navItem : navGroup.getChildren()) {
            NavWorkbenchCtx navCtx = NavWorkbenchCtx.get(navItem);
            String perspectiveName = navCtx.getResourceId();
            LayoutTemplate layoutTemplate = pluginServices.getLayoutTemplate(perspectiveName);
            if (layoutTemplate != null) {

                // A deadlock occurs either when the nav item is linked to an already
                // traversed perspective or when the layout itself is causing a deadlock.

                if (ancestorSet.contains(perspectiveName)) {
                    return true;
                }
                else {
                    try {
                        ancestorSet.add(perspectiveName);
                        if (hasDeadlock(layoutTemplate, ancestorSet)) {
                            return true;
                        }
                    } finally {
                        ancestorSet.remove(perspectiveName);
                    }
                }
            }
        }
        return false;
    }
}
