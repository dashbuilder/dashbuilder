Elastic Search Data Set Provider
================================

This module provides the integration between JBoss Dashbuilder and Elastic Search.          

Table of contents
------------------

* **[Usage](#usage)**
* **[Data types mappings](#data-types-mappings)**
* **[How Elastic Search Data Set Provider works](#how-elastic-search-data-set-provider-works)**
* **[Data Set Look-up operation transformations](#data-set-look-up-operation-transformations)**
* **[Elastic Search Client](#elastic-search-client)**
* **[Elastic Search Query builder](#elastic-search-query-builder)**
* **[Running an EL server with examples](#running-an-el-server-with-examples)**
* **[Notes](#notes)**

Usage
-----

**Data Set definition**

In order to define an ElasticSearch data set you have to set these mandatory parameters:                
* <code>provider</code>: Value MUST be <code>ELASTICSEARCH</code> (MANDATORY)         
* <code>serverURL</code>: The URL for the ElasticSearch server remote API. For example: <code>http://localhost:9200</code> (MANDATORY)           
* <code>clusterName</code>: The cluster name to query in the given ElasticSearch server (MANDATORY)           
* <code>index</code>: The index name in the ElasticSearch server to be queried (MANDATORY)           

These other parameters are optional:                
* <code>type</code>: The document type to query in the given index. It can contain multiple values, comma separated. If not defined, all types for the given index are used. (OPTIONAL)           

Here is an example of a DataSet definition JSON contents:                        

    {
        "uuid": "expense_reports",
        "provider": "ELASTICSEARCH",
        "pushEnabled": true,
        "pushMaxSize": 1024,
        "isPublic": true,
        "serverURL": "http://localhost:9200",
        "clusterName": "elasticsearch",
        "index": "expensereports",
        "type": "expense",
        "cacheEnabled": false,
        "cacheMaxRows": 1000,
        "columns": [
                    {"id": "id", "type": "number"},
                    {"id": "amount", "type": "number"},
                    {"id": "department", "type": "label"},
                    {"id": "employee", "type": "text"},
                    {"id": "date", "type": "date"},
                    {"id": "city", "type": "label"},
                ]
    }

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

You can override a mapping for a given column using the <code>columns</code> property in the data set definition.                    


Data types mappings
-------------------

The communication between Dashbuilder backend and the Elastic Search server instance is performed using the REST API services from ELS. In order to send and receive the JSON messages the Elastic Search Data Provider has to serialize and de-serialize document values into String formats. This serialization patterns are given by the type of the field and it's pattern, if any, defined in your index mappings.                  

By default, when you create an Elastic Search data set and perform any data look-up, the data provider performs and index mappings query to the ELS instance for your index, and determine the column format and pattern as:                       

**String fields**              

* String fields are considered LABEL or TEXT column types in Dashbuilder, depending if the field is analyzed or not analyzed.              
* String fields that are analyzed are considered TEXT column types in Dashbuilder.               
* String fields that are not analyzed are considered LABEL column types in Dashbuilder.               

*IMPORTANT NOTE:* An analyzed index field in the ELS instance cannot be never used as <code>LABEL</code> column type for Dashbuilder.       

**Numeric fields**                         

* The numeric field types <code>float, double, byte, short, integer, long</code> are considered NUMBER column types in Dashbuilder.                      
* As Dashbuilder numeric column type does not differentiate between numeric types in ELS (int, float, etc), the concrete type is stored in the column's pattern.                   

**Date fields**          

* Date fields are considered DATE column type in Dashbuilder.                        
* Elastic Search uses the [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601) format for date/time as the default one. It allows to specify a concrete one in your index mappings, if necessary.                     
* Dashbuilder determines the format for the date field by querying the mappings for your index, so you don't have to specify any pattern for the marshalling process.                 
* If the index mapping response do not contain any format information for the date field, it uses the default format as Elastic Search, the the [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601).                     
* NOTE: You can use multiple formats for a given date field in Elastic Search (eg: <code>yyyy/MM/dd HH:mm:ss||yyyy/MM/dd</code>), but this feature is NOT supported in Dashbuilder.                   
* NOTE: In order to perform data set look-ups using FIXED date interval types, *groovy dynamic scripting* must be enabled in your ElasticSearch server. For more information go [here](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/modules-scripting.html#_enabling_dynamic_scripting).              

How Elastic Search Data Set Provider works
------------------------------------------

The main goal for the Elastic Search Data Provider is to be able to perform data set look-up operations against an Elastic Search server instance.                  

There exist several components used to achieve the provider's goal, than can be customized to handle your custom index/es mappings and settings, if the default ones do not fit in your environment.
These components are:                

* Elastic Search Client - This component is used by the provider to transform look-up filter and sort operations into ELS query *aggregations* and *sort* operations, and manage the communication between the ELS instance and the Dashbuilder backend. See next section *Elastic Search Client* for more information.                                   
* Elastic Search Query builder - This component is used by the provider to transform the look-up filter operations (given by Dashbuilder users) into ELS *queries* and *filters*. See next section *Elastic Search Query builder* for more details.                   

Data Set Look-up operation transformations
------------------------------------------

In order to perform data set look-up operations, the Elastic Search Data Provider has to generate the JSON query to send to the REST API of the ELS instance. This query
 is generated depending on the look-up operations specified by the user as:            
 
**Filter operations**                 
* Data Set look-up filtering operations are translated by the Elastic Search Query builder into ELS *queries* and *filters*.                   
* The bean used to translate the operations into ELS queries is <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchQueryBuilder</code> . See next section *Elastic Search Client* for more information.                     
* The default implementation provided for that bean is <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.ElasticSearchQueryBuilderImpl</code>. See next section *Elastic Search Client* for more information.               
* You can provide your own bean implementation, if necessary, to improve the performance or the features for your given custom index mappings and settings.             
                    
**Group operations**                   
* Data Set look-up grouping operations are translated by the Elastic Search Client into ELS *aggregations*.                   

**Sort operations**                   
* Data Set look-up sorting operations are translated by the Elastic Search Client into ELS queries *sort* operations.                   


Elastic Search Client
---------------------

The ElasticSearch data set provider implementation is decoupled from the REST API client used to query the server instance.        

The client is responsible for:                 
* Translating data set look-up group operations into ELS query *aggregations*.                   
* Translating data set look-up sort operations into ELS query *sort* operations.                   
* Marshall and unmarshall the JSON requests from/to the ELS instance.                                          

By default, the provided client implementation is based on [ElasticSearch Jest client](https://github.com/searchbox-io/Jest), as this implementation works with Java 6+.                   

You can build your own client implementation by implementing the interface <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient</code> and use the CDI features to override it as the default one.         

Elastic Search Query builder
----------------------------

Data Set look-up filtering operations are translated by the Elastic Search Query builder into ELS *queries* and *filters* by the <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchQueryBuilder</code> bean.                         

By default, the implementation given for this bean is <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.ElasticSearchQueryBuilderImpl</code>. It tranforms filters into ELS queries as:                    
 

<table>
    <tr>
        <th>Filter operation</th>
        <th>ELS query</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>IS_NULL</td>
        <td>EXISTS filter (negated)</td>
        <td>The EXISTS filter is negated with a NOT boolean query</td>
    </tr>
    <tr>
        <td>NOT_NULL</td>
        <td>EXISTS filter</td>
        <td></td>
    </tr>
    <tr>
        <td>EQUALS_TO</td>
        <td>TERM filter or MATCH query</td>
        <td>If the column type is LABEL, the filter is transformed into a TERM filter query, otherwise into a MATCH query</td>
    </tr>
    <tr>
        <td>NOT_EQUALS_TO</td>
        <td>TERM filter or MATCH query (negated)</td>
        <td>If the column type is LABEL, the filter is transformed into a TERM filter query, otherwise into a MATCH query</td>
    </tr>
    <tr>
        <td>LIKE_TO</td>
        <td>Not implemented yet</td>
        <td>Not implemented yet</td>
    </tr>
    <tr>
        <td>LOWER_THAN</td>
        <td>RANGE filter</td>
        <td></td>
    </tr>
    <tr>
        <td>LOWER_OR_EQUALS_TO</td>
        <td>RANGE filter</td>
        <td></td>
    </tr>
    <tr>
        <td>GREATER_THAN</td>
        <td>RANGE filter</td>
        <td></td>
    </tr>
    <tr>
        <td>BETWEEN</td>
        <td>RANGE filter</td>
        <td></td>
    </tr>    
    <tr>
        <td>TIME_FRAME</td>
        <td>RANGE filter</td>
        <td></td>
    </tr>    
</table>

If you don't feel comfortable with those default queries and filter generated by the default bean implementation, due to index/es mapping incompatibilities or the performance issues, you can provide you own Query Builder implementation by implementing the interface <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchQueryBuilder</code> and use CDI to override it as the default one.                   


Running an EL server with examples
----------------------------------

These are the steps for running an ElasticSearch server instance locally, if you want to use it as source for an ElasticSearch data provider:               

1.- Download ElasticSearch version <code>1.7.X</code> from [downloads page](http://www.elasticsearch.org/download/) and follow the installation instructions                

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

Here is an example of a DataSet definition for this example:                        

    {
        "uuid": "expense_reports",
        "provider": "ELASTICSEARCH",
        "pushEnabled": true,
        "pushMaxSize": 1024,
        "isPublic": true,
        "serverURL": "http://localhost:9200",
        "clusterName": "elasticsearch",
        "index": "expensereports",
        "type": "expense",
        "cacheEnabled": false,
        "cacheMaxRows": 1000,
        "columns": [
                    {"id": "id", "type": "number"},
                    {"id": "amount", "type": "number"},
                    {"id": "department", "type": "label"},
                    {"id": "employee", "type": "text"},
                    {"id": "date", "type": "date"},
                    {"id": "city", "type": "label"},
                ]
    }


Notes
-----
* This module has benn build and tested against an ElasticSearch server version  <code>1.7.1</code>. In other releases you may hit with some incompatibility issues.                     
* In order to perform data set look-ups using FIXED date interval types, *groovy dynamic scripting* must be enabled in your ElasticSearch server. For more information go [here](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/modules-scripting.html#_enabling_dynamic_scripting).           
