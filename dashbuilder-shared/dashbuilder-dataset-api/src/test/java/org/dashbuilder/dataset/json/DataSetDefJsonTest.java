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
package org.dashbuilder.dataset.json;

import org.apache.commons.io.IOUtils;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.def.BeanDataSetDef;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DataSetDefJsonTest {

    private static final String UTF_8 = "UTF-8";
    private static final String BEAN_DEF_PATH = "beanDataSetDef.dset";
    private static final String FILTER_DEF_PATH = "dataSetDefFilter.dset";
    private static final String EXPENSES_DEF_PATH = "expenseReports.dset";
    private static final String CSV_DEF_PATH = "csvDataSetDef.dset";

    DataSetDefJSONMarshaller jsonMarshaller = DataSetDefJSONMarshaller.get();

    @Test
    public void testBean() throws Exception {
        final BeanDataSetDef dataSetDef = new BeanDataSetDef();
        dataSetDef.setName("bean data set name");
        dataSetDef.setUUID("bean-test-uuid");
        dataSetDef.setProvider(DataSetProviderType.BEAN);
        dataSetDef.setCacheEnabled(false);
        dataSetDef.setCacheMaxRows(100);
        dataSetDef.setPublic(true);
        dataSetDef.setPushEnabled(false);
        dataSetDef.setPushMaxSize(10);
        dataSetDef.setRefreshAlways(false);
        dataSetDef.setRefreshTime("1second");
        dataSetDef.setGeneratorClass("org.dashbuilder.DataSetGenerator");
        final Map<String, String> parameterMap = new LinkedHashMap<String, String>();
        parameterMap.put("p1", "v1");
        parameterMap.put("p2", "v2");
        dataSetDef.setParamaterMap(parameterMap);
        
        String json = jsonMarshaller.toJsonString(dataSetDef);
        String beanJSONContent = getFileAsString(BEAN_DEF_PATH);

        assertDataSetDef(json, beanJSONContent);
    }
    
    @Test
    public void testCSV() throws Exception {
        try {
            String json = getFileAsString(CSV_DEF_PATH);
            CSVDataSetDef def = (CSVDataSetDef) jsonMarshaller.fromJson(json);
            assertEquals(def.getColumns().size(), 5);

            DataColumnDef column1 = def.getColumnById("office");
            DataColumnDef column2 = def.getColumnById("department");
            DataColumnDef column3 = def.getColumnById("employee");
            DataColumnDef column4 = def.getColumnById("amount");
            DataColumnDef column5 = def.getColumnById("date");
            assertNotNull(column1);
            assertNotNull(column2);
            assertNotNull(column3);
            assertNotNull(column4);
            assertNotNull(column5);
            assertEquals(column1.getColumnType(), ColumnType.LABEL);
            assertEquals(column2.getColumnType(), ColumnType.LABEL);
            assertEquals(column3.getColumnType(), ColumnType.LABEL);
            assertEquals(column4.getColumnType(), ColumnType.NUMBER);
            assertEquals(column5.getColumnType(), ColumnType.DATE);

            assertEquals(def.getFilePath(), "expenseReports.csv");
            assertNull(def.getFileURL());
            assertEquals(def.getDatePattern(), "MM-dd-yyyy");
            assertEquals(def.getNumberPattern(), "#,###.##");
            assertEquals(def.getDatePattern("date"), "MM-dd-yyyy");
            assertEquals(def.getNumberPattern("amount"), "#,###.##");
            assertEquals(def.getSeparatorChar(), Character.valueOf(';'));
            assertEquals(def.getQuoteChar(), Character.valueOf('\"'));
        }
        catch (ClassCastException e) {
            fail("Not a CSV dataset def");
        }
    }

    @Test
    public void testColumns() throws Exception {
        String json = getFileAsString(EXPENSES_DEF_PATH);
        DataSetDef def = jsonMarshaller.fromJson(json);
        assertEquals(def.getColumns().size(), 6);

        DataColumnDef column1 = def.getColumnById("EXPENSES_ID");
        DataColumnDef column2 = def.getColumnById("DEPARTMENT");
        DataColumnDef column3 = def.getColumnById("AMOUNT");
        DataColumnDef column4 = def.getColumnById("CREATION_DATE");
        DataColumnDef column5 = def.getColumnById("EMPLOYEE");
        DataColumnDef column6 = def.getColumnById("CITY");
        assertNotNull(column1);
        assertNotNull(column2);
        assertNotNull(column3);
        assertNotNull(column4);
        assertNotNull(column5);
        assertNotNull(column6);
        assertEquals(column1.getColumnType(), ColumnType.NUMBER);
        assertEquals(column2.getColumnType(), ColumnType.LABEL);
        assertEquals(column3.getColumnType(), ColumnType.NUMBER);
        assertEquals(column4.getColumnType(), ColumnType.DATE);
        assertEquals(column5.getColumnType(), ColumnType.LABEL);
        assertEquals(column6.getColumnType(), ColumnType.LABEL);
    }

    @Test
    public void testFilters() throws Exception {
        final BeanDataSetDef dataSetDef = new BeanDataSetDef();
        dataSetDef.setName("filter data set name");
        dataSetDef.setUUID("filter-test-uuid");
        dataSetDef.setProvider(DataSetProviderType.BEAN);
        dataSetDef.setCacheEnabled(false);
        dataSetDef.setCacheMaxRows(100);
        dataSetDef.setPublic(true);
        dataSetDef.setPushEnabled(false);
        dataSetDef.setPushMaxSize(10);
        dataSetDef.setRefreshAlways(false);
        dataSetDef.setRefreshTime("1second");
        dataSetDef.setGeneratorClass("org.dashbuilder.dataprovider.SalesPerYearDataSetGenerator");
        final Map<String, String> parameterMap = new LinkedHashMap<String, String>();
        parameterMap.put("multiplier", "1");
        dataSetDef.setParamaterMap(parameterMap);
        final DataSetFilter filter = new DataSetFilter();
        final List<Comparable> params1 = new ArrayList<Comparable>();
        params1.add("JANUARY");
        ColumnFilter columnFilter = new CoreFunctionFilter("month", CoreFunctionType.EQUALS_TO, params1);
        filter.addFilterColumn(columnFilter);
        final List<Comparable> params2 = new ArrayList<Comparable>();
        params2.add(0d);
        params2.add(100.35d);
        columnFilter = new CoreFunctionFilter("amount", CoreFunctionType.BETWEEN, params2);
        filter.addFilterColumn(columnFilter);
        dataSetDef.setDataSetFilter(filter);
        
        String json = jsonMarshaller.toJsonString(dataSetDef);
        String filteredDataSetDefJSONContent = getFileAsString(FILTER_DEF_PATH);

        assertDataSetDef(json, filteredDataSetDefJSONContent);
    }

    private void assertDataSetDef(final String def1, final String def2) throws Exception {
        if (def1 == null && def2 != null) Assert.assertTrue("JSON string for Def1 is null and for Def2 is not null", false);
        if (def1 != null && def2 == null) Assert.assertTrue("JSON string for Def1 is not null and for Def2 is null", false);
        if (def1 == null) Assert.assertTrue("JSON string for both definitions is null", false);

        DataSetDef def1Object = jsonMarshaller.fromJson(def1);
        DataSetDef def2Object = jsonMarshaller.fromJson(def2);

        Assert.assertEquals(def1Object, def2Object);
    }

    protected static String getFileAsString(String file) throws Exception {
        InputStream mappingsFileUrl = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        StringWriter writer = null;
        String fileContent = null;

        try {
            writer = new StringWriter();
            IOUtils.copy(mappingsFileUrl, writer, UTF_8);
            fileContent = writer.toString();
        } finally {
            if (writer != null) writer.close();
        }

        // Ensure newline characters meet the HTTP specification formatting requirements.
        return fileContent.replaceAll("\n","\r\n");
    }
}
