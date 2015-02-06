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

import java.net.URL;
import javax.inject.Inject;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetFormatter;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.backend.DataSetDefDeployer;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.*;

@RunWith(Arquillian.class)
public class CSVAutoDeployDataSetTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    DataSetManager dataSetManager;

    @Inject
    DataSetDefDeployer dataSetDefDeployer;

    @Inject
    DataSetFormatter dataSetFormatter;

    @Before
    public void setUp() throws Exception {
        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("expenseReports.dset");
        String deploymentFolder = fileURL.toString().replaceAll("file:", "").replaceAll("expenseReports.dset", "");
        dataSetDefDeployer.deploy(deploymentFolder);
    }

    @Test
    public void testExpenseReportsDataSet() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expenseReports")
                        .buildLookup());

        assertThat(result).isNotNull();
        assertThat(result.getRowCount()).isEqualTo(50);
        assertThat(result.getColumns().size()).isEqualTo(6);
    }

    @Test
    public void testWorldPopulationDataSet() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("worldPopulation")
                        .buildLookup());

        assertThat(result).isNotNull();
        assertThat(result.getRowCount()).isGreaterThan(100);
        assertThat(result.getColumns().size()).isEqualTo(5);
    }
}
