package org.dashbuilder.dataprovider.backend.elasticsearch.suite;

import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchDataSetCustomColumnsTest;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchDataSetDatesTest;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchDataSetTest;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchDataSetTestBase;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.ElasticSearchJestClientTest;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchJSONParserTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Suite for integration with an Elastic Search instance that is operated by this test suite and suite classes.
 * - It starts and stops an ELS instance at local port tcp 9200. Ensure that it's available at localhost.
 * - It populates with default "expensereports" index documents the local ELS instance. @See ElasticSearchDataSetTestBase.class
 * @since 0.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ElasticSearchJestClientTest.class,
        ElasticSearchDataSetCustomColumnsTest.class,
        ElasticSearchDataSetTest.class,
        ElasticSearchDataSetDatesTest.class,
        ElasticSearchJSONParserTest.class
        // ElasticSearchDataSetCacheTest.class
})
public class ElasticSearchTestSuite {

        @ClassRule
        public static TemporaryFolder elHomeFolder = new TemporaryFolder();

        static final Logger logger =
                LoggerFactory.getLogger(ElasticSearchTestSuite.class);
        
        @BeforeClass
        public static void setUpClass() {
                try {
                        ElasticSearchDataSetTestBase.runELServer(elHomeFolder);
                } catch (Exception e) {
                        logger.error("Error starting up the ELS instance.", e);
                }

        }

        @AfterClass
        public static void tearDownClass() {
                try {
                        ElasticSearchDataSetTestBase.stopELServer(elHomeFolder);
                } catch (Exception e) {
                        logger.error("Error stopping the ELS instance.", e);
                }
        }
}
