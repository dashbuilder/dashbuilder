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

import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.export.ExportCallback;
import org.dashbuilder.displayer.client.export.ExportFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.dashbuilder.dataset.ExpenseReportsData.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DisplayerExportTest extends AbstractDisplayerTest {

    DisplayerSettings EMPTY = DisplayerSettingsFactory.newTableSettings()
            .dataset(EXPENSES)
            .filter(COLUMN_CITY, FilterFactory.isNull())
            .buildSettings();

    DisplayerSettings ALLROWS = DisplayerSettingsFactory.newTableSettings()
            .dataset(EXPENSES)
            .buildSettings();

    @Mock
    ExportCallback exportCallback;

    @Test
    public void testExportDisplayer() {
        Displayer displayer = displayerLocator.lookupDisplayer(ALLROWS);
        displayer.draw();
        displayer.export(ExportFormat.CSV, -1, exportCallback);

        verify(exportCallback).exportFileUrl(anyString());
    }

    @Test
    public void testExportNonDrawnDisplayer() {
        Displayer displayer = displayerLocator.lookupDisplayer(ALLROWS);
        displayer.export(ExportFormat.CSV, -1, exportCallback);

        verify(exportCallback).noData();
    }

    @Test
    public void testExportEmptyDisplayer() {
        Displayer displayer = displayerLocator.lookupDisplayer(EMPTY);
        displayer.draw();
        displayer.export(ExportFormat.CSV, -1, exportCallback);

        verify(exportCallback).noData();
    }

    @Test
    public void testExportLimitExceeded() {
        Displayer displayer = displayerLocator.lookupDisplayer(ALLROWS);
        displayer.draw();
        displayer.export(ExportFormat.CSV, 10, exportCallback);

        verify(exportCallback).tooManyRows(50);
    }

    @Test
    public void testExportLimitRuledOut() {
        Displayer displayer = displayerLocator.lookupDisplayer(ALLROWS);
        displayer.draw();
        displayer.export(ExportFormat.CSV, 0, exportCallback);

        verify(exportCallback, never()).tooManyRows(anyInt());
        verify(exportCallback).exportFileUrl(anyString());
    }
}