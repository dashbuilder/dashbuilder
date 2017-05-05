package org.dashbuilder.client.navigation.widget;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.client.navigation.plugin.PerspectivePluginManager;
import org.dashbuilder.navigation.NavItem;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
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
