/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.dashbuilder.dataset.ExpenseReportsData.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DisplayerCoordinatorTest extends AbstractDisplayerTest {

    DisplayerSettings byDepartment = DisplayerSettingsFactory.newPieChartSettings()
            .dataset(EXPENSES)
            .group(COLUMN_DEPARTMENT)
            .column(COLUMN_DEPARTMENT)
            .column(COLUMN_AMOUNT, SUM)
            .sort(COLUMN_DEPARTMENT, SortOrder.ASCENDING)
            .filterOn(false, true, true)
            .buildSettings();

    DisplayerSettings byYear = DisplayerSettingsFactory.newBarChartSettings()
            .dataset(EXPENSES)
            .group(COLUMN_DATE)
            .column(COLUMN_DATE)
            .column(COLUMN_AMOUNT, SUM)
            .filterOn(false, true, true)
            .sort(COLUMN_DATE, SortOrder.ASCENDING)
            .buildSettings();

    DisplayerSettings allRows = DisplayerSettingsFactory.newTableSettings()
            .dataset(EXPENSES)
            .column(COLUMN_DEPARTMENT)
            .column(COLUMN_CITY)
            .column(COLUMN_EMPLOYEE)
            .column(COLUMN_AMOUNT)
            .column(COLUMN_DATE)
            .filterOn(true, false, true)
            .buildSettings();

    DisplayerCoordinator displayerCoordinator;
    AbstractDisplayer allRowsTable;
    AbstractDisplayer deptPieChart;
    AbstractDisplayer yearBarChart;

    @Mock
    DisplayerListener listener;

    @Before
    public void init() throws Exception {
        super.init();

        allRowsTable = createNewDisplayer(allRows);
        deptPieChart = createNewDisplayer(byDepartment);
        yearBarChart = createNewDisplayer(byYear);

        displayerCoordinator = new DisplayerCoordinator(rendererManager);
        displayerCoordinator.addDisplayers(allRowsTable, deptPieChart, yearBarChart);
        displayerCoordinator.addListener(listener);
    }

    @Test
    public void testDrawAll() {
        displayerCoordinator.drawAll();

        verify(listener, times(3)).onDataLookup(any(Displayer.class));
        verify(listener, times(3)).onDraw(any(Displayer.class));
    }

    @Test
    public void testFilterPropagations() {
        displayerCoordinator.drawAll();

        // Click on the "Engineering" slice
        reset(listener);
        deptPieChart.filterUpdate(COLUMN_DEPARTMENT, 0);

        // Check the allRowsTable receives the filter request
        DataSet dataSet = allRowsTable.getDataSetHandler().getLastDataSet();
        assertEquals(dataSet.getRowCount(), 19);
        verify(listener).onDataLookup(allRowsTable);
        verify(listener).onRedraw(allRowsTable);
    }

    @Test
    public void testFilterWithNull() {
        // Insert a null entry into the dataset
        DataSet expensesDataSet = clientDataSetManager.getDataSet(EXPENSES);
        int column = expensesDataSet.getColumnIndex(expensesDataSet.getColumnById(COLUMN_DEPARTMENT));
        expensesDataSet.setValueAt(0, column, null);

        // Draw the charts
        displayerCoordinator.drawAll();

        // Click on the "Engineering" slice
        reset(listener);
        deptPieChart.filterUpdate(COLUMN_DEPARTMENT, 1);

        // Check the allRowsTable receives the filter request
        DataSet dataSet = allRowsTable.getDataSetHandler().getLastDataSet();
        verify(listener, never()).onError(any(Displayer.class), any(ClientRuntimeError.class));
        verify(listener).onDataLookup(allRowsTable);
        verify(listener).onRedraw(allRowsTable);
        assertEquals(dataSet.getRowCount(), 18);
    }

    /**
     * Avoid IndexOutOfBoundsException caused when a filter is notified to
     * a table consuming the whole data set (no data lookup columns set).
     */
    @Test
    public void testFullTableFilterEvent() {

        AbstractDisplayer tableNoColumns = createNewDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(EXPENSES)
                .filterOn(true, false, true)
                .buildSettings());

        displayerCoordinator = new DisplayerCoordinator(rendererManager);
        displayerCoordinator.addDisplayers(deptPieChart, tableNoColumns);
        displayerCoordinator.addListener(listener);
        displayerCoordinator.drawAll();

        // Click on the "Engineering" slice
        reset(listener);
        deptPieChart.filterUpdate(COLUMN_DEPARTMENT, 0);

        // Check the allRowsTable receives the filter request
        DataSet dataSet = allRowsTable.getDataSetHandler().getLastDataSet();
        assertEquals(dataSet.getRowCount(), 19);
        verify(listener).onDataLookup(allRowsTable);
        verify(listener).onRedraw(tableNoColumns);
   }
}