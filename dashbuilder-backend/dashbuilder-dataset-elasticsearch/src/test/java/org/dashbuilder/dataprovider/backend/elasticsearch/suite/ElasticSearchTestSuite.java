package org.dashbuilder.dataprovider.backend.elasticsearch.suite;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dashbuilder.dataprovider.backend.elasticsearch.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.NativeClientFactory;
import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Test Suite for integration with an Elastic Search instance that is operated by this test suite and suite classes.
 * - It starts and stops an ELS instance at local port tcp 9200. Ensure that it's available at localhost.
 * - It populates with default "expensereports" index documents the local ELS instance. @See ElasticSearchDataSetTestBase.class
 *
 *
 * <p>This test suite does:</p>
 * <ul>
 *     <li>Creates a temporary home folder for an ElasticSearch server with required configuration files</li>
 *     <li>Runs an elastic search server instance. The client node is available by default at <code>localhost:9300</code> and working at the temporary home folder</li>
 *     <li>Creates a default example <code>shakespeare</code> index and mappings for it</li>
 *     <li>Populates the <code>shakespeare</code> index with some documents</li>
 *     <li>At this point, inherited test classes can perform the requests to the EL server.</li>
 *     <li>Finally, stops the EL server and deletes the temporary home folder.</li>
 * </ul>
 *
 * <p>The example used consist of the creation of the index <code>expensereports</code></p>
 * <p>By default this index wil be available, using REST services, at <code>http://localhost:9200/expensereports</code></p>
 *
 * <p>Columns for index <code>expensereports</code>:</p>
 * <ul>
 *     <li><code>id</code> - integer</li>
 *     <li><code>city</code> - string</li>
 *     <li><code>department</code> - string</li>
 *     <li><code>employee</code> - string</li>
 *     <li><code>date</code> - date</li>
 *     <li><code>amount</code> - float</li>
 * </ul>
 *
 * <p>All indexed documents will have a document type value as <code>expense</code></p>
 *
 * <p>Another index named <code>expensereports-sensitive</code> can be created and populated too, with same fields and data as
 * the <code>expensereports</code> one but in this index, the field <code>employee</code> is analyzed with a custom tokenizer analyzer to 
 * provide filtering with case sensitive features.</p>
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
public class ElasticSearchTestSuite {

        @ClassRule
        public static TemporaryFolder elHomeFolder = new TemporaryFolder();

        static final Logger logger =
                LoggerFactory.getLogger(ElasticSearchTestSuite.class);

        // System properties for EL server.
        protected static final String EL_PROPERTY_ELASTICSEARCH = "elasticsearch";

        protected static final String EL_PROPERTY_HOME = "path.home";
        protected static final String EL_PROPERTY_SHARDS = "index.number_of_shards";
        protected static final String EL_PROPERTY_REPLICAS = "index.number_of_replicas";
        protected static final String EL_PROPERTY_FOREGROUND = "foreground";
        protected static final String EL_PROPERTY_SCRIPT_INLINE = "script.inline";
        protected static final String EL_PROPERTY_SCRIPT_INDEXED = "script.indexed";

        // Config files & example data for running EL server.
        protected static final String EL_EXAMPLE_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-mappings.json";
        protected static final String EL_EXAMPLE_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-data.json";
        protected static final String EL_EXAMPLE_CSENSITIVE_INDEX = "expensereports-sensitive";
        protected static final String EL_EXAMPLE_CSENSITIVE_TYPE = "expense";
        protected static final String EL_EXAMPLE_CSENSITIVE_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-csensitive-mappings.json";
        protected static final String EL_EXAMPLE_CSENSITIVE_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-csensitive-data.json";
        protected static final String EL_EXAMPLE_EMPTYINTERVALS_INDEX = "emptyintervals";
        protected static final String EL_EXAMPLE_EMPTYINTERVALS_TYPE = "emptyinterval";
        protected static final String EL_EXAMPLE_EMPTYINTERVALS_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/emptyIntervals-mappings.json";
        protected static final String EL_EXAMPLE_EMPTYINTERVALS_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/emptyIntervals-data.json";
        protected static final String EL_EXAMPLE_MULTIFIELDS_INDEX = "multifields";
        protected static final String EL_EXAMPLE_MULTIFIELDS_TYPE = "multifield";
        protected static final String EL_EXAMPLE_MULTIFIELDS_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/multifields-mappings.json";
        protected static final String EL_EXAMPLE_MULTIFIELDS_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/multifields-data.json";

