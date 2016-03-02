/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataset.client;

import javax.enterprise.event.Event;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFormatter;
import org.dashbuilder.dataset.ExpenseReportsData;
import org.dashbuilder.dataset.events.DataSetModifiedEvent;
import org.dashbuilder.dataset.events.DataSetPushOkEvent;
import org.dashbuilder.dataset.events.DataSetPushingEvent;
import org.dashbuilder.dataset.service.DataSetDefServices;
import org.dashbuilder.dataset.service.DataSetExportServices;
import org.dashbuilder.dataset.service.DataSetLookupServices;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

public abstract class AbstractDataSetTest {

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

    protected ClientDataSetCore clientDataSetCore;
    protected DataSetClientServices clientServices;
    protected ClientDataSetManager clientDataSetManager;
    protected DataSet expensesDataSet;
    protected DataSetFormatter dataSetFormatter = new DataSetFormatter();

    public static final String EXPENSES = "expenses";

    public void initClientFactory() {
        clientDataSetCore = ClientDataSetCore.get();
        clientDataSetCore.setClientDateFormatter(new ClientDateFormatterMock());
        clientDataSetCore.setChronometer(new ChronometerMock());
    }

    public void initClientDataSetManager() {
        clientDataSetManager = clientDataSetCore.getClientDataSetManager();
    }

    public void initDataSetClientServices() {
        clientServices = new DataSetClientServices(
                clientDataSetManager,
                clientDataSetCore.getAggregateFunctionManager(),
                clientDataSetCore.getIntervalBuilderLocator(),
                dataSetPushingEvent,
                dataSetPushOkEvent,
                dataSetModifiedEvent,
                dataSetLookupServicesCaller,
                dataSetDefServicesCaller,
                dataSetExportServicesCaller);
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
        registerExpensesDataSet();

        when(dataSetLookupServicesCaller.call(any(RemoteCallback.class))).thenReturn(dataSetLookupServices);
    }


    public void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}