package org.dashbuilder.client.widgets.dataset.editor.workflow.create;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.client.widgets.dataset.editor.attributes.DataSetDefBasicAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.driver.CSVDataSetDefAttributesDriver;
import org.dashbuilder.client.widgets.dataset.editor.workflow.DataSetEditorWorkflow;
import org.dashbuilder.client.widgets.dataset.event.CancelRequestEvent;
import org.dashbuilder.client.widgets.dataset.event.SaveRequestEvent;
import org.dashbuilder.client.widgets.dataset.event.TestDataSetRequestEvent;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.validations.dataset.CSVDataSetDefValidator;
import org.dashbuilder.validations.dataset.DataSetDefValidator;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class CSVDataSetBasicAttributesWorkflowTest  {

    @Mock SyncBeanManager beanManager;
    @Mock DataSetDefValidator dataSetDefValidator;
    @Mock EventSourceMock<SaveRequestEvent> saveRequestEvent;
    @Mock EventSourceMock<TestDataSetRequestEvent> testDataSetEvent;
    @Mock EventSourceMock<CancelRequestEvent> cancelRequestEvent;
    @Mock DataSetClientServices clientServices;
    @Mock CSVDataSetDef dataSetDef;
    @Mock CSVDataSetDefValidator csvDataSetDefValidator;
    @Mock DataSetDefBasicAttributesEditor basicAttributesEditor;
    @Mock org.dashbuilder.client.widgets.dataset.editor.csv.CSVDataSetDefAttributesEditor csvDataSetDefAttributesEditor;
    @Mock DataSetEditorWorkflow.View view;
    
    private CSVDataSetBasicAttributesWorkflow presenter;

    @Before
    public void setup() throws Exception {
        
        presenter = new CSVDataSetBasicAttributesWorkflow(clientServices, dataSetDefValidator, beanManager, basicAttributesEditor, saveRequestEvent,
                testDataSetEvent, cancelRequestEvent, view);
        when(dataSetDef.getProvider()).thenReturn(DataSetProviderType.CSV);
        when(dataSetDefValidator.validatorFor(any(DataSetProviderType.class))).thenReturn(csvDataSetDefValidator);
        
    }

    @Test
    public void testGetDriverClass() {
        assertEquals(CSVDataSetDefAttributesDriver.class, presenter.getDriverClass());
    }

    @Test
    public void testGetEditorClass() {
        assertEquals(org.dashbuilder.client.widgets.dataset.editor.csv.CSVDataSetDefAttributesEditor.class, presenter.getEditorClass());
    }
    
    @Test
    public void testValidateUsingFilePath() {
        presenter._setDataSetDef(dataSetDef);
        presenter.editor = csvDataSetDefAttributesEditor;
        when(csvDataSetDefAttributesEditor.isUsingFilePath()).thenReturn(true);
        presenter.validate();
        verify(csvDataSetDefValidator, times(1)).validateAttributes(dataSetDef, true);
        verify(csvDataSetDefValidator, times(0)).validate(any(CSVDataSetDef.class), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    public void testValidateUsingFileUrl() {
        presenter._setDataSetDef(dataSetDef);
        presenter.editor = csvDataSetDefAttributesEditor;
        when(csvDataSetDefAttributesEditor.isUsingFilePath()).thenReturn(false);
        presenter.validate();
        verify(csvDataSetDefValidator, times(1)).validateAttributes(dataSetDef, false);
        verify(csvDataSetDefValidator, times(0)).validate(any(CSVDataSetDef.class), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    public void testFlushDriverUsingFilePath() throws Exception {
        presenter._setDataSetDef(dataSetDef);
        presenter.editor = csvDataSetDefAttributesEditor;
        when(csvDataSetDefAttributesEditor.isUsingFilePath()).thenReturn(true);
        presenter.afterFlush();
        verify(dataSetDef, times(1)).setFileURL(null);
        verify(dataSetDef, times(0)).setFilePath(null);
    }

    @Test
    public void testFlushDriverUsingFileUrl() throws Exception {
        presenter._setDataSetDef(dataSetDef);
        presenter.editor = csvDataSetDefAttributesEditor;
        when(csvDataSetDefAttributesEditor.isUsingFilePath()).thenReturn(false);
        presenter.afterFlush();
        verify(dataSetDef, times(1)).setFilePath(null);
        verify(dataSetDef, times(0)).setFileURL(null);
    }
    
}