        protected static final String ENCODING = "UTF-8";

        private static Node elasticSearchNode = null;
        private static Client client = null;

        @BeforeClass
        public static void setUpClass() {
                try {
                        runELServer(elHomeFolder);
                        createAndPopulateExpenseReportsIndex();
                        createAndPopulateExpenseReportsCSensitiveIndex();
                        createAndPopulateEmptyIntervalsIndex();
                        createAndPopulateMultiFieldsIndex();
                } catch (Exception e) {
                        logger.error("Error starting up the ELS instance.", e);
                }

        }

        @AfterClass
        public static void tearDownClass() {
                try {
                        stopELServer(elHomeFolder);
                } catch (Exception e) {
                        logger.error("Error stopping the ELS instance.", e);
                }
        }

        // Not necessary use of @BeforeClass - @see ElasticSearchTestSuite.java.
        public static void runELServer(TemporaryFolder elHomeFolder) throws Exception {
                // Build a temporary EL home folder. Copy config files to it.
                File elHome = elHomeFolder.newFolder("dashbuilder-elasticsearch");

                elasticSearchNode = NodeBuilder
                        .nodeBuilder()
                        .local(true)
                        .clusterName(EL_PROPERTY_ELASTICSEARCH)
                        .settings(
                                Settings.settingsBuilder()
                                        .put( EL_PROPERTY_SHARDS, "1" )
                                        .put( EL_PROPERTY_REPLICAS, "0" )
                                        .put( EL_PROPERTY_FOREGROUND, "yes" )
                                        .put( EL_PROPERTY_SCRIPT_INLINE, "on" )
                                        .put( EL_PROPERTY_SCRIPT_INDEXED, "on" )
                                        .put( EL_PROPERTY_HOME, elHome.getAbsolutePath() )
                                //.put("path.data", new File(tempDir, "data").getAbsolutePath())
                                //.put("path.logs", new File(tempDir, "logs").getAbsolutePath())
                                //.put("path.work", new File(tempDir, "work").getAbsolutePath())
                        ).node();
                elasticSearchNode.start();
                client = elasticSearchNode.client();

                // Set the client instance used for running the tests.
                NativeClientFactory.getInstance().setTestClient( client );
        }

        public static void createAndPopulateExpenseReportsIndex() throws Exception{

                // Create the expensereports example index.
                createIndexELServer( ElasticSearchDataSetTestBase.EL_EXAMPLE_INDEX,
                        EL_EXAMPLE_MAPPINGS);

                // Populate the server with some test content.
                populateELServer( ElasticSearchDataSetTestBase.EL_EXAMPLE_INDEX,
                        ElasticSearchDataSetTestBase.EL_EXAMPLE_TYPE,
                        EL_EXAMPLE_DATA );

                testDocumentsCount( ElasticSearchDataSetTestBase.EL_EXAMPLE_INDEX,
                        ElasticSearchDataSetTestBase.EL_EXAMPLE_TYPE, 50 );
        }

        public static void createAndPopulateExpenseReportsCSensitiveIndex() throws Exception{

                // Create the expensereports example index.
                createIndexELServer( EL_EXAMPLE_CSENSITIVE_INDEX, EL_EXAMPLE_CSENSITIVE_MAPPINGS);

                // Populate the server with some test content.
                populateELServer( EL_EXAMPLE_CSENSITIVE_INDEX, EL_EXAMPLE_CSENSITIVE_TYPE, EL_EXAMPLE_CSENSITIVE_DATA);

                testDocumentsCount( EL_EXAMPLE_CSENSITIVE_INDEX,
                        EL_EXAMPLE_CSENSITIVE_TYPE, 50 );
        }

        public static void createAndPopulateEmptyIntervalsIndex() throws Exception{

                // Create the expensereports example index.
                createIndexELServer( EL_EXAMPLE_EMPTYINTERVALS_INDEX,
                        EL_EXAMPLE_EMPTYINTERVALS_MAPPINGS);

                // Populate the server with some test content.
                populateELServer( EL_EXAMPLE_EMPTYINTERVALS_INDEX,
                        EL_EXAMPLE_EMPTYINTERVALS_TYPE,
                        EL_EXAMPLE_EMPTYINTERVALS_DATA);

                testDocumentsCount( EL_EXAMPLE_EMPTYINTERVALS_INDEX,
                        EL_EXAMPLE_EMPTYINTERVALS_TYPE, 11 );

        }

