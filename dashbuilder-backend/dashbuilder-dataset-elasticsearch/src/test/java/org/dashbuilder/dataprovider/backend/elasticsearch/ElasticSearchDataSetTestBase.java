/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.apache.commons.io.IOUtils;
import org.dashbuilder.DataSetCore;
import org.dashbuilder.dataprovider.DataSetProviderRegistry;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFormatter;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.json.DataSetDefJSONMarshaller;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * <p>Base test for ElasticSearch providers and data sets.</p>
 *
 * @since 0.3.0
 */
public class ElasticSearchDataSetTestBase {

    static final Logger logger =
            LoggerFactory.getLogger(ElasticSearchDataSetTestBase.class);

    // Config files & example data for running EL server.
    public static final String EL_EXAMPLE_INDEX = "expensereports";
    public static final String EL_EXAMPLE_TYPE = "expense";
    public static final String EL_EXAMPLE_DEPT_ENGINEERING = "Engineering";
    public static final String EL_EXAMPLE_DEPT_SERVICES = "Services";
    public static final String EL_EXAMPLE_DEPT_MANAGEMENT = "Management";
    public static final String EL_EXAMPLE_DEPT_SALES = "Sales";
    public static final String EL_EXAMPLE_DEPT_SUPPORT = "Support";
    public static final String EL_EXAMPLE_CITY_BARCELONA = "Barcelona";
    public static final String EL_EXAMPLE_CITY_MADRID = "Madrid";
    public static final String EL_EXAMPLE_CITY_RALEIGH = "Raleigh";
    public static final String EL_EXAMPLE_CITY_LONDON = "London";
    public static final String EL_EXAMPLE_EMP_ROXIE = "Roxie Foraker";
    public static final String EL_EXAMPLE_EMP_JAMIE = "Jamie Gilbeau";
    public static final String EL_EXAMPLE_EMP_NITA = "Nita Marling";
    public static final String EL_EXAMPLE_EMP_HANNA = "Hannah B. Mackey";
    public static final String EL_EXAMPLE_EMP_PATRICIA = "Patricia J. Behr";

    protected DataSetManager dataSetManager;
    protected DataSetDefRegistry dataSetDefRegistry;
    protected DataSetDefJSONMarshaller jsonMarshaller;
    protected DataSetFormatter dataSetFormatter;

    @Before
    public void setUp() throws Exception {
        dataSetManager = DataSetCore.get().getDataSetManager();
        dataSetDefRegistry = DataSetCore.get().getDataSetDefRegistry();
        jsonMarshaller = DataSetCore.get().getDataSetDefJSONMarshaller();
        dataSetFormatter = new DataSetFormatter();

        DataSetProviderRegistry dataSetProviderRegistry = DataSetCore.get().getDataSetProviderRegistry();
        dataSetProviderRegistry.registerDataProvider(ElasticSearchDataSetProvider.get());
    }

    /**
     * Registers a dataset given into the <code>resource</code> definition.
     *
     * @param resource The dataset definition resource.
     */
    protected ElasticSearchDataSetDef _registerDataSet(String resource) throws Exception {
        // Register the SQL data set
        URL fileURL = Thread.currentThread().getContextClassLoader().getResource(resource);
        String json = IOUtils.toString(fileURL);
        ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);
        return def;
    }

    protected static void log(Object message) {
        System.out.print(message);
        if (logger.isDebugEnabled()) {
            logger.debug(message.toString());
        }
    }

    /**
     * Helper method to print to standard output the data set values.
     */
    protected void printDataSet(DataSet dataSet) {
        log(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}