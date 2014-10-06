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
package org.dashbuilder.dataprovider;

import java.net.URL;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetFormatter;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.RawDataSetSamples;
import org.dashbuilder.dataset.backend.DataSetDefJSONMarshaller;
import org.dashbuilder.dataset.def.DataSetDef;
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
import static org.dashbuilder.dataset.sort.SortOrder.*;

@RunWith(Arquillian.class)
public class BeanDataSetGeneratorTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    DataSetDefJSONMarshaller jsonMarshaller;

    @Inject
    DataSetManager dataSetManager;

    @Inject
    DataSetDefRegistry dataSetDefRegistry;

    @Inject
    DataSetFormatter dataSetFormatter;

    @Before
    public void setUp() throws Exception {
        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("salesPerYear.dset");
        String json = IOUtils.toString(fileURL);
        DataSetDef def = jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);
    }

    @Test
    public void testGenerateDataSet() throws Exception {
        DataSet result = dataSetManager.getDataSet("salesPerYear");

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"JANUARY", "1,000.00", "2,000.00", "3,000.00"},
                {"FEBRUARY", "1,400.00", "2,300.00", "2,000.00"},
                {"MARCH", "1,300.00", "2,000.00", "1,400.00"},
                {"APRIL", "900.00", "2,100.00", "1,500.00"},
                {"MAY", "1,300.00", "2,300.00", "1,600.00"},
                {"JUNE", "1,010.00", "2,000.00", "1,500.00"},
                {"JULY", "1,050.00", "2,400.00", "3,000.00"},
                {"AUGUST", "2,300.00", "2,000.00", "3,200.00"},
                {"SEPTEMBER", "1,900.00", "2,700.00", "3,000.00"},
                {"OCTOBER", "1,200.00", "2,200.00", "3,100.00"},
                {"NOVEMBER", "1,400.00", "2,100.00", "3,100.00"},
                {"DECEMBER", "1,100.00", "2,100.00", "4,200.00"}
        }, 0);
    }

    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }

}
