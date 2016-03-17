/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DataSetDefDeployerCDITest {

    @Spy
    DataSetDefDeployerCDI dataSetDefDeployer;

    @Before
    public void setUp() {
        dataSetDefDeployer.init();
    }

    @Test
    public void testDataSetDirDetected() throws Exception {
        URL testDsetUrl = Thread.currentThread().getContextClassLoader().getResource("WEB-INF/datasets/test.dset");
        if (testDsetUrl != null) {
            File dsetDir = new File(testDsetUrl.getPath()).getParentFile();
            verify(dataSetDefDeployer).deploy(dsetDir.getPath());
        }
        else {
            fail("test.dset not found");
        }
    }
}
