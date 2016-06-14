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
* <code>serverURL</code>: The host and port for the ElasticSearch node transport client. For example: <code>localhost:9300</code> (MANDATORY)           
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
        "serverURL": "localhost:9300",
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
* NOTE: An analyzed index field in the ELS instance cannot be never used as <code>LABEL</code> column type for Dashbuilder. 
Why? Dashbuilder is not a text indexing engine neither a client. The LABEL column type is used internally for data set indexing and 
grouping operations, and the values for this column type are considered to be not analyzed in order to be consisent with other data providers and the Data Set API. 
If your index field have to be analyzed due to any external constraints, and you need it as a LABEL column type, you can do some workarounds such as using multi fields and generating different columns, for example, to achieve the use of different analyzers for same document type's field.            
* NOTE: Case sensitiveness in String fields is determined by the field analyzer used in your mappings. For more information read the section *Elastic Search Query builder* and consider the use of multi fields for applying different analyzers on same field.                     

**Numeric fields**                         

* The numeric field types <code>float, double, byte, short, integer, long</code> are considered NUMBER column types in Dashbuilder.                      
* As Dashbuilder numeric column type does not differentiate between numeric types in ELS (int, float, etc), the concrete type is stored in the column's pattern.                   

**Date fields**          

* Date fields are considered DATE column type in Dashbuilder.                        
* Elastic Search uses the [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601) format for date/time as the default one. It allows to specify a concrete one in your index mappings, if necessary.                     
* Dashbuilder determines the format for the date field by querying the mappings for your index, so you don't have to specify any pattern for the marshalling process.                 
* If the index mapping response do not contain any format information for the date field, it uses the default format as Elastic Search, the the [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601).                     
* NOTE: You can use multiple formats for a given date field in Elastic Search (eg: <code>yyyy/MM/dd HH:mm:ss||yyyy/MM/dd</code>), but this feature is NOT supported in Dashbuilder.                   
* **IMPORTANT NOTE**: In order to perform data set look-ups using FIXED date interval types, *groovy dynamic scripting* must be enabled in your ElasticSearch server. For more information go [here](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/modules-scripting.html#_enabling_dynamic_scripting).              

**Multi fields support**

The use of multi fields in Elastic Search allows several goals, such as using different analyzers for a single field, or different index types for it, etc. Fore more information read the documentation [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-core-types.html#_multi_fields_3).                  

Dashbuilder supports the use of multi fields in any index. Each multi field is considered a data set column using the same naming convention as Elastic Search does:     

    # Use the dot symbol as the multi field separator.
    <main_field>.<multi_field>

If the multi field does not specify the data type or the index type, those values are inherited from the main field.                
 
*Example*                          

* Consider the following index mappings:              

    "field1" : {
        "type": "string", 
        "index": "analyzed" 
    },
    "field2" : {
        "type": "string", 
        "index": "analyzed" ,
        "fields": {
            "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
            }
        }
    }

* The resulting data set columns are:               

<table>
    <tr>
        <th>Column Id</th>
        <th>Column type</th>
    </tr>
    <tr>
        <td>field1</td>
        <td>TEXT</td>
    </tr>
    <tr>
        <td>field2</td>
        <td>TEXT</td>
    </tr>
    <tr>
        <td>field2.raw</td>
        <td>LABEL</td>
    </tr>
</table>


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

The ElasticSearch data set provider implementation is decoupled from the client used to consume the ElasticSearch server.           

The client is responsible for:                 
* Translating data set look-up group operations into ELS query *aggregations*.                   
* Translating data set look-up sort operations into ELS query *sort* operations.                   

You can build your own client implementation by implementing the interface <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient</code> and use CDI features to set it as the default.         

Elastic Search Query builder
----------------------------

Data Set look-up filtering operations are translated by the Elastic Search Query builder into ELS *queries* and *filters* by the <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchQueryBuilder</code> bean.                         

By default, the implementation given for this bean is <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.ElasticSearchQueryBuilderImpl</code>. It transforms filters into ELS queries as:                    
 

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
        <td>TERM/S filter or MATCH query</td>
        <td>If the column type is LABEL, the filter is transformed into a TERM filter query, otherwise into a MATCH query</td>
    </tr>
    <tr>
        <td>NOT_EQUALS_TO</td>
        <td>TERM/S filter or MATCH query (negated)</td>
        <td>If the column type is LABEL, the filter is transformed into a TERM filter query, otherwise into a MATCH query</td>
    </tr>
    <tr>
        <td>LIKE_TO</td>
        <td>Query String Query</td>
        <td>The Dashbuilder's filter option *Case Sensitive* is used for the query parameter <code>lowercase_expanded_terms</code>. Please read the note *LIKE TO operation - Usage and notes* for more details. </td>
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

If you don't feel comfortable with those default queries and filters generated by the default bean implementation, due to index/es mapping incompatibilities or some performance issues in your concrete scenario, you can provide you own Query Builder implementation by implementing the interface <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchQueryBuilder</code> and use CDI to override it as the default one. Just build your custom query structure and use it into the search request!                    

**LIKE TO operation - Usage and notes**            

The Dashbuilder's filter operation *LIKE TO* provides the *Case Sensitive* option. It's used to generate the ELS Query String query parameter <code>lowercase_expanded_terms</code> as:                 
* If it's set to <code>true</code>, then <code>lowercase_expanded_terms = false</code>                          
* If it's set to <code>false</code>, then <code>lowercase_expanded_terms = true</code>                         

This behavior has impact depending on the analyzer used for your field, if it's analyzed. So please, consider the following notes for the LIKE TO filter operation:                     
* String fields that are NOT ANALYZED, the LIKE operation MUST BE always case sensitive (Your fields are not analyzed and ELS is not able to perform the operation)                         
* String fields that are ANALYZED and use the default analyzer (lower-case all the terms), the LIKE operation using case un-sensitive works as excepted, by if you set enable the case sensitive feature, it always will match only with lower-cased patterns, as the terms are indexed in lower-case too. So using the default analyzer for a String field, only expect case un-sensitive working in the LIKE TO operation.                                           
* String fields that are ANALYZED and you want to be case sensitive, you have to specify a custom analyzer for that field in your index mappings (for example, a custom tokenizer analyzer).  So using a custom analyzer for a String field that provider case sensitive indexation, only expect case sensitive working in the LIKE TO operation.                

Summary:                  
* Not Analyzed String fields MUST be always case sensitive.                    
* For using case un-sensitive search on a String analyzed field, analyze it using the default analyzer (lower-cased).                           
* For using case sensitive search on a String analyzed field, analyze it using the custom tokenizer analyzer, as the following index mappings example:                            

        // In this mappings example, the field employee is analyzed using a custom tokenizer analyzer, so LIKE TO filter operation will be done using case sensitive match. 
        {
            "settings": {
                "analysis" : {
                    "analyzer" : {
                        "case_sensitive" : {
                            "type" : "custom",
                            "tokenizer" : "keyword"
                        }
                    }
                }
            },
            "mappings" : {
                "_default_" : {
                    "properties" : {
                        "EXPENSES_ID" : {"type": "integer" },
                        "CITY" : {"type": "string", "index": "not_analyzed" },
                        "DEPARTMENT" : { "type" : "string", "index": "not_analyzed" },
                        "EMPLOYEE" : { "type" : "string", "index": "analyzed", "analyzer" : "case_sensitive"  },
                        "CREATION_DATE" : { 
                            "type" : "date",
                            "format": "MM-dd-YYYY"
                        },
                        "AMOUNT" : { "type" : "float" }
                    }
                }
            }
        }
        
* Consider the use of multi-fields for this index's filed if you need to apply different mappings or analyzers for it, as this provider supports the use of ElasticSearch multi fields.                    

Running an EL server with examples
----------------------------------

See an example [here](./example/README.md).                                            

Notes
-----
             
* This module has benn build and tested against an ElasticSearch server version  <code>2.1.2</code>. In other releases you may hit with some incompatibility issues.                      
        
* In order to perform data set look-ups using FIXED date interval types, *groovy dynamic scripting* must be enabled in your ElasticSearch server. 
For more information go [here](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/modules-scripting.html#_enabling_dynamic_scripting). 
In order to perform data set look-ups using FIXED date interval types, *groovy dynamic scripting* must be enabled in your ElasticSearch server. You can do it 
 by editing the configuration file `<EL_HOME>/config/elasticsearch.yml` and adding those lines at the end:                           
 
        script.inline: on
        script.indexed: on

* Wildfly versions support               

If you're using Wildfly >= `8.2.X `, just skip this point.      

Otherwise, note that Elastic Search 2.x requires `jackson-core` version >= `2.6`. So if you're 
using Widfly at versions < `8.2.X ` you have to exclude the 
jackson core and some related modules in the `jboss-deployment-structure.xml` as:                

        <exclusions>
          <module name="com.fasterxml.jackson.jaxrs.jackson-jaxrs-json-provider"/>
          <module name="com.fasterxml.jackson.core.jackson-core"/>
          <module name="com.fasterxml.jackson.core.jackson-databind"/>
          <module name="org.jboss.resteasy.resteasy-jackson2-provider"/>
        </exclusions>

