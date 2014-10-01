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
package org.dashbuilder.dataprovider.backend.csv;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import javax.inject.Inject;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetFormatter;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dashbuilder.dataset.Assertions.*;
import static org.fest.assertions.api.Assertions.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;

@RunWith(Arquillian.class)
public class CSVDataSetBasicTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static final String EXPENSE_REPORTS = "dataset_expense_reports";

    @Inject
    DataSetManager dataSetManager;

    @Inject
    protected DataSetDefRegistry dataSetDefRegistry;

    @Inject
    DataSetFormatter dataSetFormatter;

    @Before
    public void setUp() throws Exception {
        URL fileURl = Thread.currentThread().getContextClassLoader().getResource("expenseReports.csv");
        dataSetDefRegistry.registerDataSetDef(
                DataSetFactory.newCSVDataSetDef()
                .uuid(EXPENSE_REPORTS)
                .fileURL(fileURl.toString())
                .label("id")
                .date("date", "MM-dd-yyyy")
                .number("amount", "#,###.##")
                .datePattern("MM-dd-yyyy")
                .separatorChar(';')
                .quoteChar('\"')
                .escapeChar('\\')
                .separatorChar(';')
                .buildDef());
    }

    @Test
    public void testLoadDataSet() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(50);
        assertThat(result.getColumns().size()).isEqualTo(6);
    }

    @Test
    public void testLookupDataSet() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter("amount", isLowerThan(1000))
                        .group("department")
                        .count("#items")
                        .sum("amount")
                        .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Engineering", "16.00", "6,547.56"},
                {"Services", "5.00", "2,504.50"},
                {"Sales", "8.00", "3,213.53"},
                {"Support", "6.00", "2,343.70"},
                {"Management", "11.00", "6,017.47"}
        }, 0);
    }

    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
