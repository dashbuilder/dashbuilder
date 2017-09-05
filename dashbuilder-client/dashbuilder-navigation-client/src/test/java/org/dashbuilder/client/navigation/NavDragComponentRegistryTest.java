/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.navigation;

import java.util.Collection;

import org.dashbuilder.client.navigation.layout.editor.NavDragComponent;
import org.dashbuilder.client.navigation.layout.editor.NavDragComponentRegistry;
import org.dashbuilder.client.navigation.layout.editor.NavPointDescriptor;
import org.dashbuilder.client.navigation.widget.NavWidget;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NavDragComponentRegistryTest {

    @Mock
    SyncBeanManager beanManager;

    @Mock
    NavDragComponent mock1;

    @Mock
    NavDragComponent mock2;

    @Mock
    NavWidget widget1;

    @Mock
    NavWidget widget2;

    @Mock
    NavGroup navGroup;

    @Mock
    NavItem navItem;

    NavDragComponentRegistry registry;

    @Before
    public void setUp() throws Exception {
        when(mock1.getNavId()).thenReturn("mock1");
        when(mock2.getNavId()).thenReturn("mock2");
        when(mock2.getNavPoint()).thenReturn("mock1");
        when(mock1.getNavWidget()).thenReturn(widget1);
        when(mock2.getNavWidget()).thenReturn(widget2);
        registry = new NavDragComponentRegistry(beanManager);

        registry.checkIn(mock1);
        registry.checkIn(mock2);
    }

    @Test
    public void testNavPoints() {
        Collection<NavPointDescriptor> navPoints = registry.getAll();
        assertEquals(navPoints.size(), 2);
        assertNotNull(registry.lookupNavPoint("mock1"));
        assertNotNull(registry.lookupNavPoint("mock2"));

        NavPointDescriptor np = registry.lookupNavPoint("mock1");
        assertEquals(np.getSource(), mock1);
        assertEquals(np.getSubscribers().size(), 1);
        assertEquals(np.getSubscribers().get(0), mock2);
    }

    @Test
    public void testAvoidDuplicates() {
        registry.checkIn(mock1);
        registry.checkIn(mock2);
        assertEquals(registry.getAll().size(), 2);
    }

    @Test
    public void testClearAll() {
        registry.clearAll();
        assertEquals(registry.getAll().size(), 0);

        verify(beanManager).destroyBean(mock1);
        verify(beanManager).destroyBean(mock2);
        verify(mock1).dispose();
        verify(mock2).dispose();

        reset(beanManager);
        registry.clearAll();
        verify(beanManager, never()).destroyBean(any());
    }

    @Test
    public void testShowOnCheckIn() {
        registry.clearAll();
        reset(widget2);

        when(widget1.getItemSelected()).thenReturn(navGroup);
        registry.checkIn(mock1);
        registry.checkIn(mock2);
        verify(widget2).show(navGroup);
    }

    @Test
    public void testHideOnCheckIn() {
        registry.clearAll();
        reset(widget2);

        when(widget1.getItemSelected()).thenReturn(navItem);
        registry.checkIn(mock1);
        registry.checkIn(mock2);
        verify(widget2).hide();
    }
}