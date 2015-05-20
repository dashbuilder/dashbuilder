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
package org.dashbuilder.dataset;

import java.sql.Timestamp;
import java.util.Calendar;
import javax.inject.Inject;

import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dashbuilder.dataset.Assertions.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.fest.assertions.api.Assertions.*;

@RunWith(Arquillian.class)
public class DataSetTrimTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static final String EXPENSE_REPORTS = "expense_reports";

    @Inject
    public DataSetManager dataSetManager;

    @Inject
    public DataSetFormatter dataSetFormatter;

    @Before
    public void setUp() throws Exception {
        DataSet dataSet = RawDataSetSamples.EXPENSE_REPORTS.toDataSet();
        dataSet.setUUID(EXPENSE_REPORTS);
        dataSetManager.registerDataSet(dataSet);
        dataSetFormatter = new DataSetFormatter();
    }

    @Test
    public void testTrim() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .rowNumber(10)
                .buildLookup());

        assertThat(result.getColumns().size()).isEqualTo(6);
        assertThat(result.getRowCount()).isEqualTo(10);
    }

    @Test
    public void testDuplicatedColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .column("city", "city1")
                .column("city", "city2")
                .rowNumber(10)
                .buildLookup());

        assertThat(result.getColumns().size()).isEqualTo(2);
        assertThat(result.getRowCount()).isEqualTo(10);
    }
}
