Deployment onto JBoss Widlfly 8.X
=================================

Please follow the next steps in order to deploy the application.           

Deploy
------

Run your JBoss Wildfly instance using the `full` server profile as:         

    $JBOSS_HOME/bin/standalone.sh --server-config=standalone-full.xml

Once server is up and running, get the proper WAR file (e.g. `dashbuilder-<version>-wildfly8.war`) and execute the following command to deploy the application into your JBoss Wildfly instance:              

    $ cd $JBOSS_HOME/bin
    $ ./jboss-cli.sh --connect --command="deploy <path_to_war_file>"
    
    NOTES:
        - <path_to_war_file>: is the local path to the application war file.
        - e.g. $ ./jboss-cli.sh --connect --command="deploy /home/myuser/myfiles/dashbuilder-0.4.0-SNAPSHOT-wildfly8.war" )


Datasources
-----------

The application is configured to use the default Wildfly8 data source. It binds to the following JNDI URL: `java:jboss/datasources/ExampleDS`.                             

Notice that this data source is intended for development/demo purposes, it's configured to use an H2 database using in-memory storage,  and it's present by default at any JBoss Wildfly installation.               

If you want to deploy on a database different from H2 like Oracle, MySQL, Postgres or MS SQL Server please follow the next steps:               

**1.- Install the database driver on the JBoss Wildfly server (read the JBoss documentation)**                              

Considering a PostgreSQL 9.3 database, the filesystem structure for adding a postgres 9.3 driver module for jdbc4 can be as:                       
    
        <JBOSS_HOME>/modules/system/layers/base/
                                                org/
                                                    postgres/
                                                            main/
                                                                 postgresql-9.3-1103.jdbc4.jar
                                                                 module.xml
The content for the `module.xml` can be as:                   

        <?xml version="1.0" encoding="UTF-8"?>  
        
        <!--
          ~ JBoss, Home of Professional Open Source.  
          ~ Copyright 2010, Red Hat, Inc., and individual contributors  
          ~ as indicated by the @author tags. See the copyright.txt file in the  
          ~ distribution for a full listing of individual contributors.  
          ~  
          ~ This is free software; you can redistribute it and/or modify it  
          ~ under the terms of the GNU Lesser General Public License as  
          ~ published by the Free Software Foundation; either version 2.1 of  
          ~ the License, or (at your option) any later version.  
          ~  
          ~ This software is distributed in the hope that it will be useful,  
          ~ but WITHOUT ANY WARRANTY; without even the implied warranty of  
          ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
          ~ Lesser General Public License for more details.  
          ~  
          ~ You should have received a copy of the GNU Lesser General Public  
          ~ License along with this software; if not, write to the Free  
          ~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
          ~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.  
          -->
        
          <module xmlns="urn:jboss:module:1.3" name="org.postgres">
          <resources>
        	<resource-root path="postgresql-9.3-1103.jdbc4.jar"/>
          </resources>
          <dependencies>
        	<module name="javax.api"/>
        	<module name="javax.transaction.api"/>
          </dependencies>
        </module>

**2.- Create an empty database & users**                        

Considering the PostgreSQL example above, run the `psql` command and type:                

        postgres=# CREATE USER myuser WITH PASSWORD 'myuser'; 
        postgres=# CREATE DATABASE dashbuilder;
        postgres=# GRANT ALL PRIVILEGES ON DATABASE dashbuilder to myuser;
        
Check you can connect successfully using the above user to the recently created database:                 

        [root@host ~]# psql -U myuser -d dashbuilder -W

**IMPORTANT NOTE**: If you have permission issues trying to connect the database from the Dashboard Builder, please configure your `pg_hba.conf` for allowing local TCP connections.              

**3.- Create a JBoss data source**                          

By default, the dashbuilder WAR file for JBoss Wildfly 8.X uses the default wildfly data source named `ExampleDS`.              

The easiest way is just re-using the `ExampleDS` but configuring it the PostgreSQL connection, in that case, edit your `standalone-full.xml` and modify the default data source named `ExampleDS` as:                       

        <datasources>
            <datasource jndi-name="java:jboss/datasources/ExampleDS" pool-name="ExampleDS" enabled="true" use-java-context="true">
                <connection-url>jdbc:postgresql://localhost:5432/dashbuilder</connection-url>
                <driver>postgres</driver>
                <security>
                <user-name>myuser</user-name>
                <password>myuser</password>
                </security>
            </datasource>
            <drivers>
                <driver name="postgres" module="org.postgres">
                    <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
                </driver>
            </drivers>
        </datasources>
  
Another option is to create a new data source for the PostgreSQL database, if you choose this option, you have to specify the data source to use in the dashbuilder application (instead of the `ExampleDS` one used by default):                      
So you have to modify the file `dashbuilder/dashbuilder-distros/src/main/wildfly8/WEB-INF/jboss-web.xml` as:

        <jboss-web>
           <context-root>/dashbuilder</context-root>
           <resource-ref>
               <res-ref-name>jdbc/dashbuilder</res-ref-name>
               <res-type>javax.sql.DataSource</res-type>
               <jndi-name>java:jboss/datasources/myDataSource</jndi-name>
           </resource-ref>
           ...

   Replace the *jndi-name* parameter value by the JNDI path of the JBoss data source you've just created.

User Authentication
--------------------

Once started, open a browser and type the following URL:          
        
        http://localhost:8080/dashbuilder        # A login screen should be displayed.

However, some extra configuration is needed before you can sign in:               

* The application is based on the J2EE container managed authentication  mechanism.
This means that the login itself is delegated to the application server.

* To create users and define the roles use the command line utility provided by JBoss Wildfly at `$JBOSS_HOME/bin/add-user.sh`.                  

* The application roles are defined at [web.xml](./WEB-INF/web.xml) file.
Roles can be used to create access profiles and define custom authorization policies.
There exist a single application role named `admin`. In order to use the application, create a user with role `admin`.               

* The application uses the JBoss' default security domain as you can see [here](./WEB-INF/jboss-web.xml).                
Alternatively, you can define your own security domain and use, for instance, an LDAP, a database, or whatever mechanism you want to use as your credential storage.            
There are plenty of examples in the JBoss Wildfly documentation about.

Feel free to change any settings regarding the application security and, once finished, to generate a distribution war that fits your needs.          

File System provider
---------------------
Dashbuilder stores all the internal artifacts (such as the data set definition files, the uploaded files, etc) into a GIT repository. You can clone the repository and noddle around with it if you need to.                

By default, the GIT repository is created when the application starts for first time at `$WORKING_DIR/.niogit`, considering `$WORKING_DIR` as the current directory where the application server is started.            

You can specify a custom repository location by setting the following Java system property to your target file system directory:                   
 
        -Dorg.uberfire.nio.git.dir=/home/youruser/some/path
        
If necessary you can make GIT repositories available from outside localhost using the following Java system property:                 
 
        -org.uberfire.nio.git.ssh.host=0.0.0.0
        
You can set this Java system properties permanent by adding the following lines in your `standalone-full.xml` file as:                
 
        <system-properties>
          <!-- Custom repository location. -->
          <property name="org.uberfire.nio.git.dir" value="/home/youruser/some/path"/>
          <!-- Make GIT repositories available from outside localhost. -->
          <property name="org.uberfire.nio.git.ssh.host" value="0.0.0.0"/>
        </system-properties>
        
