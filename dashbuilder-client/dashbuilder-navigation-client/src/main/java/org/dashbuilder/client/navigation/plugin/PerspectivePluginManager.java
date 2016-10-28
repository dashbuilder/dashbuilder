/*
 * Copyright 2016 JBoss, by Red Hat, Inc
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
package org.dashbuilder.client.navigation.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.event.PerspectivePluginsChangedEvent;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.workbench.NavWorkbenchCtx;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.uberfire.client.workbench.type.ClientResourceType;
import org.uberfire.client.workbench.type.ClientTypeRegistry;
import org.uberfire.ext.layout.editor.api.LayoutServices;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;
import org.uberfire.ext.layout.editor.client.generator.LayoutGenerator;
import org.uberfire.ext.plugin.client.type.PerspectiveLayoutPluginResourceType;
import org.uberfire.ext.plugin.event.PluginAdded;
import org.uberfire.ext.plugin.event.PluginDeleted;
import org.uberfire.ext.plugin.event.PluginRenamed;
import org.uberfire.ext.plugin.event.PluginSaved;
import org.uberfire.ext.plugin.model.LayoutEditorModel;
import org.uberfire.ext.plugin.model.Plugin;
import org.uberfire.ext.plugin.model.PluginType;
import org.uberfire.ext.plugin.service.PluginServices;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.workbench.model.ActivityResourceType;

@EntryPoint
@ApplicationScoped
public class PerspectivePluginManager {

    private ClientTypeRegistry clientTypeRegistry;
    private LayoutGenerator layoutGenerator;
    private NavigationManager navigationManager;
    private Caller<PluginServices> pluginServices;
    private Caller<LayoutServices> layoutServices;
    private Event<PerspectivePluginsChangedEvent> perspectivesChangedEvent;
    private Map<String, Plugin> pluginMap = new HashMap<>();
    private Set<String> perspectiveNames = new HashSet<>();

    @Inject
    public PerspectivePluginManager(ClientTypeRegistry clientTypeRegistry,
                                    LayoutGenerator layoutGenerator,
                                    NavigationManager navigationManager,
                                    Caller<PluginServices> pluginServices,
                                    Caller<LayoutServices> layoutServices,
                                    Event<PerspectivePluginsChangedEvent> perspectivesChangedEvent) {
        this.clientTypeRegistry = clientTypeRegistry;
        this.layoutGenerator = layoutGenerator;
        this.navigationManager = navigationManager;
        this.pluginServices = pluginServices;
        this.layoutServices = layoutServices;
        this.perspectivesChangedEvent = perspectivesChangedEvent;
    }

    @AfterInitialization
    private void init() {
        pluginServices.call(((Collection<Plugin> plugins) -> {
            plugins.stream().filter(this::isRuntimePerspective).forEach(p -> {
                pluginMap.put(p.getName(), p);
            });
        })).listPlugins();
    }

    public List<Plugin> getPerspectivePlugins() {
        return new ArrayList<>(pluginMap.values());
    }

    public boolean isRuntimePerspective(Plugin plugin) {
        ClientResourceType type = clientTypeRegistry.resolve(plugin.getPath());
        return type != null && type instanceof PerspectiveLayoutPluginResourceType;
    }

    public boolean isRuntimePerspective(NavItem navItem) {
        return getRuntimePerspectiveId(navItem) != null;
    }

    public boolean isRuntimePerspective(String perspectiveId) {
        return pluginMap.containsKey(perspectiveId);
    }

    public String getRuntimePerspectiveId(NavItem navItem) {
        NavWorkbenchCtx navCtx = NavWorkbenchCtx.get(navItem);
        String resourceId = navCtx.getResourceId();
        ActivityResourceType resourceType = navCtx.getResourceType();
        boolean isRuntimePerspective = resourceId != null && ActivityResourceType.PERSPECTIVE.equals(resourceType) && isRuntimePerspective(resourceId);
        return isRuntimePerspective ? resourceId : null;
    }

    public void buildPerspectiveWidget(String perspectiveName, ParameterizedCommand<IsWidget> afterBuild, Command onRecursivity) {
        Plugin plugin = pluginMap.get(perspectiveName);
        if (plugin != null) {
            buildPerspectiveWidget(plugin, afterBuild, onRecursivity);
        }
    }

    public void buildPerspectiveWidget(Plugin perspectivePlugin, ParameterizedCommand<IsWidget> afterBuild, Command onRecursivity) {
        final String perspectiveName = perspectivePlugin.getName();

        if (perspectiveNames.contains(perspectiveName) && onRecursivity != null) {
            onRecursivity.execute();
            //GWT.log("RECURSIVE " + perspectiveName);
        } else {
            //GWT.log("START" + perspectiveName);
            perspectiveNames.add(perspectiveName);
            pluginServices.call((LayoutEditorModel layoutEditorModel) -> {
                layoutServices.call((LayoutTemplate layoutTemplate) -> {

                    if (layoutTemplate != null) {
                        IsWidget result = layoutGenerator.build(layoutTemplate);
                        afterBuild.execute(result);
                        perspectiveNames.remove(perspectiveName);
                        //GWT.log("END" + perspectiveName);
                    }
                }).convertLayoutFromString(layoutEditorModel.getLayoutEditorModel());
            }).getLayoutEditor(perspectivePlugin.getPath(), PluginType.PERSPECTIVE_LAYOUT);
        }
    }

    // Sync up both the internals plugin & widget registry

    public void onPlugInAdded(@Observes final PluginAdded event) {
        Plugin plugin = event.getPlugin();
        if (isRuntimePerspective(plugin)) {
            pluginMap.put(plugin.getName(), plugin);
            perspectivesChangedEvent.fire(new PerspectivePluginsChangedEvent());
        }
    }

    public void onPlugInSaved(@Observes final PluginSaved event) {
        Plugin plugin = event.getPlugin();
        if (isRuntimePerspective(plugin)) {
            pluginMap.put(plugin.getName(), plugin);
        }
    }

    public void onPlugInRenamed(@Observes final PluginRenamed event) {
        Plugin plugin = event.getPlugin();
        if (isRuntimePerspective(plugin)) {
            pluginMap.remove(event.getOldPluginName());
            pluginMap.put(plugin.getName(), plugin);

            NavWorkbenchCtx ctx = NavWorkbenchCtx.perspective(event.getOldPluginName());
            NavWorkbenchCtx newCtx = NavWorkbenchCtx.perspective(event.getPlugin().getName());
            List<NavItem> itemsToRename = navigationManager.getNavTree().searchItems(ctx);
            for (NavItem navItem : itemsToRename) {
                navItem.setContext(newCtx.toString());
            }
            if (!itemsToRename.isEmpty()) {
                navigationManager.saveNavTree(navigationManager.getNavTree(), () -> {});
            }
            perspectivesChangedEvent.fire(new PerspectivePluginsChangedEvent());
        }
    }


    public void onPlugInDeleted(@Observes final PluginDeleted event) {
        String pluginName = event.getPluginName();
        pluginMap.remove(pluginName);

        NavWorkbenchCtx ctx = NavWorkbenchCtx.perspective(pluginName);
        NavTree navTree = navigationManager.getNavTree();
        List<NavItem> itemsToDelete = navTree.searchItems(ctx);
        for (NavItem item : itemsToDelete) {
            navTree.deleteItem(item.getId());
        }
        if (!itemsToDelete.isEmpty()) {
            navigationManager.saveNavTree(navTree, null);
        }
        perspectivesChangedEvent.fire(new PerspectivePluginsChangedEvent());
    }
}