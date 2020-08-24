[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

&#32;[![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-kickstart/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-kickstart/job/master/)&#32;[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-kickstart&metric=coverage)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-kickstart)&#32;[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-kickstart&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-kickstart)&#32;[![JavaDoc](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.kickstart.svg)](https://www.javadoc.io/doc/org.apache.sling/org-apache-sling-kickstart)&#32;[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.kickstart/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.kickstart%22) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Sling Kickstart Launcher

This project is the Feature Model based version of the **sling-org-apache-sling-starter**
module and creates an executable JAR file (for now).
It is also a test case for the Sling Kickstart Maven Plugin as it uses it
to launch a Launchpad Ready Rule and Smoke tests.
The Kickstart is using an embedded Sling Feature Archive to launch Sling and so the
Kicstart can be started w/o having access to the internet.

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
mvn -f sling-fm-pom.xml clean package -Dsling.starter.folder=<path to the sling starter folder> -P create-far
```
3. Copy the Sling12 Feature File: **target/slingfeature-tmp/feature-sling12.json** into the
**src/main/resources/standalone/fm** folder replacing the old one
4. Copy the Sling12 Feature File: **target/org.apache.sling.kickstart-\*.far** into the
**src/main/resources/standalone/far** folder replacing the old one by renaming it to
**org.apache.sling.kickstart.far**.
5. Build the Kickstart project (see above in **Build**) and then run it (see below in **Usage**)

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
Usage: java -jar <Sling Kickstart JAR File> [-hmnSv] [-a=<address>]
                                            [-c=<slingHome>] [-f=<logFile>]
                                            [-j=<controlAddress>]
                                            [-l=<logLevel>] [-p=<port>]
                                            [-r=<contextPath>]
                                            [-s=<mainFeatureFile>]
                                            [-af=<additionalFeatureFile>]...
                                            [-D=<String=String>]...
                                            [-O=<overrides>]... [COMMAND]
Apache Sling Kickstart
      [COMMAND]             Optional Command for Server Instance Interaction, can be
                              one of: 'start', 'stop', 'status' or 'threads'
  -a, --address=<address>   the interface to bind to (use 0.0.0.0 for any)
      -af, --additionalFeature=<additionalFeatureFile>
                            additional feature files
  -c, --slingHome=<slingHome>
                            the sling context directory (default launcher)
  -D, --define=<String=String>
                            sets property n to value v. Make sure to use this option
                              *after* the jar filename. The JVM also has a -D option
                              which has a different meaning
  -f, --logFile=<logFile>   the log file, "-" for stdout (default logs/error.log)
  -h, --help                Display the usage message.
  -j, --control=<controlAddress>
                            host and port to use for control connection in the
                              format '[host:]port' (default 127.0.0.1:0)
  -l, --logLevel=<logLevel> the initial loglevel (0..4, FATAL, ERROR, WARN, INFO,
                              DEBUG)
  -m, --nofar               Do not use Sling FAR (if no Main Feature was provided)
                              and use FM instead
  -n, --noShutdownHook      don't install the shutdown hook
  -O, --overrides=<overrides>
                            Overrides in format <type>=<value>, type: C = artifact,
                              CC = config, V = variable
  -p, --port=<port>         the port to listen to (default 8080)
  -r, --context=<contextPath>
                            the root servlet context path for the http service
                              (default is /)
  -s, --mainFeature=<mainFeatureFile>
                            main feature file (file path or URL) replacing the
                              provided Sling Feature File
  -S, --nofm                Do not use Sling Feature Archive or Model file
  -v, --verbose             the feature launcher is verbose on launch
Copyright(c) 2020 The Apache Software Foundation.
```

These are four additional parameters:

**-s**: this takes a path to a Feature Model (FM) that replaces the provided
Sling Feature Module. With it it is possible to provide your own Sling FM
which may or may not contain your own project FMs.

**-af**: each parameter will have a path to a Feature Model (FM) that is
added to the provided Sling FM (or its override). To add multiple FMs just
use multiple *-af* parameter lines.

**-m/--nofar**: do not use the embedded Sling Feature Archive and use the
plain Sling Feature Model. This will greatly increase the launch performance
if most of the dependencies are already in the local Maven repository.

**-S/--nofm**: do not use the embedded Sling Feature Archive or Model file.
If no other Feature Module file is provided this launch aborts.

### Composite Node Store

The Kickstart project also comes with the Feature Models and the scripts to run
a Sling Composite Node Store as well having the option to upgrade a Sling instance
afterwards.

#### Feature Models

The Sling Feature Model of the Composite Node Store needs to have the Default Node
Store removed which is available here: **src/main/resources/feature-sling12-two-headed.json**.
Then we have to additional Feature Models:
* feature-two-headed-seed.json: The setup to seed the initial **Libs** node store
which will be later the **read-only** node store
* feature-two-headed-runtime.json: This will create the Sling Instance with the composite
node store

#### Scripts

There are two sets of two scripts available in the **bin** folder:

* create_seed_fm.sh: creates the Seed Sling instance
* run_composite_fm.sh: launches the Sling instance with a composite node store where **libs**
is read-only
* create_updated_seed_fm.sh: creates an update Seed Sling Instance
* run_updated_composite_fm.sh: launches the Sling Instance with a composite node store
which the updates available

**Note**: the first two scripts support optionally additional features. Just add the path to the
feature model file or feature archive as parameters to the script and they will be added when
launched. It is important that both scripts get the same arguments, though.
The last two scripts requires at least one additional feature as an update only makes sense
when there is something to be updated.

Example:
```
    ./bin/create_seed_fm.sh my-project-fm.json another-project.json
    ./bin/run_composite_fm.sh my-project-fm.json another-project.json
```
