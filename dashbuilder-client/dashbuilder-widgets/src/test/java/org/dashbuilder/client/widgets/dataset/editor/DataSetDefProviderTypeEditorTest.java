package org.dashbuilder.client.widgets.dataset.editor;

import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.client.widgets.dataset.event.DataSetDefCreationRequestEvent;
import org.dashbuilder.common.client.editor.list.HorizImageListEditor;
import org.dashbuilder.common.client.event.ValueChangeEvent;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.uberfire.mocks.EventSourceMock;

import java.util.Collection;

import javax.enterprise.event.Event;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class DataSetDefProviderTypeEditorTest {
    
    @Mock HorizImageListEditor<DataSetProviderType> provider;
    @Mock DataSetDefProviderTypeEditor.View view;
    @Mock EventSourceMock<DataSetDefCreationRequestEvent> createEvent;

    private DataSetDefProviderTypeEditor tested;
    
    @Before
    public void setup() {
        tested = spy(new DataSetDefProviderTypeEditor(provider, createEvent, view));

        final String typeTitle = "typeTitle";
        doReturn(typeTitle).when(tested).getTypeSelectorTitle(any(DataSetProviderType.class));
        final String typeText = "typeText";
        doReturn(typeText).when(tested).getTypeSelectorText(any(DataSetProviderType.class));
        final SafeUri imageUri = mock(SafeUri.class);
        doReturn(imageUri).when(tested).getTypeSelectorImageUri(any(DataSetProviderType.class));
    }

    @Test
    public void testInit() throws Exception {
        tested.init();
        verify(view, times(1)).init(tested);
        verify(view, times(1)).initWidgets(any(IsWidget.class));
        final ArgumentCaptor<Collection> actualEntriesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(provider, times(1)).setEntries(actualEntriesCaptor.capture());
        final Collection actualEntries = actualEntriesCaptor.getValue();
        assertEquals(4, actualEntries.size());
    }

    @Test
    public void testProviderInstance() throws Exception {
        assertEquals(provider, tested.provider());
    }

    @Test
    public void testProviderSelected() throws Exception {
        tested.onItemClicked(new ValueChangeEvent<>(provider, null, DataSetProviderType.BEAN));
        verify(createEvent).fire(any());
    }
}
