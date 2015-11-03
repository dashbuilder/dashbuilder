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
package org.dashbuilder.renderer.client.selector;

import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.AbstractDisplayerTest;
import org.dashbuilder.displayer.client.DataSetHandlerImpl;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.dashbuilder.dataset.ExpenseReportsData.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SelectorDisplayerTest extends AbstractDisplayerTest {

    public SelectorDisplayer createSelectorDisplayer(DisplayerSettings settings) {
        return initDisplayer(new SelectorDisplayer(mock(SelectorDisplayer.View.class)), settings);
    }

    @Test
    public void testDraw() {
        DisplayerSettings departmentList = DisplayerSettingsFactory.newSelectorSettings()
                .dataset(EXPENSES)
                .group(COLUMN_DEPARTMENT)
                .column(COLUMN_DEPARTMENT)
                .column(COLUMN_ID, AggregateFunctionType.COUNT)
                .filterOn(false, true, false)
                .buildSettings();

        SelectorDisplayer presenter = createSelectorDisplayer(departmentList);
        SelectorDisplayer.View view = presenter.getView();
        presenter.draw();

        verify(view).setFilterEnabled(true);
        verify(view).clearItems();
        verify(view).showSelectHint(COLUMN_DEPARTMENT);
        verify(view, times(5)).addItem(anyString(), anyString());
        verify(view, never()).setSelectedIndex(anyInt());
        verify(view, never()).showResetHint(anyString());
    }

    @Test
    public void testNoData() {
        DisplayerSettings departmentList = DisplayerSettingsFactory.newSelectorSettings()
                .dataset(EXPENSES)
                .filter(COLUMN_ID, FilterFactory.isNull())
                .group(COLUMN_DEPARTMENT)
                .column(COLUMN_DEPARTMENT)
                .column(COLUMN_ID, AggregateFunctionType.COUNT)
                .buildSettings();

        SelectorDisplayer presenter = createSelectorDisplayer(departmentList);
        SelectorDisplayer.View view = presenter.getView();
        presenter.draw();

        verify(view).clearItems();
        verify(view).showSelectHint(COLUMN_DEPARTMENT);
        verify(view, never()).addItem(anyString(), anyString());
        verify(view, never()).setSelectedIndex(anyInt());
    }

    @Test
    public void testSelectDisabled() {
        DisplayerSettings departmentList = DisplayerSettingsFactory.newSelectorSettings()
                .dataset(EXPENSES)
                .group(COLUMN_DEPARTMENT)
                .column(COLUMN_DEPARTMENT)
                .column(COLUMN_ID, AggregateFunctionType.COUNT)
                .filterOff(true)
                .buildSettings();

        SelectorDisplayer presenter = createSelectorDisplayer(departmentList);
        DisplayerListener listener = mock(DisplayerListener.class);
        SelectorDisplayer.View view = presenter.getView();
        presenter.draw();

        reset(view);
        when(view.getSelectedIndex()).thenReturn(1);
        presenter.addListener(listener);
        presenter.onItemSelected();

        // Check filter notifications
        verify(listener, never()).onFilterEnabled(eq(presenter), any(DataSetGroup.class));
        verify(listener, never()).onRedraw(presenter);

        // Ensure data does not change
        verify(view).showResetHint(COLUMN_DEPARTMENT);
        verify(view, never()).clearItems();
        verify(view, never()).showSelectHint(COLUMN_DEPARTMENT);
        verify(view, never()).addItem(anyString(), anyString());
        verify(view, never()).setSelectedIndex(anyInt());
    }

    @Test
    public void testSelectItem() {
        DisplayerSettings departmentList = DisplayerSettingsFactory.newSelectorSettings()
                .dataset(EXPENSES)
                .group(COLUMN_DEPARTMENT)
                .column(COLUMN_DEPARTMENT)
                .column(COLUMN_ID, AggregateFunctionType.COUNT)
                .filterOn(false, true, true)
                .buildSettings();

        SelectorDisplayer presenter = createSelectorDisplayer(departmentList);
        SelectorDisplayer.View view = presenter.getView();
        DisplayerListener listener = mock(DisplayerListener.class);
        presenter.draw();

        reset(view);
        when(view.getSelectedIndex()).thenReturn(1);
        presenter.addListener(listener);
        presenter.onItemSelected();

        verify(view).showResetHint(COLUMN_DEPARTMENT);
        verify(view, never()).showSelectHint(COLUMN_DEPARTMENT);

        // Check filter notifications
        verify(listener).onFilterEnabled(eq(presenter), any(DataSetGroup.class));
        verify(listener, never()).onRedraw(presenter);

        // Ensure data does not change
        verify(view, never()).clearItems();
        verify(view, never()).addItem(anyString(), anyString());
        verify(view, never()).setSelectedIndex(anyInt());
    }

    @Test
    public void testDrillDown() {
        DisplayerSettings departmentList = DisplayerSettingsFactory.newSelectorSettings()
                .dataset(EXPENSES)
                .group(COLUMN_DEPARTMENT)
                .column(COLUMN_DEPARTMENT)
                .column(COLUMN_ID, AggregateFunctionType.COUNT)
                .filterOn(true, true, true)
                .buildSettings();

        SelectorDisplayer presenter = createSelectorDisplayer(departmentList);
        SelectorDisplayer.View view = presenter.getView();
        DisplayerListener listener = mock(DisplayerListener.class);
        presenter.draw();

        reset(view);
        when(view.getSelectedIndex()).thenReturn(1);
        presenter.addListener(listener);
        presenter.onItemSelected();

        // Check filter notifications
        verify(listener).onFilterEnabled(eq(presenter), any(DataSetGroup.class));
        verify(listener).onRedraw(presenter);

        // Check selector refreshes
        verify(view).clearItems();
        verify(view, atLeastOnce()).showResetHint(COLUMN_DEPARTMENT);
        verify(view, never()).showSelectHint(COLUMN_DEPARTMENT);
        verify(view, times(1)).addItem(anyString(), anyString());
    }
}