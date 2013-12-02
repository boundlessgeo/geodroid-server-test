geodroid-server-testing
=======================

Requirements
------------

Android SDK
Maven
Geodroid Server installed to device

Setup
-----

No specific setup is needed unless geodroid-server defaults have been changed.

To run the tests, only the `ANDROID_HOME` environment variable is needed. This should point to the `sdk` directory.

Advanced Setup
--------------

The defaults can be modified using the `test.properties` file. This will be located in the working directory.

The following properties can be specified if needed:

* baseURI - network URL to find running server at, for example: http://192.168.0.15
* port - if running the server on a different port. defaults to 8000
* adbDevice - if more than one device is connected to adb, you must specify the device. The first column of output from `adb devices -l` is the device identifier.
* installData - set to false to skip installation of test data (only useful for speeding up read-only tests)

Test Execution
--------------

The test suite will download the fixture data and copy it to the device. The suite will also ensure the service is running.

Running Tests via Maven
-----------------------

Assuming the ANDROID_HOME environment variable is set:

  mvn test
  
Building Executable Jar
-----------------------

`mvn install -DskipTests assembly:single`

The `-DskipTests` property is required to compile the tests without running 
them. The above command will create a single executable jar w/ all dependencies 
in the `target` directory. 

Running The Executable Jar
--------------------------

`java -jar geodroid-server-testing-0-SNAPSHOT-test-jar-with-dependencies.jar`

Test Output
-----------

Whether running via maven or the single jar, the test output should be at `target/report.xml`. This can be opened in a browser. The associated `report.css` is required, too.

Test output should include for each test:

* test name
* HTTP method and URL
* response (if applicable and not too big)
* request body (if applicable)
* execution time

Failed tests will have their failure message in a red-bordered box.
