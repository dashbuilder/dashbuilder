package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.dashbuilder.dataset.DataSetTrimTest;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test case delegates to the common tests from data set core module.
 */
@RunWith(Arquillian.class)
public class ElasticSearchCommonTests extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset";
    
    @Before
    public void setUp() throws Exception {
        // Register the data set definition for expense reports index.
        _registerDataSet(EL_EXAMPLE_DATASET_DEF);
    }
    
    @Test
    public void testTrim() throws Exception {
        DataSetTrimTest subTest = new DataSetTrimTest();
        subTest.dataSetManager = dataSetManager;
        subTest.dataSetFormatter = dataSetFormatter;
        subTest.testTrim();
        subTest.testDuplicatedColumns();
    }
}
