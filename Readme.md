# Sling Kickstart Launcher

This project is the Feature Model based version of the **sling-org-apache-sling-starter**
module and creates an executable JAR file (for now).
It is also a test case for the Sling Kickstart Maven Plugin as it uses it
to launch a Launchpad Ready Rule and Smoke tests.

## Build

This plugin depends on the **Sling Kickstart Maven Plugin** which is then used to run the IT tests:

1. Go to **sling-kickstart-maven-plugin** module
2. Build with: `mvn clean install`
3. Go back to **sling-org-apache-sling-kickstart**
4. Build and Launch it with: `mvn clean install`
5. Sling will come up and run the IT tests and then shut down. Sling can be
   kept running after the end of the IT tests by providing the property
   **block.sling.at.the.end** with the value **true**

## Update to the Latest Sling

This project ships with a Sling 12 Feature Model that is pretty soon outdated
and will only be updated on the next release.
That said this project contains the means to update that file if you need to do so:

1. Checkout the **Sling Starter Project** (sling-org-apache-sling-starter
2. Run the **sling-fm-pom.xml** build with the sling starter path as property
```
mvn -f sling-fm-pom.xml clean package -Dsling.starter.folder=<path to the sling starter folder>
```
3. Copy the Sling12 Feature File: **target/slingfeature-tmp/feature-sling12.json** into the
**src/main/resources** folder replacing the old one
4. Build the Kickstart project (see above in **Build**) and then run it (see below in **Usage**)

## Usage

After the resulting jar file **org.apache.sling.kickstart--<version>.jar**
can be executed with:
```
java -jar org.apache.sling.kickstart--<version>.jar ...
```

### Running as a Service / Background

The kickstart can be started in the background as a service with the 'start'
command but the starter needs to be put into the background:
```
java -jar org.apache.sling.kickstart--<version>.jar start <optional options> &
```

The status of the service then can be retrieved by using the 'status' command:
```
java -jar org.apache.sling.kickstart--<version>.jar status
```

To shutdown the service use the 'stop' command:
```
java -jar org.apache.sling.kickstart--<version>.jar stop
```

### Parameters

The parameters of the Kickstarter is beside **-af, -s** are the same as
the for the Sling Starater. To checkout the usage of the parameters you
can use the **help** parameter when starting the kickstarter JAR file:

```
java -jar org.apache.sling.kickstart-0.0.1-SNAPSHOT.jar -h
Usage: java -jar <Sling Kickstarter JAR File> [-hnv] [-a=<address>]
                                              [-c=<slingHome>] [-f=<logFile>]
                                              [-i=<launcherHome>]
                                              [-j=<controlAddress>]
                                              [-l=<logLevel>] [-p=<port>]
                                              [-r=<contextPath>]
                                              [-s=<mainFeatureFile>]
                                              [-af=<additionalFeatureFile>]...
                                              [-D=<String=String>]... [COMMAND]
Apache Sling Kickstart
      [COMMAND]             Optional Command for Server Instance Interaction, can be
                              one of: 'start', 'stop', 'status' or 'threads'
  -a, --address=<address>   the interface to bind to (use 0.0.0.0 for any)
      -af, --additionalFeature=<additionalFeatureFile>
                            additional feature files
  -c, --slingHome=<slingHome>
                            the sling context directory (default sling)
  -D, --define=<String=String>
                            sets property n to value v. Make sure to use this option
                              *after* the jar filename. The JVM also has a -D option
                              which has a different meaning
  -f, --logFile=<logFile>   the log file, "-" for stdout (default logs/error.log)
  -h, --help                Display the usage message.
  -i, --launcherHome=<launcherHome>
                            the launcher home directory (default launcher)
  -j, --control=<controlAddress>
                            host and port to use for control connection in the
                              format '[host:]port' (default 127.0.0.1:0)
  -l, --logLevel=<logLevel> the initial loglevel (0..4, FATAL, ERROR, WARN, INFO,
                              DEBUG)
  -n, --noShutdownHook      don't install the shutdown hook
  -p, --port=<port>         the port to listen to (default 8080)
  -r, --context=<contextPath>
                            the root servlet context path for the http service
                              (default is /)
  -s, --mainFeature=<mainFeatureFile>
                            main feature file (file path or URL) replacing the
                              provided Sling Feature File
  -v, --verbose             the feature launcher is verbose on launch
Copyright(c) 2020 The Apache Software Foundation.
```

These are two additional parameters:

**-s**: this takes a path to a Feature Model (FM) that replaces the provided
Sling Feature Module. With it it is possible to provide your own Sling FM
which may or may not contain your own project FMs.

**-af**: each parameter will have a path to a Feature Model (FM) that is
added to the provided Sling FM (or its override). To add multiple FMs just
use multiple *-af* parameter lines.