UF Dashbuilder - Distributions Builder
=======================================

This module generates the product distribution for different applications servers.             

Usage
-----
This module is not build by default. You can build it in two ways:                    

*Run the maven build on this module path `dashbuilder-distros`:       
 
    $ cd dashbuilder/dashbuilder-distros/
    $ mvn clean install -DskipTests

*Run the maven build on the root path for the project and use the `full` Maven profile:            

    $ cd dashbuilder/
    $ mvn clean install -DskipTests -Dfull
    
Distributions
-------------
 
Currently, the following artifacts are generated:                   

* **dashbuilder-wildfly8.war:**  Product distribution for the JBoss Widlfly 8.x application server.             

  Detailed installation instructions [here](./src/main/wildfly8/README.md).          

* **dashbuilder-eap6_4.war:**  Product distribution for the JBoss EAP 6.4 application server.           

  Detailed installation instructions [here](./src/main/eap6_4/README.md).           

* **dashbuilder-tomcat7.war:**  Product distribution for Apache Tomcat 7 server.           

  Detailed installation instructions [here](./src/main/tomcat7/README.md).             
