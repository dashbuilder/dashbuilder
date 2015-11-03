/**
 * Copyright 2015 JBoss Inc
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
package org.dashbuilder.displayer.client;

import java.util.List;
import javax.enterprise.event.Event;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFormatter;
import org.dashbuilder.dataset.ExpenseReportsData;
import org.dashbuilder.dataset.client.ClientDataSetManager;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.ClientFactory;
import org.dashbuilder.dataset.events.DataSetModifiedEvent;
import org.dashbuilder.dataset.events.DataSetPushOkEvent;
import org.dashbuilder.dataset.events.DataSetPushingEvent;
import org.dashbuilder.dataset.service.DataSetDefServices;
import org.dashbuilder.dataset.service.DataSetExportServices;
import org.dashbuilder.dataset.service.DataSetLookupServices;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.formatter.ValueFormatterRegistry;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

public abstract class AbstractDisplayerTest {

    @Mock
    protected Event<DataSetPushingEvent> dataSetPushingEvent;

    @Mock
    protected Event<DataSetPushOkEvent> dataSetPushOkEvent;

    @Mock
    protected Event<DataSetModifiedEvent> dataSetModifiedEvent;

    @Mock
    protected Caller<DataSetLookupServices> dataSetLookupServicesCaller;

    @Mock
    protected DataSetLookupServices dataSetLookupServices;

    @Mock
    protected Caller<DataSetDefServices> dataSetDefServicesCaller;

    @Mock
    protected Caller<DataSetExportServices> dataSetExportServicesCaller;

    @Mock
    protected RendererManager rendererManager;

    @Mock
    protected RendererLibrary rendererLibrary;

    @Mock
    protected ValueFormatterRegistry formatterRegistry;

    protected ClientFactory clientFactory;
    protected DataSetClientServices clientServices;
    protected ClientDataSetManager clientDataSetManager;
    protected DisplayerLocator displayerLocator;
    protected DataSet expensesDataSet;
    protected DataSetFormatter dataSetFormatter = new DataSetFormatter();


    public static final String EXPENSES = "expenses";

    public void initClientFactory() {
        clientFactory = ClientFactory.get();
        clientFactory.setClientDateFormatter(new ClientDateFormatterMock());
        clientFactory.setChronometer(new ChronometerMock());
    }

    public void initClientDataSetManager() {
        clientDataSetManager = clientFactory.getClientDataSetManager();
    }

    public void initDataSetClientServices() {
        clientServices = new DataSetClientServices(
                clientDataSetManager,
                clientFactory.getAggregateFunctionManager(),
                clientFactory.getIntervalBuilderLocator(),
                dataSetPushingEvent,
                dataSetPushOkEvent,
                dataSetModifiedEvent,
                dataSetLookupServicesCaller,
                dataSetDefServicesCaller,
                dataSetExportServicesCaller);
    }

    public void initDisplayerLocator() {
        displayerLocator = new DisplayerLocator(clientServices,
                clientDataSetManager,
                rendererManager,
                formatterRegistry);
    }

    public void registerExpensesDataSet() throws Exception {
        expensesDataSet = ExpenseReportsData.INSTANCE.toDataSet();
        expensesDataSet.setUUID(EXPENSES);
        clientDataSetManager.registerDataSet(expensesDataSet);
    }

    @Before
    public void init() throws Exception {
        initClientFactory();
        initClientDataSetManager();
        initDataSetClientServices();
        initDisplayerLocator();
        registerExpensesDataSet();

        when(rendererManager.getRendererForDisplayer(any(DisplayerSettings.class))).thenReturn(rendererLibrary);
        when(dataSetLookupServicesCaller.call(any(RemoteCallback.class))).thenReturn(dataSetLookupServices);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new DisplayerMock(mock(AbstractDisplayer.View.class), null);
            }
        }).when(rendererLibrary).lookupDisplayer(any(DisplayerSettings.class));

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                List<Displayer> displayerList = (List<Displayer>) invocationOnMock.getArguments()[0];
                for (Displayer displayer : displayerList) {
                    displayer.draw();
                }
                return null;
            }
        }).when(rendererLibrary).draw(anyListOf(Displayer.class));
    }


    public void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }

    public AbstractDisplayer createNewDisplayer(DisplayerSettings settings) {
        return initDisplayer(new DisplayerMock(mock(AbstractDisplayer.View.class), null), settings);
    }

    public <D extends AbstractDisplayer> D initDisplayer(D displayer, DisplayerSettings settings) {
        displayer.setEvaluator(new DisplayerEvaluatorMock());
        displayer.setFormatter(new DisplayerFormatterMock());
        if (settings != null) {
            displayer.setDisplayerSettings(settings);
            displayer.setDataSetHandler(new DataSetHandlerImpl(clientServices, settings.getDataSetLookup()));
        }
        return displayer;
    }
}