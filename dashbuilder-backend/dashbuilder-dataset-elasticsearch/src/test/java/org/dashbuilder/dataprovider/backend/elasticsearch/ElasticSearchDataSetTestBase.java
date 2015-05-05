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
package org.dashbuilder.dataprovider.backend.elasticsearch;

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
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFormatter;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.backend.DataSetDefJSONMarshaller;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.elasticsearch.bootstrap.Bootstrap;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

/**
 * <p>Base test for ElasticSearch providers and datasets.</p>
 * 
 * <p>This test does:</p>
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
 * <p>By default this index wil be available at <code><http://localhost:9200/expensereports/code></p>
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
 * @since 0.3.0
 */
@RunWith(Arquillian.class)
public class ElasticSearchDataSetTestBase {

    // NOTE: If you change the host or port in config/elasticsearch.yml, you should modify this value.
    public static final String EL_SERVER = "http://localhost:9200/";
    
    // System properties for EL server.
    protected static final String EL_PROPERTY_ELASTICSEARCH = "elasticsearch";
    protected static final String EL_PROPERTY_HOME = "es.path.home";
    protected static final String EL_PROPERTY_FOREGROUND = "es.foreground";

    // Config files & example data for running EL server.
    protected static final String EL_CONFIG_DIR = "config";
    protected static final String EL_CONFIG_ELASTICSEARCH = "org/dashbuilder/dataprovider/backend/elasticsearch/server/config/elasticsearch.yml";
    protected static final String EL_CONFIG_LOGGING = "org/dashbuilder/dataprovider/backend/elasticsearch/server/config/logging.yml";
    protected static final String EL_EXAMPLE_INDEX = "expensereports";
    protected static final String EL_EXAMPLE_TYPE = "expense";
    protected static final String EL_EXAMPLE_MAPPINGS = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-mappings.json";
    protected static final String EL_EXAMPLE_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-data.json";
    protected static final String EL_EXAMPLE_MORE_DATA = "org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-more-data.json";
    protected static final String EL_EXAMPLE_COLUMN_ID = "id";
    protected static final String EL_EXAMPLE_COLUMN_AMOUNT = "amount";
    protected static final String EL_EXAMPLE_COLUMN_DEPT = "department";
    protected static final String EL_EXAMPLE_COLUMN_EMPLOYEE = "employee";
    protected static final String EL_EXAMPLE_COLUMN_DATE = "date";
    protected static final String EL_EXAMPLE_COLUMN_CITY = "city";
    protected static final String EL_EXAMPLE_DEPT_ENGINEERING = "Engineering";
    protected static final String EL_EXAMPLE_DEPT_SERVICES = "Services";
    protected static final String EL_EXAMPLE_DEPT_MANAGEMENT = "Management";
    protected static final String EL_EXAMPLE_DEPT_SALES = "Sales";
    protected static final String EL_EXAMPLE_DEPT_SUPPORT = "Support";
    protected static final String EL_EXAMPLE_CITY_BARCELONA = "Barcelona";
    protected static final String EL_EXAMPLE_CITY_MADRID = "Madrid";
    protected static final String EL_EXAMPLE_CITY_RALEIGH = "Raleigh";
    protected static final String EL_EXAMPLE_CITY_LONDON = "London";
    protected static final String EL_EXAMPLE_EMP_ROXIE = "Roxie Foraker";
    protected static final String EL_EXAMPLE_EMP_JAMIE = "Jamie Gilbeau";
    protected static final String EL_EXAMPLE_EMP_NITA = "Nita Marling";
    protected static final String EL_EXAMPLE_EMP_HANNA = "Hannah B. Mackey";
    protected static final String EL_EXAMPLE_EMP_PATRICIA = "Patricia J. Behr";
    
    
    
    // EL remote REST API endpoints & other parameters.
    protected static final String EL_REST_BULK = "_bulk";
    protected static final String EL_REST_COUNT = "_count";
    protected static final int EL_REST_RESPONSE_OK = 200;
    protected static final int EL_REST_RESPONSE_CREATED = 201;
    
    // Other constants.
    protected static final String HEADER_ACCEPT = "Accept";
    protected static final String HEADER_CONTENTTYPE = "content-type";
    protected static final String CONTENTTYPE_TEXTPLAIN = "text/plain; charset=utf-8";
    protected static final String CONTENTTYPE_JSON = "application/json; charset=utf-8";
    protected static final String ENCODING = "UTF-8";
    protected static final String SYMBOL_SLASH = "/";

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    DataSetManager dataSetManager;

    @Inject
    DataSetFormatter dataSetFormatter;

    @Inject
    DataSetDefRegistry dataSetDefRegistry;

    @Inject
    DataSetDefJSONMarshaller jsonMarshaller;

    @ClassRule
    public static TemporaryFolder elHomeFolder = new TemporaryFolder();
    
    protected static ElasticSearchUrlBuilder urlBuilder = new ElasticSearchUrlBuilder(EL_SERVER, EL_EXAMPLE_INDEX);
    
    // For local testing against an existing and running EL server.
    private static boolean runServer = true;
    private static boolean populateServer = true;

    @BeforeClass
    public static void runELServer() throws Exception {
        if (runServer) {
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

            // Run the EL server.
            new Thread("test_ELserver") {
                @Override
                public void run() {
                    Bootstrap.main(new String[] {});
                }
            }.start();
        }
        
        if (populateServer) {
            // Create the expensereports example index.
            createIndexELServer();

            // Populate the server with some test content.
            populateELServer(EL_EXAMPLE_DATA);

            // Test mappings and document count.
            testMappingCreated();
            testDocumentsCount();
            
        }
    }

