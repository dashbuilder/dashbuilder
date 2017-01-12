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
package org.dashbuilder.displayer.client.widgets;

import java.util.Arrays;
import javax.enterprise.event.Event;

import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.uuid.UUIDGenerator;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsBuilder;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerLocator;
import org.dashbuilder.displayer.client.events.DataSetLookupChangedEvent;
import org.dashbuilder.displayer.client.events.DisplayerEditorClosedEvent;
import org.dashbuilder.displayer.client.events.DisplayerEditorSavedEvent;
import org.dashbuilder.displayer.client.prototypes.DisplayerPrototypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DisplayerEditorTest {

    @Mock
    DisplayerEditor.View view;

    @Mock
    DataSetClientServices clientServices;

    @Mock
    DisplayerLocator displayerLocator;

    @Mock
    DisplayerTypeSelector typeSelector;

    @Mock
    DataSetLookupEditor lookupEditor;

    @Mock
    DisplayerSettingsEditor settingsEditor;

    @Mock
    DisplayerEditorStatus editorStatus;

    @Mock
    Event<DisplayerEditorSavedEvent> saveEvent;

    @Mock
    Event<DisplayerEditorClosedEvent> closeEvent;

    @Mock
    DisplayerSettings displayerSettings;

    @Mock
    DisplayerSettings tableSettings;

    @Mock
    Displayer displayer;

    @Mock
    DisplayerHtmlEditor displayerHtmlEditor;

    @Mock
    Displayer tableDisplayer;

    @Mock
    UUIDGenerator uuidGenerator;

    @Mock
    DisplayerPrototypes displayerPrototypes;

    @Mock
    DisplayerConstraints displayerConstraints;

    @Mock
    DataSetLookupConstraints lookupConstraints;

    DisplayerEditor presenter = null;

    @Before
    public void init() throws Exception {
        when(displayerPrototypes.getProto(any(DisplayerType.class))).thenReturn(displayerSettings);
        when(displayerSettings.cloneInstance()).thenReturn(tableSettings);
        when(displayerLocator.lookupDisplayer(displayerSettings)).thenReturn(displayer);
        when(displayerLocator.lookupDisplayer(tableSettings)).thenReturn(tableDisplayer);
        when(displayer.getDisplayerConstraints()).thenReturn(displayerConstraints);
        when(displayerConstraints.getDataSetLookupConstraints()).thenReturn(lookupConstraints);

        presenter = new DisplayerEditor(view, clientServices, displayerLocator, displayerPrototypes,
                typeSelector, lookupEditor, settingsEditor, editorStatus, displayerHtmlEditor, saveEvent, closeEvent);

    }

    @Test
    public void testBrandNewDisplayer() {
        when(view.getBrandNewDisplayerTitle()).thenReturn("New displayer");
        presenter.init(null);

        assertEquals(presenter.isBrandNewDisplayer(), true);
        verify(displayerSettings).setTitle("New displayer");
        verify(displayerPrototypes).getProto(any(DisplayerType.class));
        verify(displayerLocator).lookupDisplayer(displayerSettings);
        verify(displayer).draw();

        verify(typeSelector).init(any(DisplayerType.class), any(DisplayerSubType.class));
        verify(lookupEditor).init(lookupConstraints, null);
        verify(settingsEditor).init(displayer);

        verify(view).showDisplayer(displayer);
    }

    @Test
    public void testNavigation() {
        presenter.setTypeSelectorEnabled(true);
        presenter.setDataSetLookupConfEnabled(true);
        presenter.setDisplaySettingsEnabled(true);

        // Default
        when(editorStatus.getSelectedOption(anyString())).thenReturn(-1);
        presenter.init(null);
        verify(view).gotoTypeSelection(typeSelector);

        // Type selector
        when(editorStatus.getSelectedOption(anyString())).thenReturn(0);
        presenter.init(null);
        verify(view).gotoTypeSelection(typeSelector);

        // Data lookup conf
        when(editorStatus.getSelectedOption(anyString())).thenReturn(1);
        presenter.init(null);
        verify(view).gotoDataSetLookupConf(lookupEditor);

        // Display settings
        when(editorStatus.getSelectedOption(anyString())).thenReturn(2);
        presenter.init(null);
        verify(view).gotoDisplaySettings(settingsEditor);
    }

    @Test
    public void testTypeSelectorDisabled() {
        presenter.setTypeSelectorEnabled(false);
        presenter.setDataSetLookupConfEnabled(true);
        presenter.setDisplaySettingsEnabled(true);

        when(editorStatus.getSelectedOption(anyString())).thenReturn(-1);
        presenter.init(null);
        verify(view).gotoDataSetLookupConf(lookupEditor);
        verify(view, never()).gotoTypeSelection(typeSelector);
        verify(view, never()).gotoDisplaySettings(settingsEditor);
    }

    @Test
    public void testDataLookupConfDisabled() {
        presenter.setTypeSelectorEnabled(false);
        presenter.setDataSetLookupConfEnabled(false);
        presenter.setDisplaySettingsEnabled(true);

        when(editorStatus.getSelectedOption(anyString())).thenReturn(-1);
        presenter.init(null);
        verify(view, never()).gotoDataSetLookupConf(lookupEditor);
        verify(view, never()).gotoTypeSelection(typeSelector);
        verify(view).gotoDisplaySettings(settingsEditor);
    }

    @Test
    public void testTableModeAvailable() {
        for (DisplayerType type : Arrays.asList(
                DisplayerType.BARCHART,
                DisplayerType.LINECHART,
                DisplayerType.PIECHART,
                DisplayerType.AREACHART,
                DisplayerType.BUBBLECHART,
                DisplayerType.METERCHART,
                DisplayerType.METRIC,
                DisplayerType.MAP)) {

            reset(view);
            when(displayerSettings.getType()).thenReturn(type);
            presenter.init(null);
            presenter.gotoDataSetLookupConf();

            verify(view).setTableDisplayModeEnabled(true);
        }
    }

    @Test
    public void testTableModeNotAvailable() {
        when(displayerSettings.getType()).thenReturn(DisplayerType.TABLE);
        presenter.init(null);
        presenter.gotoDataSetLookupConf();

        verify(view).setTableDisplayModeEnabled(false);
        verify(view, never()).setTableDisplayModeEnabled(true);
    }

    @Test
    public void testTableMode() {
        when(view.isTableDisplayModeOn()).thenReturn(true);
        presenter.init(null);

        verify(tableDisplayer).draw();
        verify(view).showDisplayer(tableDisplayer);
    }

    @Test
    public void testDataLookupChanged() {
        DisplayerSettings settings1 = DisplayerSettingsFactory.newPieChartSettings()
                .uuid("test1")
                .dataset("test")
                .group("employee")
                .column("employee").format("Employee")
                .column(AggregateFunctionType.COUNT, "#items").format("#Items")
                .buildSettings();

        DisplayerSettings settings2 = DisplayerSettingsFactory.newPieChartSettings()
                .uuid("test2")
                .dataset("test")
                .group("department")
                .column("department").format("Department")
                .column("amount", AggregateFunctionType.SUM).format("Total amount")
                .buildSettings();

        when(displayerLocator.lookupDisplayer(any())).thenReturn(displayer);
        presenter.init(settings1);
        assertEquals(presenter.getDisplayerSettings().getColumnSettingsList().size(), 2);
        reset(settingsEditor);

        presenter.onDataSetLookupChanged(new DataSetLookupChangedEvent(settings2.getDataSetLookup()));
        verify(settingsEditor).init(any());
        assertEquals(presenter.getDisplayerSettings().getColumnSettingsList().size(), 0);
    }
}