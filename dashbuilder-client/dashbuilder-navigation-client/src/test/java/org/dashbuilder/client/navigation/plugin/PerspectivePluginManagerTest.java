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
package org.dashbuilder.client.navigation.plugin;

import java.util.Collections;

import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.event.PerspectivePluginsChangedEvent;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.impl.NavTreeBuilder;
import org.dashbuilder.navigation.service.PerspectivePluginServices;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.client.workbench.type.ClientTypeRegistry;
import org.uberfire.ext.plugin.client.type.PerspectiveLayoutPluginResourceType;
import org.uberfire.ext.plugin.event.PluginAdded;
import org.uberfire.ext.plugin.event.PluginDeleted;
import org.uberfire.ext.plugin.model.Plugin;
import org.uberfire.ext.plugin.model.PluginType;
import org.uberfire.mocks.CallerMock;

import javax.enterprise.event.Event;

import static org.dashbuilder.navigation.workbench.NavWorkbenchCtx.perspective;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PerspectivePluginManagerTest {

    private static final String PERSPECTIVE_ID = "Persp1";
    public static final NavTree TEST_NAV_TREE = new NavTreeBuilder()
            .item(PERSPECTIVE_ID, "name1", "desciption1", true, perspective(PERSPECTIVE_ID))
            .build();
    @Mock
    private NavigationManager navigationManager;

    @Mock
    private ClientTypeRegistry clientTypeRegistry;

    @Mock
    private PerspectivePluginServices pluginServices;

    @Mock
    private Event<PerspectivePluginsChangedEvent> perspectiveChangedEvent;

    private PluginAdded pluginAddedEvent;
    private PluginDeleted pluginDeletedEvent;
    private Plugin perspectivePlugin;
    private PerspectivePluginManager testedPluginManager;

    @Before
    public void setUp() {
        when(clientTypeRegistry.resolve(any())).thenReturn(new PerspectiveLayoutPluginResourceType());
        perspectivePlugin = new Plugin(PERSPECTIVE_ID, PluginType.PERSPECTIVE_LAYOUT, null);
        pluginDeletedEvent = new PluginDeleted(perspectivePlugin, null);
        pluginAddedEvent = new PluginAdded(perspectivePlugin, null);
        when(pluginServices.listPlugins()).thenReturn(Collections.emptyList());
        testedPluginManager = new PerspectivePluginManager(clientTypeRegistry, null, navigationManager, new CallerMock<>(pluginServices), perspectiveChangedEvent);
        testedPluginManager.getPerspectivePlugins(plugins -> {});
    }

    @Test
    public void testPluginAdded() {
        testedPluginManager.getPerspectivePlugins(plugins -> {
            assertEquals(plugins.size(), 0);
        });

        testedPluginManager.onPlugInAdded(pluginAddedEvent);
        verify(perspectiveChangedEvent).fire(anyObject());

        testedPluginManager.getPerspectivePlugins(plugins -> {
            assertEquals(plugins.size(), 1);
        });
    }

    @Test
    public void testPluginDeleted() {
        NavTree testTree = TEST_NAV_TREE.cloneTree();

        assertNotNull(testTree.getItemById(PERSPECTIVE_ID));

        when(navigationManager.getNavTree()).thenReturn(testTree);

        testedPluginManager.onPlugInDeleted(pluginDeletedEvent);

        assertNull("Plugin should be removed from the tree when PluginDeleted event occurs", testTree.getItemById(PERSPECTIVE_ID));
        verify(navigationManager).saveNavTree(anyObject(), eq(null));
        verify(perspectiveChangedEvent).fire(anyObject());
    }
}
