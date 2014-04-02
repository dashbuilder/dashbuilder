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
package org.dashbuilder.dataset.sales;

import java.util.Calendar;
import javax.inject.Inject;

import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.*;

@RunWith(Arquillian.class)
public class SalesDataSetGeneratorTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private SalesDataSetGenerator dataSetGenerator;

    @Test
    public void generateDataSet() {
        int perMonth = 30;
        int startYear = Calendar.getInstance().get(Calendar.YEAR) - 2;
        int endYear = Calendar.getInstance().get(Calendar.YEAR) + 2;
        DataSet dataSet = dataSetGenerator.generateDataSet("test", dataSetGenerator.randomOpportunities(perMonth, startYear, endYear));
        assertThat(dataSet.getRowCount()).isGreaterThan(0);
    }

}

