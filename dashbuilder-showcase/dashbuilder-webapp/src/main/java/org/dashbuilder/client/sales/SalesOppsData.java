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
package org.dashbuilder.client.sales;

import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetRef;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.date.DayOfWeek;

import static org.dashbuilder.client.sales.SalesConstants.*;

/**
 * The set of DataSetRef defined on top of the Sales Opportunities data set.
 */
public class SalesOppsData {

    public static final DataSetRef DATA_ALL_OPPS = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .buildLookup();

    public static final DataSetRef GROUP_PIPELINE = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(PIPELINE)
            .count("occurrences")
            .buildLookup();

    public static final DataSetRef GROUP_STATUS = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(STATUS)
            .sum(AMOUNT)
            .buildLookup();

    public static final DataSetRef GROUP_CLOSING_DATE = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(CLOSING_DATE, 24, DateIntervalType.MONTH)
            .sum(EXPECTED_AMOUNT)
            .buildLookup();

    public static final DataSetRef GROUP_SALES_PERSON = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(SALES_PERSON)
            .sum(AMOUNT)
            .buildLookup();

    public static final DataSetRef GROUP_PRODUCT = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(PRODUCT)
            .sum(AMOUNT)
            .buildLookup();

    public static final DataSetRef GROUP_COUNTRY = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(COUNTRY)
            .count("opps")
            .min(AMOUNT, "min")
            .max(AMOUNT, "max")
            .avg(AMOUNT, "avg")
            .sum(AMOUNT, "total")
            .buildLookup();

}
