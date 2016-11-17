package org.dashbuilder.client.widgets.dataset.editor.workflow.edit;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.client.widgets.dataset.editor.driver.BeanDataSetDefDriver;
import org.dashbuilder.client.widgets.dataset.editor.workflow.DataSetEditorWorkflow;
import org.dashbuilder.client.widgets.dataset.event.CancelRequestEvent;
import org.dashbuilder.client.widgets.dataset.event.SaveRequestEvent;
import org.dashbuilder.client.widgets.dataset.event.TestDataSetRequestEvent;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.editor.DataSetDefRefreshAttributesEditor;
import org.dashbuilder.dataset.def.BeanDataSetDef;
import org.dashbuilder.validations.dataset.BeanDataSetDefValidator;
import org.dashbuilder.validations.dataset.DataSetDefValidator;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class BeanDataSetEditWorkflowTest {

    @Mock SyncBeanManager beanManager;
    @Mock DataSetDefValidator dataSetDefValidator;
    @Mock EventSourceMock<SaveRequestEvent> saveRequestEvent;
    @Mock EventSourceMock<TestDataSetRequestEvent> testDataSetEvent;
    @Mock EventSourceMock<CancelRequestEvent> cancelRequestEvent;
    @Mock DataSetClientServices clientServices;
    @Mock DataSet dataSet;
    @Mock DataSetEditorWorkflow.View view;
    @Mock org.dashbuilder.client.widgets.dataset.editor.bean.BeanDataSetEditor beanEditor;
    @Mock BeanDataSetDefValidator beanDataSetDefValidator;
    @Mock BeanDataSetDef dataSetDef;
    @Mock DataSetDefRefreshAttributesEditor refreshEditor;
    
    private BeanDataSetEditWorkflow presenter;
    
    @Before
    public void setup() throws Exception {
        presenter = new BeanDataSetEditWorkflow(clientServices, dataSetDefValidator, beanManager,
                saveRequestEvent, testDataSetEvent, cancelRequestEvent, view);
        when(dataSetDef.getProvider()).thenReturn(DataSetProviderType.BEAN);
        when(dataSetDefValidator.validatorFor(any(DataSetProviderType.class))).thenReturn(beanDataSetDefValidator);
        when(beanEditor.refreshEditor()).thenReturn(refreshEditor);
        when(refreshEditor.isRefreshEnabled()).thenReturn(true);
    }

    @Test
    public void testGetDriverClass() {
        assertEquals(BeanDataSetDefDriver.class, presenter.getDriverClass());
    }

    @Test
    public void testGetEditorClass() {
        assertEquals(org.dashbuilder.client.widgets.dataset.editor.bean.BeanDataSetEditor.class, presenter.getEditorClass());
    }
    
    @Test
    public void testValidate() {
        presenter._setDataSetDef(dataSetDef);
        presenter.validate(true, true, true);
        verify(beanDataSetDefValidator, times(1)).validate(dataSetDef, true, true, true);
        verify(beanDataSetDefValidator, times(0)).validateAttributes(dataSetDef, true);
    }

}
