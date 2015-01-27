Elastic Search Data Set Provider
================================

This module provides the integration between JBoss Dashbuilder and ElasticSearch server.          

Table of contents
------------------

* **[Usage](#usage)**
* **[Running an EL server with examples](#running-an-el-server-with-examples)**
* **[clients](#clients)**
* **[Query builder](#query-builder)**
* **[Notes](#notes)**


Usage
-----

**Dataset definition**

In order to define an ElasticSearch dataset you have to set these mandatory parameters:                
* <code>provider</code>: Value MUST be <code>ELASTICSEARCH</code>           
* <code>serverURL</code>: The URL for the ElasticSearch server remote API. For example: <code>http://localhost:9200</code>           
* <code>clusterName</code>: The cluster name to query in the given ElasticSearch server           
* <code>index</code>: The index name in the ElasticSearch server to be queried           

These other parameters are optional:                
* <code>type</code>: The document type to query in the given index. It can contain multiple values, comma separated. If not defined, all types for the given index are used.           

**Supported column types and mappings**

By default, the following ElasticSearch core types are mapped into Dashbuilder types as:                     
<table>
    <tr>
        <th>ElasticSearch type</th>
        <th>Dashbuilder type</th>
    </tr>
    <tr>
        <td>string (analyzed)</td>
        <td>TEXT</td>
    </tr>
    <tr>
        <td>string (not analyzed)</td>
        <td>LABEL</td>
    </tr>
    <tr>
        <td>float</td>
        <td>NUMBER</td>
    </tr>
    <tr>
        <td>double</td>
        <td>NUMBER</td>
    </tr>
    <tr>
        <td>byte</td>
        <td>NUMBER</td>
    </tr>
    <tr>
        <td>short</td>
        <td>NUMBER</td>
    </tr>
    <tr>
        <td>integer</td>
        <td>NUMBER</td>
    </tr>
    <tr>
        <td>long</td>
        <td>NUMBER</td>
    </tr>
    <tr>
        <td>token_count</td>
        <td>LABEL</td>
    </tr>
    <tr>
        <td>date</td>
        <td>DATE</td>
    </tr>
    <tr>
        <td>boolean</td>
        <td>LABEL</td>
    </tr>
    <tr>
        <td>binary</td>
        <td>LABEL</td>
    </tr>
</table>

You can override a mapping for a given column using the <code>columns</code> property in the dataset definition. But keep in mind that analyzed index fields cannot be never used as <code>LABEL</code> type for Dashbuilder.       

Running an EL server with examples
----------------------------------

These are the steps for running an ElasticSearch server instance locally, if you want to use it as source for an ElasticSearch data provider:               

1.- Download ElasticSearch version <code>1.4.2</code> from [downloads page](http://www.elasticsearch.org/download/) and follow the installation instructions                

2.- Run the ElasticSearch server using the command:
    
    <EL_HOME>/bin/elasticsearch -f
    
Next step is to create the expense reports example index and bulk some data:           

3.- Create the index mappings using the JSON definition found [here](./src/test/resources/org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-mappings.json)                      
    
    curl -XPUT http://localhost:9200/expensereports -d '<JSON_MAPPINGS_DEFINITION>'
    
4.- Index using bulk operation some example data found [here](./src/test/resources/org/dashbuilder/dataprovider/backend/elasticsearch/server/example-data/expensereports-data.json)               
    
    curl -XPUT http://localhost:9200/_bulk --data-binary @expensereports-data.json

Once index mappings and data are indexed, you can try to query the ElasticSearch server using:                     

    curl -XGET http://localhost:9200/expensereports/_count
    
You should obtain a resulting value count of <code>50</code> documents.
    
Clients
-------

The ElasticSearch dataset provider implementation is decoupled from the REST API client used to query the server.       

Be default the provided implementation is the [ElasticSearch java client](http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/client.html). This implementation requires to build the application using **Java 1.7+**.                

You can build your own client implementation by implementing the interface <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.ElasticSearchClient</code> and use CDI to override it as the default one.         

Query builder
-------------

The ElasticSearch dataset provider allows to perform queries to the server by just setting, as minimal configuration, the index name to query.        

This advantage is useful for not technical application users, as it introspects the index mappings and generate queries that match the dataset lookup constraints that can be applied.         

If you don't feel comfortable with the default queries that are build to query the server or with the performance in some situations, you can provide you own Query Builder implementation by implementing the interface <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.ElasticSearchQueryBuilder</code> and use CDI to override it as the default one.                   

Notes
-----
* In order to use the official elasticsearch java client api as rest client, you must compile and run using Java 1.7+.               
* This module has benn build and tested against an ElasticSearch server version  <code>1.4.2</code>. In other releases you may hit with some incompatibility issues.                     