    public static void createIndexELServer() throws Exception {

        // Create an http client
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // Obtain data to configure & populate the server.
        String mappingsContent = getFileAsString(EL_EXAMPLE_MAPPINGS);

        // Create an index mappings.
        HttpPost httpPost = new HttpPost(urlBuilder.getIndexRoot());
        StringEntity inputMappings = new StringEntity(mappingsContent);
        inputMappings.setContentType(CONTENTTYPE_JSON);
        httpPost.setEntity(inputMappings);
        CloseableHttpResponse mappingsResponse = httpclient.execute(httpPost);
        if (mappingsResponse.getStatusLine().getStatusCode() != EL_REST_RESPONSE_OK) {
            System.out.println("Error response body:");
            System.out.println(responseAsString(mappingsResponse));
        }
        Assert.assertEquals(EL_REST_RESPONSE_OK, mappingsResponse.getStatusLine().getStatusCode());
    }
    
    public static void populateELServer(String dataFile) throws Exception {

        // Insert documents in bulk mode.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        File dataContentFile = new File(Thread.currentThread().getContextClassLoader().getResource(dataFile).getFile());
        addDocuments(httpclient, dataContentFile);
        
        // Let EL server some time to index all documents...
        Thread.sleep(5000);
    }

    /**
     * <p>Index documents in bulk mode into EL server.</p>
     */
    protected static void addDocuments(CloseableHttpClient httpClient, File dataContentFile) throws Exception {
        HttpPost httpPost2 = new HttpPost(urlBuilder.getBulk());
        FileEntity inputData = new FileEntity(dataContentFile);
        inputData.setContentType(CONTENTTYPE_JSON);
        httpPost2.addHeader(HEADER_ACCEPT, CONTENTTYPE_JSON);
        httpPost2.addHeader(HEADER_CONTENTTYPE, CONTENTTYPE_JSON);
        httpPost2.setEntity(inputData);
        CloseableHttpResponse dataResponse = httpClient.execute(httpPost2);
        if (dataResponse.getStatusLine().getStatusCode() != EL_REST_RESPONSE_OK) {
            System.out.println("Error response body:");
            System.out.println(responseAsString(dataResponse));
        }
        httpPost2.completed();
        Assert.assertEquals(dataResponse.getStatusLine().getStatusCode(), EL_REST_RESPONSE_OK);
    }

    /**
     * <p>Index a single document into EL server.</p>
     */
    protected static void addDocument(CloseableHttpClient httpclient, String type, String document) throws Exception {
        HttpPost httpPut = new HttpPost(urlBuilder.getIndexRoot() + "/" + type);
        StringEntity inputData = new StringEntity(document);
        inputData.setContentType(CONTENTTYPE_JSON);
        httpPut.addHeader(HEADER_ACCEPT, CONTENTTYPE_JSON);
        httpPut.addHeader(HEADER_CONTENTTYPE, CONTENTTYPE_JSON);
        httpPut.setEntity(inputData);
        CloseableHttpResponse dataResponse = httpclient.execute(httpPut);
        if (dataResponse.getStatusLine().getStatusCode() != EL_REST_RESPONSE_CREATED) {
            System.out.println("Error response body:");
            System.out.println(responseAsString(dataResponse));
        }
        Assert.assertEquals(dataResponse.getStatusLine().getStatusCode(), EL_REST_RESPONSE_CREATED);
    }

    @AfterClass
    public static void stopELServer() throws Exception {
        if (!runServer) return;
        
        // Clear the system properties that have been set for running the EL server.
        System.clearProperty(EL_PROPERTY_ELASTICSEARCH);
        System.clearProperty(EL_PROPERTY_FOREGROUND);
        System.clearProperty(EL_PROPERTY_HOME);

        // Stop the EL server.
        Bootstrap.close(new String[]{});
        
        // Delete the working home folder for elasticsearch.
        elHomeFolder.delete();
    }

    /**
     * Registers a dataset given into the <code>resource</code> definition.
     * 
     * @param resource The dataset definition resource.
     */
    protected ElasticSearchDataSetDef _registerDataSet(String resource) throws Exception {
        // Register the SQL data set
        URL fileURL = Thread.currentThread().getContextClassLoader().getResource(resource);
        String json = IOUtils.toString(fileURL);
        ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);
        return def;
    }

    public static void testMappingCreated() throws Exception {
        Object[] response = doGet(urlBuilder.getIndexRoot());
        Assert.assertEquals(response[0], EL_REST_RESPONSE_OK);
        System.out.println("Mappings for index [" + EL_EXAMPLE_INDEX + "]:");
        System.out.println(response[1]);
    }

    public static void testDocumentsCount() throws Exception {
        Object[] response = doGet(urlBuilder.getIndexCount());
        Assert.assertEquals(response[0], EL_REST_RESPONSE_OK);
        System.out.println("Count for index [" + EL_EXAMPLE_INDEX + "]:");
        System.out.println(response[1]);
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

    /**
     * Helper method to print to standard output the dataset values.
     */
    protected void printDataSet(DataSet dataSet) {
        final String SPACER = "| \t |";
        
        if (dataSet == null) System.out.println("DataSet is null");
        if (dataSet.getRowCount() == 0) System.out.println("DataSet is empty");
        
        List<DataColumn> dataSetColumns = dataSet.getColumns();
        int colColunt = dataSetColumns.size();
        int rowCount = dataSet.getRowCount();

        System.out.println("********************************************************************************************************************************************************");
        for (int row = 0; row < rowCount; row++) {
            System.out.print(SPACER);
            for (int col= 0; col< colColunt; col++) {
                Object value = dataSet.getValueAt(row, col);
                System.out.print(value);
                System.out.print(SPACER);
            }
            System.out.println("");
        }
        System.out.println("********************************************************************************************************************************************************");
    }
}