        public static void createAndPopulateMultiFieldsIndex() throws Exception{

                // Create the expensereports example index.
                createIndexELServer( EL_EXAMPLE_MULTIFIELDS_INDEX, EL_EXAMPLE_MULTIFIELDS_MAPPINGS);

                // Populate the server with some test content.
                populateELServer( EL_EXAMPLE_MULTIFIELDS_INDEX,
                        EL_EXAMPLE_MULTIFIELDS_TYPE,
                        EL_EXAMPLE_MULTIFIELDS_DATA);

                testDocumentsCount( EL_EXAMPLE_MULTIFIELDS_INDEX,
                        EL_EXAMPLE_MULTIFIELDS_TYPE, 6 );

        }

        public static void createIndexELServer( String index, String jsonMappingsFile ) throws Exception {

                // Obtain data to configure & populate the server.
                String mappingsContent = getFileAsString(jsonMappingsFile);

                CreateIndexRequest indexRequest = new CreateIndexRequestBuilder( client, CreateIndexAction.INSTANCE )
                        .setIndex( index)
                        .setSource( mappingsContent )
                        .request();

                CreateIndexResponse indexResponse = client.admin().indices().create( indexRequest ).actionGet();

                if ( !indexResponse.isAcknowledged() ) {
                        throw new RuntimeException( "Error creating index [" + index + "]" );
                }

        }

        public static void populateELServer( String index, String type, String dataFile ) throws Exception {

                String data = getFileAsString( dataFile );

                BulkRequestBuilder bulkRequestBuilder = client.prepareBulk().setRefresh(true);

                String textStr[] = data.split("\\r\\n|\\n|\\r");

                for ( String line : textStr ) {
                        bulkRequestBuilder.add(client.prepareIndex( index, type ).setSource( line ));
                }

                BulkResponse response = bulkRequestBuilder.execute().actionGet();

                if ( response.hasFailures() ) {
                        throw new RuntimeException( "Error when performing test index's data bulk operation. " +
                                "Index=[" + index + "]" );
                }
        }


        // Not necessary use of @AfterClass - @see ElasticSearchTestSuite.java.
        public static void stopELServer(TemporaryFolder elHomeFolder) throws Exception {

                // Close the client.
                client.close();

                // Stop the EL server.
                elasticSearchNode.close();

                // Delete the working home folder for elasticsearch.
                elHomeFolder.delete();
        }

        public static void testDocumentsCount( String index, String type, long count ) throws Exception {

                SearchRequest request = new SearchRequestBuilder( client, SearchAction.INSTANCE )
                        .setIndices( index )
                        .setTypes( type)
                        .request();

                SearchResponse response = client.search( request ).actionGet();

                long c = response.getHits().totalHits();

                assert count == c;

        }

        protected static String getFileAsString(String file) throws Exception {
                InputStream mappingsFileUrl = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
                StringWriter writer = null;
                String fileContent = null;

                try {
                        writer = new StringWriter();
                        IOUtils.copy(mappingsFileUrl, writer, ENCODING);
                        fileContent = writer.toString();
                } finally {
                        if (writer != null) writer.close();
                }

                // Ensure newline characters meet the HTTP specification formatting requirements.
                return fileContent.replaceAll("\n","\r\n");
        }

        protected static Object[] doGet(String url) throws Exception {
                Object[] response = null;
                if (url == null || url.trim().length() == 0) return response;

                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(url);
                CloseableHttpResponse response1 = httpclient.execute(httpGet);
                try {
                        HttpEntity entity1 = response1.getEntity();
                        String responseBody = responseAsString(response1);
                        int responseStatus = response1.getStatusLine().getStatusCode();
                        response = new Object [] {responseStatus, responseBody};

                        // do something useful with the response body
                        // and ensure it is fully consumed
                        EntityUtils.consume(entity1);
                } finally {
                        response1.close();
                }

                return response;
        }

        protected static String responseAsString(CloseableHttpResponse response) throws IOException {
                return streamAsString(response.getEntity().getContent());
        }

        protected static String streamAsString(InputStream inputStream) throws IOException {
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, ENCODING);
                return  writer.toString();
        }

        protected static void log(Object message) {
                // System.out.print(message);
                if (logger.isDebugEnabled()) {
                        logger.debug(message.toString());
                }
        }

}