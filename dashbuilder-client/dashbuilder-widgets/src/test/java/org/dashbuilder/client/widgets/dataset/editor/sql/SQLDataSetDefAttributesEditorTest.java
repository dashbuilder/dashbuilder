package org.dashbuilder.client.widgets.dataset.editor.sql;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.common.client.editor.ValueBoxEditor;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class SQLDataSetDefAttributesEditorTest {

    @Mock ValueBoxEditor<String> dataSource;
    @Mock ValueBoxEditor<String> dbSchema;
    @Mock ValueBoxEditor<String> dbTable;
    @Mock ValueBoxEditor<String> dbSQL;
    @Mock SQLDataSetDefAttributesEditor.View view;
    
    private SQLDataSetDefAttributesEditor presenter;
    
    
    @Before
    public void setup() {
        presenter = new SQLDataSetDefAttributesEditor(dataSource, dbSchema, dbTable, dbSQL, view);
    }

    @Test
    public void testInit() {
        presenter.init();
        verify(view, times(1)).init(presenter);
        verify(view, times(1)).initWidgets(any(ValueBoxEditor.View.class), any(ValueBoxEditor.View.class), 
                any(ValueBoxEditor.View.class) ,any(ValueBoxEditor.View.class));
        verify(dataSource, times(1)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbSchema, times(1)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbTable, times(1)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbSQL, times(1)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(view, times(1)).query();
        verify(view, times(0)).table();
    }
    
    @Test
    public void testDataSource() {
        assertEquals(dataSource, presenter.dataSource());
    }

    @Test
    public void testDbSchema() {
        assertEquals(dbSchema, presenter.dbSchema());
    }

    @Test
    public void testDbTable() {
        assertEquals(dbTable, presenter.dbTable());
    }

    @Test
    public void testDbSQL() {
        assertEquals(dbSQL, presenter.dbSQL());
    }

    @Test
    public void testSetValueUsingTable() {
        final SQLDataSetDef dataSetDef = mock(SQLDataSetDef.class);
        when(dataSetDef.getUUID()).thenReturn("uuid1");
        when(dataSetDef.getName()).thenReturn("name1");
        when(dataSetDef.getProvider()).thenReturn(DataSetProviderType.SQL);
        when(dataSetDef.getDbTable()).thenReturn("table1");
        when(dataSetDef.getDbSQL()).thenReturn(null);
        presenter.setValue(dataSetDef);
        assertEquals(false, presenter.isUsingQuery());
        verify(view, times(1)).table();
        verify(view, times(0)).init(presenter);
        verify(view, times(0)).initWidgets(any(ValueBoxEditor.View.class), any(ValueBoxEditor.View.class),
                any(ValueBoxEditor.View.class) ,any(ValueBoxEditor.View.class));
        verify(dataSource, times(0)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbSchema, times(0)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbTable, times(0)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbSQL, times(0)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(view, times(0)).query();
    }

    @Test
    public void testSetValueUsingQuery() {
        final SQLDataSetDef dataSetDef = mock(SQLDataSetDef.class);
        when(dataSetDef.getUUID()).thenReturn("uuid1");
        when(dataSetDef.getName()).thenReturn("name1");
        when(dataSetDef.getProvider()).thenReturn(DataSetProviderType.SQL);
        when(dataSetDef.getDbTable()).thenReturn(null);
        when(dataSetDef.getDbSQL()).thenReturn("query1");
        presenter.setValue(dataSetDef);
        assertEquals(true, presenter.isUsingQuery());
        verify(view, times(1)).query();
        verify(view, times(0)).init(presenter);
        verify(view, times(0)).initWidgets(any(ValueBoxEditor.View.class), any(ValueBoxEditor.View.class),
                any(ValueBoxEditor.View.class) ,any(ValueBoxEditor.View.class));
        verify(dataSource, times(0)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbSchema, times(0)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbTable, times(0)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(dbSQL, times(0)).addHelpContent(anyString(), anyString(), any(Placement.class));
        verify(view, times(0)).table();
    }
    
}
