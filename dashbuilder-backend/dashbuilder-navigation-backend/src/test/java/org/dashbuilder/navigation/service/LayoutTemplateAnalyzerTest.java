/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.util.HashSet;

import org.dashbuilder.navigation.impl.NavTreeBuilder;
import org.dashbuilder.navigation.workbench.NavWorkbenchCtx;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.layout.editor.api.editor.LayoutColumn;
import org.uberfire.ext.layout.editor.api.editor.LayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutRow;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LayoutTemplateAnalyzerTest {

    public static final String GROUP_ID = "navGroupId";
    public static final String PERSPECTIVE_ID = "perspectiveId";

    @Mock
    PerspectivePluginServicesImpl pluginServices;

    @Mock
    NavigationServicesImpl navigationServices;

    @InjectMocks
    LayoutTemplateAnalyzer layoutTemplateAnalyzer;

    LayoutTemplate perspectiveA = new LayoutTemplate();
    LayoutTemplate perspectiveB = new LayoutTemplate();
    LayoutTemplate perspectiveC = new LayoutTemplate();
    LayoutColumn layoutColumnA = new LayoutColumn("12");
    LayoutColumn layoutColumnB = new LayoutColumn("12");


    @Before
    public void setUp() throws Exception {
        LayoutRow layoutRowA = new LayoutRow();
        LayoutComponent layoutComponentA = new LayoutComponent();
        layoutComponentA.addProperty(GROUP_ID, "groupA");
        layoutRowA.add(layoutColumnA);
        layoutColumnA.add(layoutComponentA);
        perspectiveA.addRow(layoutRowA);

        LayoutRow layoutRowB = new LayoutRow();
        LayoutComponent layoutComponentB = new LayoutComponent();
        layoutComponentB.addProperty(GROUP_ID, "groupB");
        layoutRowB.add(layoutColumnB);
        layoutColumnB.add(layoutComponentB);
        perspectiveB.addRow(layoutRowB);

        LayoutRow layoutRowC = new LayoutRow();
        LayoutColumn layoutColumnC1 = new LayoutColumn("6");
        LayoutColumn layoutColumnC2 = new LayoutColumn("6");
        LayoutComponent layoutComponentC1 = new LayoutComponent();
        LayoutComponent layoutComponentC2 = new LayoutComponent();
        layoutComponentC1.addProperty(GROUP_ID, "groupC");
        layoutComponentC2.addProperty(GROUP_ID, "groupC");
        layoutRowC.add(layoutColumnC1);
        layoutRowC.add(layoutColumnC2);
        layoutColumnC1.add(layoutComponentC1);
        layoutColumnC2.add(layoutComponentC2);
        perspectiveC.addRow(layoutRowC);

        when(pluginServices.getLayoutTemplate("A")).thenReturn(perspectiveA);
        when(pluginServices.getLayoutTemplate("B")).thenReturn(perspectiveB);
        when(pluginServices.getLayoutTemplate("C")).thenReturn(perspectiveC);
    }

    @Test
    public void testOneLevelNoDeadlock() throws Exception {
        when(navigationServices.loadNavTree()).thenReturn(new NavTreeBuilder()
                .group("groupA", "", "", true)
                    .item("layout", "", "", true, NavWorkbenchCtx.perspective("B"))
                    .endGroup()
                .build());

        boolean isRecursive = layoutTemplateAnalyzer.hasDeadlock(perspectiveA, new HashSet<>());
        assertFalse(isRecursive);
    }

    @Test
    public void testPerspectiveReuseNoDeadlock() throws Exception {
        when(navigationServices.loadNavTree()).thenReturn(new NavTreeBuilder()
                .group("groupC", "", "", true)
                    .item("layout", "", "", true, NavWorkbenchCtx.perspective("B"))
                    .endGroup()
                .build());

        boolean isRecursive = layoutTemplateAnalyzer.hasDeadlock(perspectiveC, new HashSet<>());
        assertFalse(isRecursive);
    }

    @Test
    public void testSimpleDeadlock() throws Exception {
        when(navigationServices.loadNavTree()).thenReturn(new NavTreeBuilder()
                .group("groupA", "", "", true)
                    .item("layout", "", "", true, NavWorkbenchCtx.perspective("A"))
                    .endGroup()
                .build());

        boolean isRecursive = layoutTemplateAnalyzer.hasDeadlock(perspectiveA, new HashSet<>());
        assertTrue(isRecursive);
    }

    @Test
    public void testPerspectiveComponent() throws Exception {
        LayoutComponent layoutComponentB = new LayoutComponent();
        layoutComponentB.addProperty(PERSPECTIVE_ID, "B");
        layoutColumnA.add(layoutComponentB);

        when(navigationServices.loadNavTree()).thenReturn(new NavTreeBuilder().build());
        boolean isRecursive = layoutTemplateAnalyzer.hasDeadlock(perspectiveA, new HashSet<>());
        assertFalse(isRecursive);
    }

    @Test
    public void testPerspectiveDeadlock() throws Exception {
        LayoutComponent layoutComponentB = new LayoutComponent();
        layoutComponentB.addProperty(PERSPECTIVE_ID, "B");
        layoutColumnA.add(layoutComponentB);
        LayoutComponent layoutComponentA = new LayoutComponent();
        layoutComponentB.addProperty(PERSPECTIVE_ID, "A");
        layoutColumnB.add(layoutComponentA);

        when(navigationServices.loadNavTree()).thenReturn(new NavTreeBuilder().build());
        boolean isRecursive = layoutTemplateAnalyzer.hasDeadlock(perspectiveA, new HashSet<>());
        assertTrue(isRecursive);
    }

    @Test
    public void testIndirectDeadlock() throws Exception {
        when(navigationServices.loadNavTree()).thenReturn(new NavTreeBuilder()
                .group("groupA", "", "", true)
                    .item("layout", "", "", true, NavWorkbenchCtx.perspective("B"))
                    .endGroup()
                .group("groupB", "", "", true)
                    .item("layout", "", "", true, NavWorkbenchCtx.perspective("A"))
                    .endGroup()
                .build());

        boolean isRecursive = layoutTemplateAnalyzer.hasDeadlock(perspectiveA, new HashSet<>());
        assertTrue(isRecursive);
    }
}
