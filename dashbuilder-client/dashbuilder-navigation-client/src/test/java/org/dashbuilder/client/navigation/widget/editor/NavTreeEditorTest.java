package org.dashbuilder.client.navigation.widget.editor;

import java.util.Collections;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.event.NavItemEditCancelledEvent;
import org.dashbuilder.client.navigation.event.NavItemEditStartedEvent;
import org.dashbuilder.client.navigation.event.NavItemGotoEvent;
import org.dashbuilder.client.navigation.event.NavTreeChangedEvent;
import org.dashbuilder.client.navigation.event.NavTreeLoadedEvent;
import org.dashbuilder.client.navigation.impl.NavigationManagerImpl;
import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.dashbuilder.navigation.NavFactory;
import org.dashbuilder.navigation.NavGroup;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.impl.NavTreeBuilder;
import org.dashbuilder.navigation.service.NavigationServices;
import org.dashbuilder.navigation.workbench.NavSecurityController;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.client.authz.PerspectiveTreeProvider;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.plugin.client.perspective.editor.generator.PerspectiveEditorActivity;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.mvp.Command;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class NavTreeEditorTest {

    @Mock
    NavTreeEditorView view;

    @Mock
    SyncBeanManager beanManager;

    @Mock
    PerspectiveTreeProvider perspectiveTreeProvider;

    @Mock
    EventSourceMock<NavItemEditStartedEvent> navItemEditStartedEvent;

    @Mock
    EventSourceMock<NavItemEditCancelledEvent> navItemEditCancelledEvent;

    @Mock
    EventSourceMock<NavItemGotoEvent> navItemGotoEvent;

    @Mock
    EventSourceMock<NavTreeLoadedEvent> navTreeLoadedEvent;

    @Mock
    EventSourceMock<NavTreeChangedEvent> navTreeChangedEvent;

    @Mock
    NavigationServices navServices;

    @Mock
    NavSecurityController navController;

    @Mock
    PlaceManager placeManager;

    @Mock
    TargetPerspectiveEditor targetPerspectiveEditor;

    @Mock
    PerspectivePluginManager perspectivePluginManager;

    @Mock
    NavItemDefaultEditorView navItemEditorView;

    @Mock
    NavRootNodeEditorView navRootNodeEditorView;

    @Mock
    SyncBeanDef<NavItemDefaultEditor> navItemEditorBeanDef;

    @Mock
    SyncBeanDef<NavRootNodeEditor> navRootNodeEditorBeanDef;

    @Mock
    Command updateCommand;

    NavigationManager navigationManager;
    NavTreeEditor navTreeEditor;
    NavItemDefaultEditor navItemEditor;
    NavRootNodeEditor navRootNodeEditor;

    NavTree NAV_TREE = new NavTreeBuilder()
            .group("level1a", "level1a", "level1a", true)
                .group("level2a", "level2a", "level2a", true)
                .endGroup()
            .endGroup()
            .group("level1b", "level1b", "level1b", true)
                .group("level2b", "level2b", "level2b", true)
                .endGroup()
            .endGroup()
            .build();

    @Before
    public void setUp() {
        navigationManager = new NavigationManagerImpl(new CallerMock<>(navServices), navController,
                navTreeLoadedEvent, navTreeChangedEvent, navItemGotoEvent);

        navTreeEditor = spy(new NavTreeEditor(view, navigationManager, beanManager,
                placeManager, perspectiveTreeProvider, targetPerspectiveEditor,
                perspectivePluginManager, navItemEditStartedEvent, navItemEditCancelledEvent));

        navTreeEditor.setChildEditorClass(NavRootNodeEditor.class);

        navItemEditor = spy(new NavItemDefaultEditor(navItemEditorView, beanManager, placeManager,
                perspectiveTreeProvider, targetPerspectiveEditor, perspectivePluginManager,
                navItemEditStartedEvent, navItemEditCancelledEvent));

        navRootNodeEditor = spy(new NavRootNodeEditor(navRootNodeEditorView, beanManager, placeManager,
                perspectiveTreeProvider, targetPerspectiveEditor, perspectivePluginManager,
                navItemEditStartedEvent, navItemEditCancelledEvent));

        when(beanManager.lookupBean(NavItemDefaultEditor.class)).thenReturn(navItemEditorBeanDef);
        when(beanManager.lookupBean(NavRootNodeEditor.class)).thenReturn(navRootNodeEditorBeanDef);
        when(navItemEditorBeanDef.newInstance()).thenReturn(navItemEditor);
        when(navRootNodeEditorBeanDef.newInstance()).thenReturn(navRootNodeEditor);

        when(navItemEditorView.getItemName()).thenReturn("editor1");
        when(navRootNodeEditorView.getItemName()).thenReturn("editor2");

        SyncBeanDef perspBeanDef = mock(SyncBeanDef.class);
        PerspectiveEditorActivity perspActivity = mock(PerspectiveEditorActivity.class);
        when(beanManager.lookupBeans(PerspectiveActivity.class)).thenReturn(Collections.singleton(perspBeanDef));
        when(perspBeanDef.getInstance()).thenReturn(perspActivity);
    }

    @Test
    public void testNewPerspectiveEnabled() {
        navTreeEditor.getSettings().setNewPerspectiveEnabled(true);
        assertTrue(navTreeEditor.getSettings().isNewPerspectiveEnabled(NAV_TREE.getItemById("level1b")));

        navTreeEditor.setNewPerspectiveEnabled("level1b", false);
        assertFalse(navTreeEditor.getSettings().isNewPerspectiveEnabled(NAV_TREE.getItemById("level1b")));
        assertTrue(navTreeEditor.getSettings().isNewPerspectiveEnabled(NAV_TREE.getItemById("level2b")));

        navTreeEditor.setNewPerspectiveEnabled("level1b", false).applyToAllChildren();
        assertFalse(navTreeEditor.getSettings().isNewPerspectiveEnabled(NAV_TREE.getItemById("level1b")));
        assertFalse(navTreeEditor.getSettings().isNewPerspectiveEnabled(NAV_TREE.getItemById("level2b")));
    }

    @Test
    public void testNewDividerEnabled() {
        navTreeEditor.getSettings().setNewDividerEnabled(true);
        assertTrue(navTreeEditor.getSettings().isNewDividerEnabled(NAV_TREE.getItemById("level1b")));

        navTreeEditor.setNewDividerEnabled("level1b", false);
        assertFalse(navTreeEditor.getSettings().isNewDividerEnabled(NAV_TREE.getItemById("level1b")));
        assertTrue(navTreeEditor.getSettings().isNewDividerEnabled(NAV_TREE.getItemById("level2b")));

        navTreeEditor.setNewDividerEnabled("level1b", false).applyToAllChildren();
        assertFalse(navTreeEditor.getSettings().isNewDividerEnabled(NAV_TREE.getItemById("level1b")));
        assertFalse(navTreeEditor.getSettings().isNewDividerEnabled(NAV_TREE.getItemById("level2b")));
    }

    @Test
    public void testAllSubgroupsAllowed() {
        navTreeEditor.getSettings().setMaxLevels(-1);
        navTreeEditor.edit(NAV_TREE);

        verify(navTreeEditor, times(2)).createChildEditor(any());
        verify(navRootNodeEditor, times(2)).createChildEditor(any());
        verify(navItemEditor, never()).createChildEditor(any());
    }

    @Test
    public void testNoSubgroupsAllowed() {
        navTreeEditor.getSettings().setMaxLevels(1);
        navTreeEditor.edit(NAV_TREE);

        NavItem level1a = NAV_TREE.getItemById("level1a");
        NavItem level2a = NAV_TREE.getItemById("level2a");
        NavItem level1b = NAV_TREE.getItemById("level1b");
        NavItem level2b = NAV_TREE.getItemById("level2b");

        verify(navTreeEditor, never()).createChildEditor(eq(level1a));
        verify(navTreeEditor, never()).createChildEditor(eq(level1b));
        verify(navRootNodeEditor, never()).createChildEditor(eq(level2b));
        verify(navRootNodeEditor, never()).createChildEditor(eq(level2a));
    }

    @Test
    public void testSubgroupNotAllowed() {
        NavItem level1a = NAV_TREE.getItemById("level1a");
        NavItem level2a = NAV_TREE.getItemById("level2a");
        NavItem level1b = NAV_TREE.getItemById("level1b");
        NavItem level2b = NAV_TREE.getItemById("level2b");

        navTreeEditor.getSettings().setMaxLevels("level1a", 1);
        navTreeEditor.edit(NAV_TREE);

        verify(navTreeEditor).createChildEditor(eq(level1a));
        verify(navTreeEditor).createChildEditor(eq(level1b));
        verify(navRootNodeEditor).createChildEditor(eq(level2b));
        verify(navRootNodeEditor, never()).createChildEditor(eq(level2a));
    }

    @Test
    public void testOnlyThreeLevelsAllowed() {
        NavItem level1a = NAV_TREE.getItemById("level1a");
        NavItem level2a = NAV_TREE.getItemById("level2a");
        NavItem level1b = NAV_TREE.getItemById("level1b");
        NavItem level2b = NAV_TREE.getItemById("level2b");

        navTreeEditor.getSettings().setMaxLevels("root", 3);
        navTreeEditor.edit(NAV_TREE);

        verify(navTreeEditor).createChildEditor(eq(level1a));
        verify(navTreeEditor).createChildEditor(eq(level1b));
        verify(navTreeEditor, never()).createChildEditor(eq(level2a));
        verify(navTreeEditor, never()).createChildEditor(eq(level2b));
    }

    @Test
    public void testFinishEdition() {
        navTreeEditor.edit(NAV_TREE);

        navRootNodeEditor.newGroup();
        navRootNodeEditor.finishEdition();

        assertNull(navTreeEditor.getCurrentlyEditedItem());
    }

    @Test
    public void itShouldBeImpossibleToOpenMultipleNavItemEditorInputs() { // DASHBUILDE-217
        NavTree tree = NavFactory.get().createNavTree();
        navTreeEditor.edit(tree);

        NavItemEditor first = mock(NavItemEditor.class);
        NavItemEditor second = mock(NavItemEditor.class);
        NavItem firstItem = mock(NavItem.class);
        when(first.getNavItem()).thenReturn(firstItem);

        navTreeEditor.onItemEditStarted(new NavItemEditStartedEvent(first));
        navTreeEditor.onItemEditStarted(new NavItemEditStartedEvent(second));

        verify(first).cancelEdition();
    }

    @Test
    public void whenItemEditFinishedNavTreeEditorCleared() {
        navTreeEditor.edit(NAV_TREE);

        // When item editing starts the item is remembered in the tree
        NavItemEditor navItemEditor = navTreeEditor.newGroup();
        navTreeEditor.onItemEditStarted(new NavItemEditStartedEvent(navItemEditor));
        assertEquals(navItemEditor, navTreeEditor.currentlyEditedItem.get());

        // When item editing finishes, it is cleaned from the navTreeEditor and view resets to "non-editing" state
        navItemEditor.onItemUpdated();
        assertFalse(navTreeEditor.currentlyEditedItem.isPresent());
    }

    @Test
    public void testNewGroup() {
        navTreeEditor.setOnUpdateCommand(updateCommand);
        navTreeEditor.edit(NAV_TREE);
        navTreeEditor.collapse();
        assertFalse(navTreeEditor.isExpanded());

        reset(view);
        NavItemEditor navItemEditor = navTreeEditor.newGroup();
        assertEquals(((NavGroup) navTreeEditor.getNavItem()).getChildren().size(), 2);
        assertTrue(navTreeEditor.isExpanded());
        verify(navItemEditor).startEdition();
        verify(view, times(3)).addChild(any());
        verify(updateCommand, never()).execute();

        when(navRootNodeEditorView.getItemName()).thenReturn("A");
        navItemEditor.onChangesOk();
        verify(updateCommand).execute();
    }

    @Test
    public void testNewPerspective() {
        navTreeEditor.setOnUpdateCommand(updateCommand);
        navTreeEditor.edit(NAV_TREE);
        navTreeEditor.collapse();
        assertFalse(navTreeEditor.isExpanded());

        reset(view);
        NavItemEditor navItemEditor = navTreeEditor.newPerspective();
        assertEquals(((NavGroup) navTreeEditor.getNavItem()).getChildren().size(), 2);
        assertTrue(navTreeEditor.isExpanded());
        verify(navItemEditor).startEdition();
        verify(view, times(3)).addChild(any());
        verify(updateCommand, never()).execute();
    }

    @Test
    public void testNewDivider() {
        navTreeEditor.setOnUpdateCommand(updateCommand);
        navTreeEditor.edit(NAV_TREE);
        navTreeEditor.collapse();
        assertFalse(navTreeEditor.isExpanded());

        reset(view);
        navTreeEditor.newDivider();
        assertEquals(((NavGroup) navTreeEditor.getNavItem()).getChildren().size(), 3);
        verify(view, times(3)).addChild(any());
        verify(updateCommand).execute();
    }

    @Test
    public void testSaveAndCancel() {
        navTreeEditor.edit(NAV_TREE);
        NavItemEditor newEditor = navTreeEditor.newGroup();
        newEditor.onChangesOk();
        navTreeEditor.onSaveClicked();

        NavTree navTree = navTreeEditor.getNavTree();
        assertNotNull(navTree.getItemById(newEditor.getNavItem().getId()));
    }
}