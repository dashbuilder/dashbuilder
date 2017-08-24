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
package org.dashbuilder.client.navigation.widget;

import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.dashbuilder.navigation.NavFactory;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.impl.NavTreeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.client.authz.PerspectiveTreeProvider;
import org.uberfire.ext.widgets.common.client.dropdown.PerspectiveDropDown;
import org.uberfire.mvp.Command;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TargetPerspectiveEditorTest {

    @Mock
    TargetPerspectiveEditor.View view;

    @Mock
    PerspectiveDropDown perspectiveDropDown;

    @Mock
    PerspectiveTreeProvider perspectiveTreeProvider;

    @Mock
    Command updateCommand;

    TargetPerspectiveEditor presenter;

    NavTree NAV_TREE = new NavTreeBuilder()
            .group("root", "root", "root", true)
                .group("level1a", "level1a", "level1a", true)
                .endGroup()
                .group("level1b", "level1b", "level1b", true)
                .endGroup()
            .build();

    @Before
    public void setUp() throws Exception {
        presenter = new TargetPerspectiveEditor(view, perspectiveDropDown, perspectiveTreeProvider);
        presenter.setNavTree(NAV_TREE);
        presenter.setPerspectiveId("A");
        presenter.setNavGroupId("level1a");
        presenter.setOnUpdateCommand(updateCommand);
        presenter.show();
    }

    @Test
    public void testShow() {
        verify(perspectiveDropDown).setSelectedPerspective("A");
        verify(view).setPerspectiveSelector(perspectiveDropDown);
        verify(view).clearNavGroupItems();
        verify(view).setNavGroupSelection(eq("root>level1a"), any());
        verify(view).addNavGroupItem(eq("root"), any());
        verify(view, never()).addNavGroupItem(eq("root>level1a"), any());
        verify(view).addNavGroupItem(eq("root>level1b"), any());
    }

    @Test
    public void testGroupChange() {
        reset(view);
        presenter.onGroupSelected("level1b");

        verify(view).clearNavGroupItems();
        verify(view).setNavGroupSelection(eq("root>level1b"), any());
        verify(view).addNavGroupItem(eq("root"), any());
        verify(view).addNavGroupItem(eq("root>level1a"), any());
        verify(view, never()).addNavGroupItem(eq("root>level1b"), any());

        verify(updateCommand).execute();
    }
}