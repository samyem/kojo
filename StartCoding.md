Steps to start coding to add (small to begin with) features to Kojo:

  * Clone the Kojo repo (from the [Source](http://code.google.com/p/kojo/source/checkout) page)
  * [Build](http://code.google.com/p/kojo/source/browse/Build.txt) your local version of Kojo to make sure you're set to go
  * Fire up Netbeans, and open the Kojo project
  * To play with the Turtle stuff:
    * Head on over to [net.kogics.kojo.xscala.ScalaCodeRunner](http://code.google.com/p/kojo/source/browse/KojoEnv/src/net/kogics/kojo/xscala/ScalaCodeRunner.scala)
    * Find the Builtins object (see ScalaCodeRunnerBuiltins)
    * Start Hacking! Any method that you add to this object will be available within the Kojo Script Editor
  * To play with Staging:
    * Open up staging.scala (within net.kogics.kojo.staging)
    * Find the API object (see StagingModule)
    * Start Hacking! Any method that you add to this object will be available within the StagingModule (and accessible from the Script Editor)