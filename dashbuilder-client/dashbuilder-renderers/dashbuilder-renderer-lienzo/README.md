Lienzo renderer for Dashbuilder
===============================

This renderer is based on Lienzo 2.0 charting library.                 

For more info about lienzo go [here](http://www.lienzo-core.com/lienzo-ks/#WELCOME) or take a look at the [Lienzo Wiki](https://github.com/ahome-it/lienzo-core/wiki).                  

Note that Lienzo charting library is still under hard development and testing... so be patient.              

Building the module
-------------------
 
This module depends on <code>com.ahome-it.lienzo-charts</code> maven artifact, which depends on <code>com.ahome-it.lienzo-core</code> as well.            

So the steps for building this dasbuilder-lienzo-renderer module are:             

1.- Clone the [lienzo-core](https://github.com/romartin/lienzo-core) and [lienzo-charting](https://github.com/romartin/lienzo-charts) libraries.

2.- Built both modules previously cloned by running on each module root:
        
        mvn clean install -DskipTests
        
3.- Once lienzo artifacts available in your local maven repository, just perform this module build:

        mvn clean install -DskipTests

