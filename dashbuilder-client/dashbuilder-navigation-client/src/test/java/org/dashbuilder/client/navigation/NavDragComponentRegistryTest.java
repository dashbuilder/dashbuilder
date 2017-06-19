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

import org.dashbuilder.client.navigation.layout.editor.NavDragComponent;
import org.dashbuilder.client.navigation.layout.editor.NavDragComponentRegistry;
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

    NavDragComponentRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new NavDragComponentRegistry(beanManager);
    }

    @Test
    public void testClearAll() {
        NavDragComponent mock1 = mock(NavDragComponent.class);
        NavDragComponent mock2 = mock(NavDragComponent.class);
        registry.checkIn(mock1);
        registry.checkIn(mock2);
        registry.clearAll();

        verify(beanManager).destroyBean(mock1);
        verify(beanManager).destroyBean(mock2);
        verify(mock1).dispose();
        verify(mock2).dispose();

        reset(beanManager);
        registry.clearAll();
        verify(beanManager, never()).destroyBean(any());
    }
}