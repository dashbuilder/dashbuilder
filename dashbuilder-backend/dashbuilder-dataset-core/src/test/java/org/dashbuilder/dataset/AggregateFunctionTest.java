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

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import org.dashbuilder.dataset.group.AggregateFunction;
import org.dashbuilder.dataset.group.AggregateFunctionManager;
import org.dashbuilder.dataset.engine.function.AverageFunction;
import org.dashbuilder.dataset.engine.function.CountFunction;
import org.dashbuilder.dataset.engine.function.DistinctFunction;
import org.dashbuilder.dataset.engine.function.MaxFunction;
import org.dashbuilder.dataset.engine.function.MinFunction;
import org.dashbuilder.dataset.engine.function.SumFunction;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class AggregateFunctionTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    AggregateFunctionManager aggregateFunctionManager;

    List listOfNumbers = Arrays.asList(1, 2, 3, 4, 5);
    List listOfStrings = Arrays.asList("A", "B", "C", "A", "B");

    @Test
    public void testSumFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByType(AggregateFunctionType.SUM);
        double result = sf.aggregate(listOfNumbers);
        assertThat(result).isEqualTo(15);
    }

    @Test
    public void testAvgFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByType(AggregateFunctionType.AVERAGE);
        double result = sf.aggregate(listOfNumbers);
        assertThat(result).isEqualTo(3);
    }

    @Test
    public void testMaxFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByType(AggregateFunctionType.MAX);
        double result = sf.aggregate(listOfNumbers);
        assertThat(result).isEqualTo(5);
    }

    @Test
    public void testMinFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByType(AggregateFunctionType.MIN);
        double result = sf.aggregate(listOfNumbers);
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testCountFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByType(AggregateFunctionType.COUNT);
        double result = sf.aggregate(listOfStrings);
        assertThat(result).isEqualTo(5);
    }

    @Test
    public void testDistinctFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByType(AggregateFunctionType.DISTINCT);
        double result = sf.aggregate(listOfStrings);
        assertThat(result).isEqualTo(3);
    }
}
