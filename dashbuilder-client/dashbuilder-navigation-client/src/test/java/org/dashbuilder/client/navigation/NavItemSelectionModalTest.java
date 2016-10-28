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

import org.dashbuilder.client.navigation.widget.NavItemSelectionModal;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.impl.NavTreeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NavItemSelectionModalTest {

    @Mock
    NavItemSelectionModal.View view;

    NavTree tree;
    NavItemSelectionModal presenter;

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

        presenter = new NavItemSelectionModal(view);
        presenter.setOnlyGroups(true);
        presenter.show(tree.getRootItems(), "A");
    }

    @Test
    public void testInit() {
        verify(view).init(presenter);
        verify(view).clearItems();
        verify(view).setCurrentSelection("A");
        verify(view).addItem(eq("A>B"), any());
        verify(view).addItem(eq("A>B>C"), any());
        verify(view).show();
    }

    @Test
    public void testSelectItem() {
        reset(view);
        NavItem navItem = tree.getItemById("B");
        presenter.onItemSelected(navItem);

        verify(view).clearItems();
        verify(view).setCurrentSelection("A>B");
        verify(view).addItem(eq("A"), any());
        verify(view).addItem(eq("A>B>C"), any());
    }
}