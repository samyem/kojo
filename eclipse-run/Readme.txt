For setting up Kojo with Eclipse, do the following:
- run devrun.sh (in the Kojo root dir)
- rename dist to dist-for-eclipse
- copy the files in this directory to dist-for-eclipse/kojo/bin
- import the Kojo Eclipse project into Eclipse (with the Scala plugin/IDE already installed. Scala-IDE version 2.1 M1 or later is recommended)

Now you can work on the Kojo codebase and compile it from within Eclipse.

To run Kojo, set up an external tool to run the dynkojo script located in the dist-for-eclipse/kojo/bin dir, with dist-for-eclipse/kojo/bin as the working dir.

To debug Kojo, set up an external tool to run the dynkojo-debug script located in the dist-for-eclipse/kojo/bin dir, with dist-for-eclipse/kojo/bin as the working dir.

Now just hack away on the code, save to compile (takes 1 to 10 secs), and click on the dynkojo external tool to run (takes a few secs).

To debug Kojo, click on the dynkojo-debug external tool to run in debug mode, and attach to Kojo with the Eclipse remote debugger.

You can also run unit tests instantaneously (this is a huge benefit). Just right click on a test and run it. Or debug it!
