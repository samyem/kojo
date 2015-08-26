  * Make sure that you can [build Kojo and make simple additions to the Kojo API](StartCoding.md).
  * Add the existing library jars to the Kojo Netbeans project:
    * Open up the Kojo Netbeans project
    * Make sure that the KojoEnv module is open
    * Right-click on the KojoEnv module
    * Go to Properties -> Libraries
    * Click on _Add New Library_, and go through the wizard steps to add the new jars to the Kojo project. The KojoEnv module can now use the new jars
  * Create a new package within KojoEnv for the new functionality that you are adding
  * Create an API object within this new package. This will be the API for your new module (look at the Staging module for an example of this approach).
  * Implement your API with the help of the library that you are integrating
  * Make your API object available to Kojo by adding something like the following code within [ScalaCodeRunner](http://code.google.com/p/kojo/source/browse/KojoEnv/src/net/kogics/kojo/xscala/ScalaCodeRunner.scala):
```
    interp.interpret("val NewApi = net.kogics.kojo.newpackage.API")
```
  * Make sure you write some tests for your code!