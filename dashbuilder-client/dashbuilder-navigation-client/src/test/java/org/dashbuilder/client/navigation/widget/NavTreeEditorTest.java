package org.dashbuilder.client.navigation.widget;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
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
import org.uberfire.ext.widgets.common.client.dropdown.PerspectiveDropDown;

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
    private PerspectiveDropDown perspectiveDropDownM;
    @Mock
    private PerspectivePluginManager perspectivePluginManagerM;
    @Mock
    private NavItemEditor.View navItemEditorViewM;
    @Mock
    private NavItem navItemM;
    @Mock
    private NavItemEditor navItemEditorM;
    @Mock
    private SyncBeanDef<NavItemEditor> navItemEditorBeanDef;

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
        when(beanManagerM.lookupBean(NavItemEditor.class)).thenReturn(navItemEditorBeanDef);
        when(navItemEditorBeanDef.newInstance()).thenReturn(navItemEditorM);
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
    public void itShouldBeImpossibleToOpenMultipleNavItemEditorInputs() { // DASHBUILDE-217
        NavTreeEditor treeEditor = new NavTreeEditor(viewM,
                                                     beanManagerM,
                                                     perspectiveTreeProviderM);

        NavItemEditor first = mock(NavItemEditor.class);
        NavItemEditor second = mock(NavItemEditor.class);

        treeEditor.onItemEditStarted(first);
        treeEditor.onItemEditStarted(second);

        verify(first).finishEditing();
    }
    @Test
    public void whenItemEditFinishedNavTreeEditorCleared() {
        NavTreeEditor treeEditor = new NavTreeEditor(viewM,
                                                     beanManagerM,
                                                     perspectiveTreeProviderM);

        when(beanManagerM.lookupBean(NavItemEditor.class))
                .thenReturn(new MockSyncBeanDefProvidingNavItemEditor());

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

    // The sole purpose of this class is to provide NavItemEditor instance
    private class MockSyncBeanDefProvidingNavItemEditor implements SyncBeanDef<NavItemEditor> {

        @Override
        public NavItemEditor getInstance() {
            return null;
        }

        @Override
        public NavItemEditor newInstance() {
            return new NavItemEditor(navItemEditorViewM,
                                     placeManagerM,
                                     perspectiveDropDownM,
                                     perspectivePluginManagerM);
        }

        @Override
        public boolean isAssignableTo(Class<?> type) {
            return false;
        }

        @Override
        public Class<NavItemEditor> getType() {
            return null;
        }

        @Override
        public Class<?> getBeanClass() {
            return null;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return null;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return null;
        }

        @Override
        public boolean matches(Set<Annotation> annotations) {
            return false;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean isActivated() {
            return false;
        }

        @Override
        public boolean isDynamic() {
            return false;
        }
    }
}
