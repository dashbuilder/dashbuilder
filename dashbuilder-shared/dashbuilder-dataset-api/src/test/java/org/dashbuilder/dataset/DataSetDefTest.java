/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataset;

import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataSetDefTest {

    DataSetDef def1 = DataSetDefFactory.newBeanDataSetDef()
            .uuid("uuid")
            .name("bean")
            .refreshOn("100s", true)
            .pushOn(100)
            .cacheOn(100)
            .generatorClass("class1")
            .generatorParam("p1", "v1")
            .buildDef();

    DataSetDef def2 = DataSetDefFactory.newBeanDataSetDef()
            .uuid("uuid")
            .name("bean")
            .refreshOn("100s", true)
            .pushOn(100)
            .cacheOn(100)
            .generatorClass("class1")
            .generatorParam("p1", "v1")
            .buildDef();

    @Test
    public void testEquals() throws Exception {
        assertTrue(def1.equals(def2));
    }
}
