Dashbuilder validations module
==============================

* Dashbuilder integrates with GWT & JSR303 Bean Validation framework in order to perform both client and server side bean validations.         
* As GWT validation framework does not support JSR303 custom Validation Providers (see <code>com.google.gwt.validation.client.impl.Validation#byProvider(Class<U> providerType)</code>, 
    this module is built on top of all dashbuilder shared modules and provides the default provider validation factory class and the default validation messages resolver.                      
