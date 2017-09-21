package org.dashbuilder.client.navigation.widget;

import java.util.Collections;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.dashbuilder.navigation.NavFactory;
import org.dashbuilder.navigation.NavItem;
import org.dashbuilder.navigation.NavTree;
import org.dashbuilder.navigation.impl.NavTreeBuilder;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.client.authz.PerspectiveTreeProvider;
import org.uberfire.client.mvp.PlaceManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class NavTreeEditorTest {

    @Mock
    private NavTreeEditor.View viewM;
    @Mock
    private SyncBeanManager beanManagerM;
    @Mock
    private PerspectiveTreeProvider perspectiveTreeProviderM;
    @Mock
    private PlaceManager placeManagerM;
    @Mock
    private TargetPerspectiveEditor targetPerspectiveEditorM;
    @Mock
    private PerspectivePluginManager perspectivePluginManagerM;
    @Mock
    private NavItemEditor.View navItemEditorViewM;
    @Mock
    private NavItem navItemM;
    @Mock
    private SyncBeanDef<NavItemEditor> navItemEditorBeanDef;

    private NavItemEditor navItemEditor;

    NavTree NAV_TREE = new NavTreeBuilder()
            .group("root", "root", "root", true)
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
        navItemEditor = spy(new NavItemEditor(navItemEditorViewM, placeManagerM, targetPerspectiveEditorM, perspectivePluginManagerM));
        when(beanManagerM.lookupBean(NavItemEditor.class)).thenReturn(navItemEditorBeanDef);
        when(navItemEditorBeanDef.newInstance()).thenReturn(navItemEditor);
    }

    @Test
    public void testNewPerspectiveEnabled() {
        NavTreeEditor treeEditor = new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM);
        treeEditor.setNewPerspectiveEnabled(true);
        assertTrue(treeEditor.isNewPerspectiveEnabled(NAV_TREE.getItemById("level1b")));

        treeEditor.setNewPerspectiveEnabled("level1b", false);
        assertFalse(treeEditor.isNewPerspectiveEnabled(NAV_TREE.getItemById("level1b")));
        assertTrue(treeEditor.isNewPerspectiveEnabled(NAV_TREE.getItemById("level2b")));

        treeEditor.setNewPerspectiveEnabled("level1b", false).applyToAllChildren();
        assertFalse(treeEditor.isNewPerspectiveEnabled(NAV_TREE.getItemById("level1b")));
        assertFalse(treeEditor.isNewPerspectiveEnabled(NAV_TREE.getItemById("level2b")));
    }

    @Test
    public void testNewDividerEnabled() {
        NavTreeEditor treeEditor = new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM);
        treeEditor.setNewDividerEnabled(true);
        assertTrue(treeEditor.isNewDividerEnabled(NAV_TREE.getItemById("level1b")));

        treeEditor.setNewDividerEnabled("level1b", false);
        assertFalse(treeEditor.isNewDividerEnabled(NAV_TREE.getItemById("level1b")));
        assertTrue(treeEditor.isNewDividerEnabled(NAV_TREE.getItemById("level2b")));

        treeEditor.setNewDividerEnabled("level1b", false).applyToAllChildren();
        assertFalse(treeEditor.isNewDividerEnabled(NAV_TREE.getItemById("level1b")));
        assertFalse(treeEditor.isNewDividerEnabled(NAV_TREE.getItemById("level2b")));
    }

    @Test
    public void testAllSubgroupsAllowed() {
        NavTreeEditor treeEditor = spy(new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM));
        treeEditor.setMaxLevels(-1);
        treeEditor.edit(NAV_TREE);

        verify(treeEditor, never()).createNavItemEditor(any(), anyBoolean(), anyBoolean(), anyBoolean(), eq(false));
    }

    @Test
    public void testNoSubgroupsAllowed() {
        NavTreeEditor treeEditor = spy(new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM));
        treeEditor.setMaxLevels(1);
        treeEditor.edit(NAV_TREE);

        verify(treeEditor, never()).createNavItemEditor(any(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
    }

    @Test
    public void testSubgroupNotAllowed() {
        NavItem root = NAV_TREE.getItemById("root");
        NavItem level1a = NAV_TREE.getItemById("level1a");
        NavItem level2a = NAV_TREE.getItemById("level2a");
        NavItem level1b = NAV_TREE.getItemById("level1b");
        NavItem level2b = NAV_TREE.getItemById("level2b");

        NavTreeEditor treeEditor = spy(new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM));
        treeEditor.setMaxLevels("level1a", 1);
        treeEditor.edit(NAV_TREE);

        verify(treeEditor).createNavItemEditor(eq(root), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
        verify(treeEditor, never()).createNavItemEditor(eq(level1a), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
        verify(treeEditor, never()).createNavItemEditor(eq(level2a), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
        verify(treeEditor).createNavItemEditor(eq(level1b), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
        verify(treeEditor).createNavItemEditor(eq(level2b), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
    }

    @Test
    public void testOnlyThreeLevelsAllowed() {
        NavItem root = NAV_TREE.getItemById("root");
        NavItem level1a = NAV_TREE.getItemById("level1a");
        NavItem level2a = NAV_TREE.getItemById("level2a");
        NavItem level1b = NAV_TREE.getItemById("level1b");
        NavItem level2b = NAV_TREE.getItemById("level2b");

        NavTreeEditor treeEditor = spy(new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM));
        treeEditor.setMaxLevels("root", 3);
        treeEditor.edit(NAV_TREE);

        verify(treeEditor).createNavItemEditor(eq(root), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
        verify(treeEditor).createNavItemEditor(eq(level1a), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
        verify(treeEditor, never()).createNavItemEditor(eq(level2a), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
        verify(treeEditor).createNavItemEditor(eq(level1b), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
        verify(treeEditor, never()).createNavItemEditor(eq(level2b), anyBoolean(), anyBoolean(), anyBoolean(), eq(true));
    }

    @Test
    public void testNewPerspectiveDisabled() {
        NavTreeEditor treeEditor = new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM);
        treeEditor.setNewPerspectiveEnabled("level1a", false);
        treeEditor.edit(NAV_TREE);

        verify(navItemEditor, times(4)).setNewPerspectiveEnabled(true);
        verify(navItemEditor, times(1)).setNewPerspectiveEnabled(false);
    }

    @Test
    public void testFinishEdition() {
        NavTreeEditor treeEditor = spy(new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM));
        treeEditor.edit(NAV_TREE);

        navItemEditor.onNewSubGroup();
        navItemEditor.finishEditing();

        assertNull(treeEditor.getCurrentlyEditedItem());
    }

    @Test
    public void itShouldBeImpossibleToOpenMultipleNavItemEditorInputs() { // DASHBUILDE-217
        NavTreeEditor treeEditor = new NavTreeEditor(viewM,
                                                     beanManagerM,
                                                     perspectiveTreeProviderM);

        NavTree tree = NavFactory.get().createNavTree();
        treeEditor.edit(tree);

        NavItemEditor first = mock(NavItemEditor.class);
        NavItemEditor second = mock(NavItemEditor.class);
        NavItem firstItem = mock(NavItem.class);
        when(first.getNavItem()).thenReturn(firstItem);

        treeEditor.onItemEditStarted(first);
        treeEditor.onItemEditStarted(second);

        verify(first).cancelEdition();
    }

    @Test
    public void whenItemEditFinishedNavTreeEditorCleared() {
        NavTreeEditor treeEditor = new NavTreeEditor(viewM,
                                                     beanManagerM,
                                                     perspectiveTreeProviderM);

        // This creates onItemEditFinishedCallback for the editor
        NavItemEditor navItemEditor = treeEditor.createNavItemEditor(navItemM,
                                                                     false,
                                                                     false,
                                                                     false,
                                                                     false);

        assertFalse(treeEditor.currentlyEditedItem.isPresent());

        // When item editing starts the item is remembered in the tree
        treeEditor.onItemEditStarted(navItemEditor);
        assertEquals(navItemEditor,
                     treeEditor.currentlyEditedItem.get());

        // When item editing finishes, it is cleaned from the treeEditor and view resets to "non-editing" state
        navItemEditor.finishEditing();
        verify(navItemEditorViewM).finishItemEdition();
        assertFalse(treeEditor.currentlyEditedItem.isPresent());
    }

    @Test
    public void testExcludeNavItem() {
        NavTreeEditor treeEditor = new NavTreeEditor(viewM, beanManagerM, perspectiveTreeProviderM);
        treeEditor.setNavItemIdsExcluded(Collections.singletonList("level2a"));
        treeEditor.edit(NAV_TREE);
        verify(viewM, times(4)).addItemEditor(any());

        reset(viewM);
        treeEditor.setNavItemIdsExcluded(Collections.singletonList("level1a"));
        treeEditor.edit(NAV_TREE);
        verify(viewM, times(3)).addItemEditor(any());
    }
}
