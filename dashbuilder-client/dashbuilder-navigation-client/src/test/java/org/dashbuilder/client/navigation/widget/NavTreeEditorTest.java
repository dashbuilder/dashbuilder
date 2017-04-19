package org.dashbuilder.client.navigation.widget;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.client.authz.PerspectiveTreeProvider;

import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class NavTreeEditorTest {

    @Mock
    private NavTreeEditor.View viewM;
    @Mock
    private SyncBeanManager beanManagerM;
    @Mock
    private PerspectiveTreeProvider perspectiveTreeProviderM;

    @Test
    public void itShouldBeImpossibleToOpenMultipleNavItemEditorInputs() { // DASHBUILDE-217
        NavTreeEditor treeEditor = new NavTreeEditor(viewM,
                                                     beanManagerM,
                                                     perspectiveTreeProviderM);

        NavItemEditor first = mock(NavItemEditor.class);
        NavItemEditor second = mock(NavItemEditor.class);

        treeEditor.onEditStarted(first);
        treeEditor.onEditStarted(second);

        verify(first).finishEditing();
    }
}
