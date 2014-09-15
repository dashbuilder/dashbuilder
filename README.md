Dashbuilder
===========

Dashboard composition tooling based on the Uberfire framework
 
Steps to build & run
---------------------------
 
Clone the project

    git clone git@github.com:dashbuilder/dashbuilder.git
    
Build the project

    cd dashbuilder
    mvn clean install -DskipTests

Run the app

    cd dashbuilder-showcase/dashbuilder-webapp
    mvn gwt:run

Login

    admin / admin


(Git client, Maven 3.x and Java 1.6+ are required)



Change log
---------------------------


0.1.0 Final

The main purpose of this very first release is to make it possible the creation of
composite dashboards using a easy to use API. Feature set:

* Shared API for defining and registering data sets
* Shared operation engine for executing filter, group & sort operations over a data set
* Client API & widgets for defining Displayer instances
* Uberfire wrapper screen for the Displayer widget
* Showcase providing a built-in displayer gallery plus some dashboard samples
* Default renderer based on the Google Visualization library
* Additional table renderer based on the KIE PagedTable widget
* Tomcat 7 and JBoss AS 7 distributions provided

0.1.1 Final

* Notify clients about data set registration/removal events
* Assign an HTML identifier to every Displayer instance (useful for testing purposes)

