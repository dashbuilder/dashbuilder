/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import java.util.Arrays;

import org.dashbuilder.client.navigation.layout.editor.NavPointList;
import org.dashbuilder.client.navigation.widget.NavComponentConfigModal;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.impl.NavTreeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.mvp.Command;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class NavComponentConfigModalTest {

    @Mock
    NavComponentConfigModal.View view;

    @Mock
    NavPointList navPointList;

    @Mock
    Command onOk;

    NavTree tree;
    NavComponentConfigModal presenter;

    @Before
    public void setUp() throws Exception {
        tree = new NavTreeBuilder()
                .item("H1", "H1", null, false)
                .divider()
                .group("A", "A", null, false)
                .item("A1", "A1", null, false)
                .item("A2", "A2", null, false)
                .group("B", "B", null, false)
                .group("C", "C", null, false)
                .build();

        when(navPointList.getNavPoints()).thenReturn(Arrays.asList("a", "b"));
        presenter = new NavComponentConfigModal(view, navPointList);
        presenter.setOnOk(onOk);
    }

    @Test
    public void testInitDefault() {
        presenter.setNavGroup(tree.getRootItems(), null);
        presenter.show();

        verify(view).init(presenter);
        verify(view).clearNavGroupItems();
        verify(view).setNavGroupSelection(eq("A"), any());
        verify(view).addNavGroupItem(eq("A>B"), any());
        verify(view).addNavGroupItem(eq("A>B>C"), any());
        verify(view).show();
    }

    @Test
    public void testInitSelected() {
        presenter.setNavGroup(tree.getRootItems(), "B");
        presenter.show();

        verify(view).init(presenter);
        verify(view).clearNavGroupItems();
        verify(view).setNavGroupSelection(eq("A>B"), any());
        verify(view).addNavGroupItem(eq("A"), any());
        verify(view).addNavGroupItem(eq("A>B>C"), any());
        verify(view).show();
    }

    @Test
    public void testIdAlreadyExists() {
        presenter.show();
        presenter.onIdChanged("a");
        verify(view).errorIdentifierAlreadyExists();

        reset(view);
        presenter.onIdChanged("c");
        verify(view).clearIdentifierErrors();
        verify(view, never()).errorIdentifierAlreadyExists();
    }

    @Test
    public void testMaxLevels() {
        presenter.show();
        presenter.onMaxLevelsChanged("a");
        verify(view).errorMaxLevelsNotNumeric();

        reset(view);
        presenter.onMaxLevelsChanged("1");
        verify(view).clearMaxLevelsErrors();
        verify(view, never()).errorMaxLevelsNotNumeric();
    }

    @Test
    public void testMaxDefaultItems() {
        presenter.setNavGroup(tree.getRootItems(), "A");
        presenter.setMaxLevels(-1);
        presenter.show();
        verify(view).clearDefaultItems();
        verify(view).addDefaultItem(eq("A>A1"), any());
        verify(view).addDefaultItem(eq("A>A2"), any());
        verify(view).addDefaultItem(eq("A>B"), any());
        verify(view).addDefaultItem(eq("A>B>C"), any());

        reset(view);
        presenter.onMaxLevelsChanged("1");
        verify(view).clearDefaultItems();
        verify(view).addDefaultItem(eq("A>A1"), any());
        verify(view).addDefaultItem(eq("A>A2"), any());
        verify(view).addDefaultItem(eq("A>B"), any());

        reset(view);
        presenter.onMaxLevelsChanged("2");
        verify(view).clearDefaultItems();
        verify(view).addDefaultItem(eq("A>A1"), any());
        verify(view).addDefaultItem(eq("A>A2"), any());
        verify(view).addDefaultItem(eq("A>B"), any());
        verify(view).addDefaultItem(eq("A>B>C"), any());
    }

    @Test
    public void testSelectItem() {
        presenter.setNavGroup(tree.getRootItems(), "A");
        presenter.show();

        reset(view);
        presenter.onGroupSelected("B");

        assertEquals(presenter.getGroupId(), "B");
        verify(view).clearNavGroupItems();
        verify(view).setNavGroupSelection(eq("A>B"), any());
        verify(view).addNavGroupItem(eq("A"), any());
        verify(view).addNavGroupItem(eq("A>B>C"), any());
    }

    @Test
    public void testNavPointsInit() {
        presenter.show();
        verify(view).clearNavPointItems();
        verify(view).addNavPoint(eq("a"), any());
        verify(view).addNavPoint(eq("b"), any());
        verify(view).setNavGroupEnabled(true);
        verify(view).setDefaultNavItemEnabled(false);
    }

    @Test
    public void testNavPointsExludeId() {
        presenter.setNavId("a");
        presenter.show();

        verify(view).clearNavPointItems();
        verify(view, never()).addNavPoint(eq("a"), any());
        verify(view).addNavPoint(eq("b"), any());
    }

    @Test
    public void testNavPointSelected() {
        presenter.setNavPoint("a");
        presenter.show();
        verify(view).clearNavPointItems();
        verify(view).addNavPoint(eq("b"), any());
        verify(view).setNavPointSelection(eq("a"), any());
        verify(view).setNavGroupEnabled(false);
        verify(view).setDefaultNavItemEnabled(false);
    }

    @Test
    public void testDefaultItemsPerGroup() {
        presenter.setNavGroup(tree.getRootItems(), "A");
        presenter.show();

        verify(view).setDefaultNavItemEnabled(true);
        verify(view).clearDefaultItems();
        verify(view).addDefaultItem(eq("A>A1"), any());
        verify(view).addDefaultItem(eq("A>A2"), any());
        verify(view).addDefaultItem(eq("A>B"), any());
        verify(view, never()).addDefaultItem(eq("C"), any());

        reset(view);
        presenter.onGroupSelected("C");
        verify(view).setDefaultNavItemEnabled(true);
        verify(view).clearDefaultItems();
        verify(view, never()).addDefaultItem(anyString(), any());
    }
}