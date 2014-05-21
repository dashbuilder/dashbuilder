/**
 * Copyright (C) 2012 JBoss Inc
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

import org.dashbuilder.dataset.function.AggregateFunction;
import org.dashbuilder.dataset.function.AggregateFunctionManager;
import org.dashbuilder.dataset.function.AverageFunction;
import org.dashbuilder.dataset.function.CountFunction;
import org.dashbuilder.dataset.function.DistinctFunction;
import org.dashbuilder.dataset.function.MaxFunction;
import org.dashbuilder.dataset.function.MinFunction;
import org.dashbuilder.dataset.function.SumFunction;
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
        AggregateFunction sf = aggregateFunctionManager.getFunctionByCode(SumFunction.CODE);
        double result = sf.aggregate(listOfNumbers);
        assertThat(result).isEqualTo(15);
    }

    @Test
    public void testAvgFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByCode(AverageFunction.CODE);
        double result = sf.aggregate(listOfNumbers);
        assertThat(result).isEqualTo(3);
    }

    @Test
    public void testMaxFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByCode(MaxFunction.CODE);
        double result = sf.aggregate(listOfNumbers);
        assertThat(result).isEqualTo(5);
    }

    @Test
    public void testMinFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByCode(MinFunction.CODE);
        double result = sf.aggregate(listOfNumbers);
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testCountFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByCode(CountFunction.CODE);
        double result = sf.aggregate(listOfStrings);
        assertThat(result).isEqualTo(5);
    }

    @Test
    public void testDistinctFunction() throws Exception {
        AggregateFunction sf = aggregateFunctionManager.getFunctionByCode(DistinctFunction.CODE);
        double result = sf.aggregate(listOfStrings);
        assertThat(result).isEqualTo(3);
    }
}
