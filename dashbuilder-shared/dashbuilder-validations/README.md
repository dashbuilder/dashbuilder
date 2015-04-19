Dashbuilder validations module
==============================

Introduction
--------------
Dashbuilder integrates with GWT & JSR303 Bean Validation framework in order to perform both client and server side bean validations.                

This module provides both client and server side validation factories, message resolvers and other related stuff.               

Notes
-----
* As GWT validation framework does not support JSR303 custom Validation Providers (see <code>com.google.gwt.validation.client.impl.Validation#byProvider(Class<U> providerType)</code>, 
    this module is built on top of all dashbuilder shared modules and provides the default provider validation factory class and the default validation messages resolver.                      

**Hibernate validation integration**

Due to a GWT issue with hibernate validator with versions greater <code>4.1.0.Final</code>, this module adds all <code>org.dashbuilder:dashbuilder-hibernate-validator:jar</code> classes and sources on the resulting JAR in package time.                 
For more information see [Dashbuilder Hibernate Validator documentation](../dashbuilder-hibernate-validator/README.md).            