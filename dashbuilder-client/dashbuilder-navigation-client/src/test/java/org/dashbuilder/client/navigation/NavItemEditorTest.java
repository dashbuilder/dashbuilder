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

import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.dashbuilder.client.navigation.widget.NavItemEditor;
import org.dashbuilder.navigation.NavDivider;
import org.dashbuilder.navigation.NavFactory;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.workbench.NavWorkbenchCtx;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.widgets.common.client.dropdown.PerspectiveDropDown;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NavItemEditorTest {

    @Mock
    NavItemEditor.View view;

    @Mock
    PlaceManager placeManager;

    @Mock
    PerspectiveDropDown perspectiveDropDown;

    @Mock
    PerspectivePluginManager perspectivePluginManager;

    NavItemEditor presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new NavItemEditor(view, placeManager, perspectiveDropDown, perspectivePluginManager);
    }

    @Test
    public void testEditGroup() {
        NavGroup navGroup = NavFactory.get().createNavGroup();
        navGroup.setId("id");
        navGroup.setName("name");
        navGroup.setDescription("description");
        navGroup.setModifiable(false);
        presenter.edit(navGroup);

        verify(view).setItemName("name");
        verify(view).setItemDescription("description");
        verify(view).setItemEditable(false);
        verify(view).setItemType(NavItemEditor.ItemType.GROUP);
        verify(view, never()).setContextWidget(any());
    }

    @Test
    public void testEditDivider() {
        NavDivider divider = NavFactory.get().createDivider();
        divider.setId("id");
        divider.setName("name");
        divider.setDescription("description");
        divider.setModifiable(true);
        presenter.edit(divider);

        verify(view).setItemName("name");
        verify(view).setItemDescription("description");
        verify(view).setItemEditable(false);
        verify(view).setItemType(NavItemEditor.ItemType.DIVIDER);
        verify(view, never()).setContextWidget(any());

        reset(view);
        presenter.onItemClick();
        verify(view, never()).startItemEdition();
    }

    @Test
    public void testEditPerspective() {
        NavItem navItem = NavFactory.get().createNavItem();
        navItem.setModifiable(false);
        navItem.setContext(NavWorkbenchCtx.perspective("p1").toString());
        presenter.edit(navItem);

        verify(view).setItemName("--------------");
        verify(view).setItemEditable(false);
        verify(view).setItemType(NavItemEditor.ItemType.PERSPECTIVE);
        verify(view).setContextWidget(perspectiveDropDown);
    }
}