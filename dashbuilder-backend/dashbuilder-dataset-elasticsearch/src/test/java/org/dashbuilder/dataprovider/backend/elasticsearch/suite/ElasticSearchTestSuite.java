package org.dashbuilder.dataprovider.backend.elasticsearch.suite;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dashbuilder.dataprovider.backend.elasticsearch.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.ElasticSearchJestClientTest;
import org.elasticsearch.bootstrap.Elasticsearch;
import org.junit.AfterClass;
import org.junit.Assert;
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
import java.net.URL;

/**
 * Test Suite for integration with an Elastic Search instance that is operated by this test suite and suite classes.
 * - It starts and stops an ELS instance at local port tcp 9200. Ensure that it's available at localhost.
 * - It populates with default "expensereports" index documents the local ELS instance. @See ElasticSearchDataSetTestBase.class
 * 
 * 
 * <p>This test suite does:</p>
 * <ul>
 *     <li>Creates a temporary home folder for an ElasticSearch server with required configuration files</li>
 *     <li>Runs an elastic search server instance, by default at <code>localhost:9200</code> and working at the temporary home folder</li>
 *     <li>Creates a default example <code>shakespeare</code> index and mappings for it</li>
 *     <li>Populates the <code>shakespeare</code> index with some documents</li>
 *     <li>At this point, inherited test classes can perform the requests to the EL server.</li>
 *     <li>Finally, stops the EL server and deletes the temporary home folder.</li>
 * </ul>
 *
 * <p>The example used consist of the creation of the index <code>expensereports</code></p>
 * <p>By default this index wil be available at <code>http://localhost:9200/expensereports</code></p>
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
 * <p>Another index named <code>expensereports-sensitive</code> can be created and populated too, with same fileds and data as
 * the <code>expensereports</code> one but in this index, the field <code>employee</code> is analyzed with a custom tokenizer analyzer to 
 * provide filtering with case sensitive features.</p>
 * 
 * @since 0.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ElasticSearchCommonTests.class,
        ElasticSearchJestClientTest.class,
        ElasticSearchDataSetCustomColumnsTest.class,
        ElasticSearchDataSetTest.class,
        ElasticSearchEmptyIntervalsTest.class,
        ElasticSearchMultiFieldsTest.class,
        ElasticSearchEmptyArgumentsTest.class
})
public class ElasticSearchTestSuite {

        @ClassRule
        public static TemporaryFolder elHomeFolder = new TemporaryFolder();

        static final Logger logger =
                LoggerFactory.getLogger(ElasticSearchTestSuite.class);

        // NOTE: If you change the host or port in config/elasticsearch.yml, you should modify this value.
        public static final String EL_SERVER = "http://localhost:9200/";

        // System properties for EL server.
        protected static final String EL_PROPERTY_ELASTICSEARCH = "elasticsearch";
        protected static final String EL_PROPERTY_HOME = "es.path.home";
        protected static final String EL_PROPERTY_FOREGROUND = "es.foreground";
        protected static final String EL_PROPERTY_SCRIPT_INLINE = "es.script.inline";
        protected static final String EL_PROPERTY_SCRIPT_INDEXED = "es.script.indexed";

        // Config files & example data for running EL server.
        protected static final String EL_CONFIG_DIR = "config";
        protected static final String EL_CONFIG_ELASTICSEARCH = "org/dashbuilder/dataprovider/backend/elasticsearch/server/config/elasticsearch.yml";
        protected static final String EL_CONFIG_LOGGING = "org/dashbuilder/dataprovider/backend/elasticsearch/server/config/logging.yml";
        protected static final String EL_EXAMPLE_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-mappings.json";
        protected static final String EL_EXAMPLE_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-data.json";
        protected static final String EL_EXAMPLE_CSENSITIVE_INDEX = "expensereports-sensitive";
        protected static final String EL_EXAMPLE_CSENSITIVE_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-csensitive-mappings.json";
        protected static final String EL_EXAMPLE_CSENSITIVE_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-csensitive-data.json";
        protected static final String EL_EXAMPLE_EMPTYINTERVALS_INDEX = "emptyintervals";
        protected static final String EL_EXAMPLE_EMPTYINTERVALS_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/emptyIntervals-mappings.json";
        protected static final String EL_EXAMPLE_EMPTYINTERVALS_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/emptyIntervals-data.json";
        protected static final String EL_EXAMPLE_MULTIFIELDS_INDEX = "multifields";
        protected static final String EL_EXAMPLE_MULTIFIELDS_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/multifields-mappings.json";
        protected static final String EL_EXAMPLE_MULTIFIELDS_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/multifields-data.json";

        // EL remote REST API endpoints & other parameters.
        protected static final String EL_REST_BULK = "_bulk";
        protected static final String EL_REST_COUNT = "_count";
        protected static final int EL_REST_RESPONSE_OK = 200;
        protected static final int EL_REST_RESPONSE_CREATED = 201;

        // Other constants.
        protected static final String HEADER_ACCEPT = "Accept";
        protected static final String HEADER_CONTENTTYPE = "content-type";
        protected static final String CONTENTTYPE_JSON = "application/json; charset=utf-8";
        protected static final String ENCODING = "UTF-8";
        protected static final String SYMBOL_SLASH = "/";
        
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
                File elHomeConfig = new File(elHome, EL_CONFIG_DIR);
                URL configFileUrl = Thread.currentThread().getContextClassLoader().getResource(EL_CONFIG_ELASTICSEARCH);
                URL loggingFileUrl = Thread.currentThread().getContextClassLoader().getResource(EL_CONFIG_LOGGING);
                File configFile = new File(configFileUrl.getFile());
                File loggingFile = new File(loggingFileUrl.getFile());

                // Create the configuration files and copy config files.
                if (!elHomeConfig.mkdirs()) throw new RuntimeException("Cannot create config directory at [" + elHomeConfig.getAbsolutePath() + "].");
                FileUtils.copyFileToDirectory(configFile, elHomeConfig);
                FileUtils.copyFileToDirectory(loggingFile, elHomeConfig);

                // Set the system properties for running the EL server.
                System.setProperty(EL_PROPERTY_ELASTICSEARCH, "");
                System.setProperty(EL_PROPERTY_FOREGROUND, "yes");
                System.setProperty(EL_PROPERTY_HOME, elHome.getAbsolutePath());
                System.setProperty(EL_PROPERTY_SCRIPT_INLINE, "on");
                System.setProperty(EL_PROPERTY_SCRIPT_INDEXED, "on");

                // Run the EL server.
                // ELS_THREAD.setDaemon(true);
                // ELS_THREAD.start();

                startInstance();
        }

        public static void createAndPopulateExpenseReportsIndex() throws Exception{
                ElasticSearchUrlBuilder urlBuilder = new ElasticSearchUrlBuilder(EL_SERVER, ElasticSearchDataSetTestBase.EL_EXAMPLE_INDEX);

                // Create the expensereports example index.
                createIndexELServer(urlBuilder, EL_EXAMPLE_MAPPINGS);

                // Populate the server with some test content.
                populateELServer(urlBuilder, EL_EXAMPLE_DATA);

                // Test mappings and document count.
                testMappingCreated(urlBuilder);
                testDocumentsCount(urlBuilder);
        }

        public static void createAndPopulateExpenseReportsCSensitiveIndex() throws Exception{
                ElasticSearchUrlBuilder urlBuilder = new ElasticSearchUrlBuilder(EL_SERVER, EL_EXAMPLE_CSENSITIVE_INDEX);

                // Create the expensereports example index.
                createIndexELServer(urlBuilder, EL_EXAMPLE_CSENSITIVE_MAPPINGS);

                // Populate the server with some test content.
                populateELServer(urlBuilder, EL_EXAMPLE_CSENSITIVE_DATA);

        }

        public static void createAndPopulateEmptyIntervalsIndex() throws Exception{
                ElasticSearchUrlBuilder urlBuilder = new ElasticSearchUrlBuilder(EL_SERVER, EL_EXAMPLE_EMPTYINTERVALS_INDEX);

                // Create the expensereports example index.
                createIndexELServer(urlBuilder, EL_EXAMPLE_EMPTYINTERVALS_MAPPINGS);

                // Populate the server with some test content.
                populateELServer(urlBuilder, EL_EXAMPLE_EMPTYINTERVALS_DATA);

        }

        public static void createAndPopulateMultiFieldsIndex() throws Exception{
                ElasticSearchUrlBuilder urlBuilder = new ElasticSearchUrlBuilder(EL_SERVER, EL_EXAMPLE_MULTIFIELDS_INDEX);

                // Create the expensereports example index.
                createIndexELServer(urlBuilder, EL_EXAMPLE_MULTIFIELDS_MAPPINGS);

                // Populate the server with some test content.
                populateELServer(urlBuilder, EL_EXAMPLE_MULTIFIELDS_DATA);

        }

        private static void startInstance() {
                Elasticsearch.main(new String[]{});
        }

        public static void createIndexELServer(ElasticSearchUrlBuilder urlBuilder, String jsonMappingsFile) throws Exception {

                // Create an http client
                CloseableHttpClient httpclient = HttpClients.createDefault();

                // Obtain data to configure & populate the server.
                String mappingsContent = getFileAsString(jsonMappingsFile);

                // Create an index mappings.
                HttpPost httpPost = new HttpPost(urlBuilder.getIndexRoot());
                StringEntity inputMappings = new StringEntity(mappingsContent);
                inputMappings.setContentType(CONTENTTYPE_JSON);
                httpPost.setEntity(inputMappings);
                CloseableHttpResponse mappingsResponse = httpclient.execute(httpPost);
                if (mappingsResponse.getStatusLine().getStatusCode() != EL_REST_RESPONSE_OK) {
                        log("Error response body:");
                        log(responseAsString(mappingsResponse));
                }
                Assert.assertEquals(EL_REST_RESPONSE_OK, mappingsResponse.getStatusLine().getStatusCode());
        }

        public static void populateELServer(ElasticSearchUrlBuilder urlBuilder, String dataFile) throws Exception {

                // Insert documents in bulk mode.
                CloseableHttpClient httpclient = HttpClients.createDefault();
                File dataContentFile = new File(Thread.currentThread().getContextClassLoader().getResource(dataFile).getFile());
                addDocuments(httpclient, urlBuilder, dataContentFile);

                // Let EL server some time to index all documents...
                Thread.sleep(5000);
        }

        /**
         * <p>Index documents in bulk mode into EL server.</p>
         */
        protected static void addDocuments(CloseableHttpClient httpClient, ElasticSearchUrlBuilder urlBuilder, File dataContentFile) throws Exception {
                HttpPost httpPost2 = new HttpPost(urlBuilder.getBulk());
                FileEntity inputData = new FileEntity(dataContentFile);
                inputData.setContentType(CONTENTTYPE_JSON);
                httpPost2.addHeader(HEADER_ACCEPT, CONTENTTYPE_JSON);
                httpPost2.addHeader(HEADER_CONTENTTYPE, CONTENTTYPE_JSON);
                httpPost2.setEntity(inputData);
                CloseableHttpResponse dataResponse = httpClient.execute(httpPost2);
                if (dataResponse.getStatusLine().getStatusCode() != EL_REST_RESPONSE_OK) {
                        log("Error response body:");
                        log(responseAsString(dataResponse));
                }
                httpPost2.completed();
                Assert.assertEquals(dataResponse.getStatusLine().getStatusCode(), EL_REST_RESPONSE_OK);
        }

        /**
         * <p>Index a single document into EL server.</p>
         */
        protected static void addDocument(ElasticSearchUrlBuilder urlBuilder, CloseableHttpClient httpclient, String type, String document) throws Exception {
                HttpPost httpPut = new HttpPost(urlBuilder.getIndexRoot() + "/" + type);
                StringEntity inputData = new StringEntity(document);
                inputData.setContentType(CONTENTTYPE_JSON);
                httpPut.addHeader(HEADER_ACCEPT, CONTENTTYPE_JSON);
                httpPut.addHeader(HEADER_CONTENTTYPE, CONTENTTYPE_JSON);
                httpPut.setEntity(inputData);
                CloseableHttpResponse dataResponse = httpclient.execute(httpPut);
                if (dataResponse.getStatusLine().getStatusCode() != EL_REST_RESPONSE_CREATED) {
                        log("Error response body:");
                        log(responseAsString(dataResponse));
                }
                Assert.assertEquals(dataResponse.getStatusLine().getStatusCode(), EL_REST_RESPONSE_CREATED);
        }

        // Not necessary use of @AfterClass - @see ElasticSearchTestSuite.java.
        public static void stopELServer(TemporaryFolder elHomeFolder) throws Exception {
                // Clear the system properties that have been set for running the EL server.
                System.clearProperty(EL_PROPERTY_ELASTICSEARCH);
                System.clearProperty(EL_PROPERTY_FOREGROUND);
                System.clearProperty(EL_PROPERTY_HOME);
                System.clearProperty(EL_PROPERTY_SCRIPT_INLINE);
                System.clearProperty(EL_PROPERTY_SCRIPT_INDEXED);

                // Stop the EL server.
                Elasticsearch.close(new String[]{});
                // ELS_THREAD.join();

                // Delete the working home folder for elasticsearch.
                elHomeFolder.delete();
        }

        public static void testMappingCreated(ElasticSearchUrlBuilder urlBuilder) throws Exception {
                Object[] response = doGet(urlBuilder.getIndexRoot());
                Assert.assertEquals(response[0], EL_REST_RESPONSE_OK);
                log("Mappings for index [" + ElasticSearchDataSetTestBase.EL_EXAMPLE_INDEX + "]:");
                log(response[1]);
        }

        public static void testDocumentsCount(ElasticSearchUrlBuilder urlBuilder) throws Exception {
                Object[] response = doGet(urlBuilder.getIndexCount());
                Assert.assertEquals(response[0], EL_REST_RESPONSE_OK);
                log("Count for index [" + ElasticSearchDataSetTestBase.EL_EXAMPLE_INDEX + "]:");
                log(response[1]);
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

        public static class ElasticSearchUrlBuilder {
                private String serverUrl;
                private String index;

                public ElasticSearchUrlBuilder(String serverUrl, String index) {
                        Assert.assertTrue(serverUrl != null && serverUrl.trim().length() > 0);
                        Assert.assertTrue(index != null && index.trim().length() > 0 && !index.endsWith(SYMBOL_SLASH));
                        this.serverUrl = serverUrl;
                        this.index = index;

                        if (!this.serverUrl.endsWith(SYMBOL_SLASH)) this.serverUrl = this.serverUrl + SYMBOL_SLASH;
                }

                public String getRoot() {
                        return serverUrl;
                }

                public String getIndexRoot() {
                        return serverUrl + index;
                }

                public String getIndexCount() {
                        return getIndexRoot() + SYMBOL_SLASH + EL_REST_COUNT;
                }

                public String getBulk() {
                        return serverUrl + EL_REST_BULK;
                }
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
