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

import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.event.PerspectivePluginsChangedEvent;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.impl.NavTreeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.plugin.event.PluginDeleted;

import javax.enterprise.event.Event;

import static org.dashbuilder.navigation.workbench.NavWorkbenchCtx.perspective;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PerspectivePluginManagerTest {

    private static final String ID_TO_DELETE = "Persp1";
    public static final NavTree TEST_NAV_TREE = new NavTreeBuilder()
            .item(ID_TO_DELETE, "name1", "desciption1", true, perspective(ID_TO_DELETE))
            .build();
    @Mock
    private NavigationManager navigationManagerMock;

    @Mock
    private PluginDeleted pluginDeletedEventMock;

    @Mock
    private Event<PerspectivePluginsChangedEvent> perspectiveChangedEventMock;

    @Test
    public void whenPluginDeleted_itIsRemovedFromNavTree() {
        NavTree testTree = TEST_NAV_TREE.cloneTree();

        assertNotNull(testTree.getItemById(ID_TO_DELETE));

        when(navigationManagerMock.getNavTree()).thenReturn(testTree);
        when(pluginDeletedEventMock.getPluginName()).thenReturn(ID_TO_DELETE);

        PerspectivePluginManager testedPluginManager = new PerspectivePluginManager(null, null, navigationManagerMock, null, perspectiveChangedEventMock);
        testedPluginManager.onPlugInDeleted(pluginDeletedEventMock);

        assertNull("Plugin should be removed from the tree when PluginDeleted event occurs", testTree.getItemById(ID_TO_DELETE));
        verify(navigationManagerMock).saveNavTree(anyObject(), eq(null));
        verify(perspectiveChangedEventMock).fire(anyObject());
    }
}
