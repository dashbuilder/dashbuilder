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


import java.util.Date;

import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.json.JsonBoolean;
import org.dashbuilder.json.JsonNull;
import org.dashbuilder.json.JsonNumber;
import org.dashbuilder.json.JsonObject;
import org.dashbuilder.json.JsonString;
import org.dashbuilder.json.JsonValue;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;

public class DataSetLookupJsonTest {

    DataSetLookupJSONMarshaller jsonMarshaller = DataSetLookupJSONMarshaller.get();

    @Test
    public void testDataSetLookupMarshalling() {
        DataSetLookup original = DataSetFactory.newDataSetLookupBuilder()
                .dataset("mydataset")
                .filter(OR(notEqualsTo("department", "IT"), greaterOrEqualsTo("amount", 100d)))
                .filter("department", notEqualsTo("IT"))
                .filter("amount", between(100d, 200d))
                .filter("date", greaterThan(jsonMarshaller.parseDate("2018-01-01 00:00:00")))
                .filter("country", isNull())
                .group("department").select("Services")
                .group("date", "year").dynamic(DateIntervalType.YEAR, true)
                .column("date")
                .column("amount", AggregateFunctionType.SUM, "total")
                .sort("date", SortOrder.ASCENDING)
                .buildLookup();

        JsonObject _jsonObj = jsonMarshaller.toJson(original);
        assertNotNull(_jsonObj.toString());

        DataSetLookup unmarshalled = jsonMarshaller.fromJson(_jsonObj);
        assertEquals(unmarshalled, original);
    }

    @Test
    public void testDateFormat() {
        String d1 = "2020-11-10 23:59:59";
        Date d2 = jsonMarshaller.parseDate(d1);
        String d3 = jsonMarshaller.formatDate(d2);
        assertEquals(d1, d3);

        d1 = "2020-01-01 00:00:00";
        d2 = jsonMarshaller.parseDate(d1);
        d3 = jsonMarshaller.formatDate(d2);
        assertEquals(d1, d3);
    }

    @Test
    public void test_DASHBUILDE_83() {

        JsonValue jsonNull = jsonMarshaller.formatValue(null);
        JsonValue jsonBoolean = jsonMarshaller.formatValue(true);
        JsonValue jsonNumber = jsonMarshaller.formatValue(100d);
        JsonValue jsonDate = jsonMarshaller.formatValue(new Date());
        JsonValue jsonString = jsonMarshaller.formatValue("string");

        assertTrue(jsonNull instanceof JsonNull);
        assertTrue(jsonBoolean instanceof JsonBoolean);
        assertTrue(jsonNumber instanceof JsonNumber);
        assertTrue(jsonDate instanceof JsonString);
        assertTrue(jsonString instanceof JsonString);
    }
}
