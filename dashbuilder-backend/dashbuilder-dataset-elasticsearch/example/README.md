Example - Expense Reports
=========================

This document provides the necessary steps and files for running an ElasticSearch server and consuming the services from Dashbuilder by the use of the Elastic Search data provider.                        

1.- Download ElasticSearch version <code>2.1.2</code> from [downloads page](https://www.elastic.co/downloads/elasticsearch/) and follow the installation instructions                

2.- In order to perform data set look-ups using FIXED date interval types, *groovy dynamic scripting* must be enabled in your ElasticSearch server. You can do it
 by editing the configuration file `<EL_HOME>/config/elasticsearch.yml` and adding those lines at the end:                           
 
    script.inline: on
    script.indexed: on

3.- Run the ElasticSearch server using the command:
    
    ./<EL_HOME>/bin/elasticsearch
    
Next step is to create the *expense reports* example index and populate it with some data:           

4.- Create the index mappings using the JSON definition found [here](./expensereports-mappings.json)                      
    
    curl -XPUT http://localhost:9200/expensereports -d '<JSON_MAPPINGS_DEFINITION>'
    
5.- Index using bulk operation some example data found [here](./expensereports-data.json)               
    
    curl -XPUT http://localhost:9200/_bulk --data-binary @expensereports-data.json

Once index mappings and data are indexed, you can try to query the ElasticSearch server using:                     

    curl -XGET http://localhost:9200/expensereports/_count
    
You should obtain a resulting value count of `50` documents.                  

6.- Run Dashbuilder and create a new *Data Set* in the *Authoring area*. Create it using the *ElasticSearch* type and the following parameters:                        

    // Here is an example of a DataSet definition for the expense reports index.                        
    {
        "uuid": "expense_reports",
        "provider": "ELASTICSEARCH",
        "name": "Elastic Expense Reports",
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
                    {"id": "EXPENSES_ID", "type": "number"},
                    {"id": "AMOUNT", "type": "number"},
                    {"id": "DEPARTMENT", "type": "label"},
                    {"id": "EMPLOYEE", "type": "text"},
                    {"id": "CREATION_DATE", "type": "date"},
                    {"id": "CITY", "type": "label"},
                ]
    }

Once the data set is created, you can start creating your data displayers and dashboards that consume the expense reports index from the ELS server node.             

Have fun! :)               

