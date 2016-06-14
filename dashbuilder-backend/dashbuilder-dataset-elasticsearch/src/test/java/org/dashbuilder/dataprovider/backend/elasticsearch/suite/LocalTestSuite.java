package org.dashbuilder.dataprovider.backend.elasticsearch.suite;

import org.dashbuilder.dataprovider.backend.elasticsearch.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Suite for local/development testing. An Elastic Serarch instance is supposed to be running in localhost and:
 * - Listening to port 9200
 * - Populated with default "expensereports" index documents. @See ElasticSearchDataSetTestBase.class
 * 
 * @since 0.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ElasticSearchCommonTests.class,
        ElasticSearchDataSetCustomColumnsTest.class,
        ElasticSearchDataSetTest.class,
        ElasticSearchEmptyIntervalsTest.class,
        ElasticSearchMultiFieldsTest.class,
})
public class LocalTestSuite {

        static final Logger logger =
                LoggerFactory.getLogger(LocalTestSuite.class);
        
        @BeforeClass
        public static void setUpClass() {
                // Elastic Search server instance supposed to be running, populated and listening to local port tcp 9200.
        }

        @AfterClass
        public static void tearDownClass() {
                // Not applicable.
        }
}
