/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.screens;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.dataset.ClientDataSetManager;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.date.Month;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnStartup;

@Dependent
@WorkbenchScreen(identifier = "StaticChartScreen")
public class StaticChartScreen {

    public interface View extends IsWidget {
        void onDataReady(DataSet dataSet);
    }

    @Inject
    protected View view;

    @Inject
    protected ClientDataSetManager dataSetManager;

    public static final Object[][] SALES_PER_YEAR = new Object[][] {
        new Object[] {Month.JANUARY, 1000d, 2000d, 3000d},
        new Object[] {Month.FEBRUARY, 1400d, 2300d, 2000d},
        new Object[] {Month.MARCH, 1300d, 2000d, 1400d},
        new Object[] {Month.APRIL, 900d, 2100d, 1500d},
        new Object[] {Month.MAY, 1300d, 2300d, 1600d},
        new Object[] {Month.JUNE, 1010d, 2000d, 1500d},
        new Object[] {Month.JULY, 1050d, 2400d, 3000d},
        new Object[] {Month.AUGUST, 2300d, 2000d, 3200d},
        new Object[] {Month.SEPTEMBER, 1900d, 2700d, 3000d},
        new Object[] {Month.OCTOBER, 1200d, 2200d, 3100d},
        new Object[] {Month.NOVEMBER, 1400d, 2100d, 3100d},
        new Object[] {Month.DECEMBER, 1100d, 2100d, 4200d}
    };

    @OnStartup
    public void init() {
        DataSet dataSet = createDataSet();
        view.onDataReady(dataSet);
    }

    public DataSet createDataSet() {
        DataSet dataSet = dataSetManager.createDataSet();
        dataSet.addColumn("month", ColumnType.LABEL);
        dataSet.addColumn("2012", ColumnType.NUMBER);
        dataSet.addColumn("2013", ColumnType.NUMBER);
        dataSet.addColumn("2014", ColumnType.NUMBER);
        dataSet.setValues(SALES_PER_YEAR);
        return dataSet;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Static Chart";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return view;
    }
}