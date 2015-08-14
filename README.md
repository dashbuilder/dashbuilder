Dashbuilder
===========

Dashbuilder is a general purpose dashboard and reporting web app which allows for:

* Visual configuration and personalization of dashboards
* Support for different types of visualizations using several charting libraries
* Full featured editor for the definition of chart visualizations
* Definition of interactive report tables
* Data extraction from external systems, through different protocols
* Support for both analytics and real-time dashboards

Licensed under the Apache License, Version 2.0

For further information, please visit the project web site <a href="http://dashbuilder.org" target="_blank">dashbuilder.org</a>

Upcoming features
=================

* New renderers based on D3 JS, Lienzo GWT & Chart JS
* Hierarchical (nested group) displayer types: Tree & Pie
* Support for multiple dynamic data series
* Rich mobility support
* Alerts and SLA configuration
* RESTful API

Architecture
=================

* Not tied to any chart rendering technology. Pluggable renderers.
* No tied to any data storage.
* Ability to read data from: CSV files, Databases, Elastic Search or  Java generators.
* Decoupled client & server layers. Ability to build pure lightweight client dashboards.
* Ability to push & handle data sets on client for better performance.
* Based on <a href="http://www.uberfireframework.org" target="_blank">Uberfire</a>, a framework for building rich workbench styled apps on the web.
* Data provider for the definition of data sets stored into SQL databases.

Change log
==========

0.3.0

* New provider for the retrieval of data stored into Elastic Search nodes.
* New displayer for showing single value metrics.
* Added new displayer subtypes: bar (stacked), pie (3d, donut), line (smooth)
* Support for real-time dashboards. Displayer refresh settings.

* New data set editor UI module:
    - Creation of Bean, CSV & Elastic Search data set definitions
    - Data set retrieval testing and preview
    - Filter, sort and export the data previews

* Displayer editor data set lookup enhancements:
    - Filter editor for retrieving only a data subset.
    - Time frame function for the retrieval of time series data in real-time.
    - Different strategies for grouping time series data.
    - Ability to add/remove the columns/series to display.

0.2.0

* Data set definition files: Support for CSV & Bean generated data sets
* Displayer Editor widget for the creation of displayer definitions
* Perspective editor integration which allows the creation of dashboards by drag&drop

0.1.1

* Notify clients about data set registration/removal events
* Assign an HTML identifier to every Displayer instance (useful for testing purposes)

0.1.0

Main goal of this very first release is to make it possible the creation of
composite dashboards using an straightforward API. Feature set:

* Shared API for defining and registering data sets
* Shared operation engine for executing filter, group & sort operations over a data set
* Client API & widgets for defining Displayer instances
* Uberfire wrapper screen for the Displayer widget
* Showcase App. providing a built-in displayer gallery plus some dashboard samples
* Default renderer based on the Google Visualization library
* Additional table renderer based on the Uberfire PagedTable widget
* Tomcat 7 and JBoss AS 7 distributions provided

Build & run
===========

Prerequisites
-------------
* Git client
* Maven 3.x 
* Java 1.6+

First steps
-----------

Clone the project

    git clone git@github.com:dashbuilder/dashbuilder.git
    
Now you can build & run the project in development or production mode.     

Development mode
----------------

Development mode allows a user to develop with the framework by compiling classes and client assets on runtime, which decreases the development time. There are more implications such as browser compatibilities, language support, etc. It's useful for developing and testing the application.                     

Dashbuilder is currently built using GWT 2.7, so you are forced to use [SuperDevMode](http://www.gwtproject.org/articles/superdevmode.html) to run the application.

Super development mode is the new way to work in GWT since version <code>2.5</code> (Native support & the default mode in GWT <code>2.7</code>).
It works in most new browsers and it's based on [Source Map](https://docs.google.com/document/d/1U1RGAehQwRypUTovF1KRlpiOFze0b-_2gc6fAH0KY0k/edit?hl=en_US&pli=1&pli=1) spec. It's faster and more efficient than the old hosted mode. There are lots of benefits and other important reasons to use it, you can find more information [here](http://www.gwtproject.org/articles/superdevmode.html).             

Dashbuilder supports and it's configured by default to use SuperDevMode.                 

Using it means running two servers, one for the web application and one for the Code Server that compiles classes for SDM when the compile button is pushed on the web page or in the bookmark.

To build the application:

    cd dashbuilder
    mvn clean install -DskipTests

To run it:

    cd dashbuilder-webapp
    mvn gwt:run

Login:

    admin / admin


(If you are an IntelliJ fan, we also provide a setup for running the application under this fantastic IDE. Details [here](https://groups.google.com/forum/#!topic/dashbuilder-development/tRa6AAMb8fM))

Production mode
---------------

Production mode is used to build & package the application for a production environment. The application is compiled and the javascript assets are build using all permutations (browser support), all languages, etc.               

In order to build the production mode:

    cd dashbuilder
    mvn clean install -DskipTests -Dfull

Once build is finished, you'll find the WAR distributions for Wildfly and Tomcat into <code>dashbuilder/dashbuilder-distros/target/</code>.
   
Just deploy the WAR file into your application server!                          
