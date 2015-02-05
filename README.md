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

    cd dashbuilder-webapp
    mvn gwt:run

Login

    admin / admin


(Git client, Maven 3.x and Java 1.6+ are required)



Change log
---------------------------


0.1.0

The main purpose of this very first release is to make it possible the creation of
composite dashboards using an straightforward API. Feature set:

* Shared API for defining and registering data sets
* Shared operation engine for executing filter, group & sort operations over a data set
* Client API & widgets for defining Displayer instances
* Uberfire wrapper screen for the Displayer widget
* Showcase App. providing a built-in displayer gallery plus some dashboard samples
* Default renderer based on the Google Visualization library
* Additional table renderer based on the Uberfire PagedTable widget
* Tomcat 7 and JBoss AS 7 distributions provided

0.1.1

* Notify clients about data set registration/removal events
* Assign an HTML identifier to every Displayer instance (useful for testing purposes)

0.2.0

* Data set definition files: Support for CSV & Bean generated data sets
* Displayer Editor widget for the creation of displayer definitions
* Perspective editor integration which allows the creation of dashboards by drag&drop

0.3.0

* SQL provider for the definition of data sets stored into external databases.
* Elastic Search integration for the retrieval of data stored into Elastic Search nodes.
* Support for real-time dashboards. Displayer refresh settings.
* Displayer editor data set lookup enhancements:
  - Filter editor for retrieving only a data subset.
  - Time frame function for the retrieval of time series data in real-time.
  - Different strategies for grouping time series data.
  - Ability to add/remove the columns/series to display.

