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
package org.dashbuilder.renderer.client.metric;

import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.AbstractDisplayerTest;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.dashbuilder.dataset.ExpenseReportsData.*;

@RunWith(MockitoJUnitRunner.class)
public class MetricDisplayerTest extends AbstractDisplayerTest {

    public MetricDisplayer createMetricDisplayer(DisplayerSettings settings) {
        MetricDisplayer displayer = initDisplayer(new MetricDisplayer(mock(MetricDisplayer.View.class)), settings);
        displayer.addListener(listener);
        return displayer;
    }

    @Mock
    DisplayerListener listener;

    @Test
    public void testDraw() {
        DisplayerSettings engExpenses = DisplayerSettingsFactory.newMetricSettings()
                .dataset(EXPENSES)
                .filter(COLUMN_DEPARTMENT, FilterFactory.equalsTo("Engineering"))
                .column(COLUMN_AMOUNT, AggregateFunctionType.SUM)
                .title("Title").titleVisible(true)
                .width(300).height(200)
                .margins(10, 20, 30, 40)
                .backgroundColor("FDE8D4")
                .filterOff(true)
                .buildSettings();

        MetricDisplayer presenter = createMetricDisplayer(engExpenses);
        MetricDisplayer.View view = presenter.getView();
        presenter.draw();

        verify(view).setWidth(300);
        verify(view).setHeight(200);
        verify(view).setMarginTop(10);
        verify(view).setMarginBottom(20);
        verify(view).setMarginLeft(30);
        verify(view).setMarginRight(40);
        verify(view).showTitle("Title");
        verify(view).setFilterEnabled(false);
        verify(view).setValue("7,650.16");
    }

    @Test
    public void testNoData() {
        DisplayerSettings empty = DisplayerSettingsFactory.newMetricSettings()
                .dataset(EXPENSES)
                .filter(COLUMN_ID, FilterFactory.isNull())
                .column(COLUMN_AMOUNT)
                .buildSettings();

        MetricDisplayer presenter = createMetricDisplayer(empty);
        MetricDisplayer.View view = presenter.getView();
        presenter.draw();

        verify(view).nodata();
        verify(view, never()).setValue(anyString());
    }

    @Test
    public void testNoFilter() {
        DisplayerSettings empty = DisplayerSettingsFactory.newMetricSettings()
                .dataset(EXPENSES)
                .column(COLUMN_AMOUNT)
                .filterOn(false, true, true)
                .buildSettings();

        MetricDisplayer presenter = createMetricDisplayer(empty);
        MetricDisplayer.View view = presenter.getView();
        presenter.draw();

        reset(view);
        reset(listener);
        presenter.filterApply();

        verify(view, never()).setFilterActive(true);
        verify(listener, never()).onFilterEnabled(eq(presenter), any(DataSetFilter.class));
    }

    @Test
    public void testSwitchOnFilter() {
        DisplayerSettings empty = DisplayerSettingsFactory.newMetricSettings()
                .dataset(EXPENSES)
                .filter(COLUMN_ID, FilterFactory.isNull())
                .column(COLUMN_AMOUNT)
                .filterOn(false, true, true)
                .buildSettings();

        MetricDisplayer presenter = createMetricDisplayer(empty);
        MetricDisplayer.View view = presenter.getView();
        presenter.draw();

        reset(view);
        reset(listener);
        presenter.filterApply();

        verify(view).setFilterActive(true);
        verify(listener).onFilterEnabled(eq(presenter), any(DataSetFilter.class));
    }

    @Test
    public void testSwitchOffFilter() {
        DisplayerSettings empty = DisplayerSettingsFactory.newMetricSettings()
                .dataset(EXPENSES)
                .filter(COLUMN_ID, FilterFactory.isNull())
                .column(COLUMN_AMOUNT)
                .filterOn(false, true, true)
                .buildSettings();

        MetricDisplayer presenter = createMetricDisplayer(empty);
        MetricDisplayer.View view = presenter.getView();
        presenter.draw();
        presenter.filterApply();

        reset(view);
        reset(listener);
        presenter.filterReset();

        verify(view).setFilterActive(false);
        verify(listener).onFilterReset(eq(presenter), any(DataSetFilter.class));
    }
}