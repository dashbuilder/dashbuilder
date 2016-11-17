Dashbuilder Hibernate Validator
===============================

Introduction
------------
This module contains copied sources from <code>org.hibernate:hibernate-validator:jar:4.1.0.Final</code>.               

Considerations:
* For using GWT validations framework a dependency to <code>org.hibernate:hibernate-validator:jar:4.1.0.Final</code> is required.                    
* There exist a [GWT Issue](http://code.google.com/p/google-web-toolkit/issues/detail?id=7661) that does not allow to use hibernate validator versions greater than <code>4.1.0.Final</code>.                
* The JBoss IP BOM defines a newer version of hibernate validator: <code>org.hibernate:hibernate-validator:jar:4.3.2.Final</code>.            
* If adding in this project a dependency to <code>org.hibernate:hibernate-validator:jar:4.1.0.Final</code>, by the transitive resolution mechanism, the version that is finally used is <code>4.3.2.Final</code>, so the GWT compilation crashes due to the GWT issue commented above.           

So in order to have all the sources from <code>org.hibernate:hibernate-validator:jar:4.1.0.Final</code> artifact, we have created this maven module with its sources. So you can add a dependency to <code>org.dashbuilder:dashbuilder-hibernate-validator:jar</code> without worrying about if the parent pom defines another hibernate validator version, as artifact coordinates are different and the Maven's transitive resolution mechanism do not modify the version.                    

So until the [GWT Issue](http://code.google.com/p/google-web-toolkit/issues/detail?id=7661) is fixed, this module is added into the GWT classpath in order to have the client side JS303 validation implementations available.           

Usage
-----

To use this module for your JSR303 client validations, you have to provide both jar and sources artifacts to allow your webapp to run in production and DevMode/SuperDevMode.           

Add these dependencies in your pom file:        

    <dependency>
      <groupId>org.dashbuilder</groupId>
      <artifactId>dashbuilder-hibernate-validator</artifactId>
    </dependency>

    <dependency>
      <groupId>org.dashbuilder</groupId>
      <artifactId>dashbuilder-hibernate-validator</artifactId>
      <classifier>sources</classifier>
    </dependency>

NOTE: The GWT Module for HibernateValidations is provided by <code>com.google.gwt:gwt-user:jar</code>, so you don't have to include any extra GWT module in your project.            
