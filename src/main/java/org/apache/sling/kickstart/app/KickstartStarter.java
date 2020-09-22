/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.kickstart.app;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.feature.launcher.impl.Main;
import org.apache.sling.kickstart.control.ControlAction;
import org.apache.sling.kickstart.control.ControlListener;
import org.apache.sling.kickstart.control.ControlTarget;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(
    name = "java -jar <Sling Kickstart JAR File>",
    description = "Apache Sling Kickstart",
    footer = "Copyright(c) 2020 The Apache Software Foundation."
)
public class KickstartStarter implements Runnable, ControlTarget {

    private static final String LOG_CONFIGURATION_FM =
        "{\n" +
            "    \"id\":\"org.apache.sling:kickstart.config:slingosgifeature:kickstart-config:1\",\n" +
            "    \"configurations\":  {\n" +
            "      \"org.apache.sling.commons.log.LogManager\":    {\n" +
            "%s\n" +
            "      }\n" +
            "    }\n" +
            "  }";

    @Option(names = { "-s", "--mainFeature" }, description = "main feature file (file path or URL) replacing the provided Sling Feature File", required = false)
    private String mainFeatureFile;

    @Option(names = { "-m", "--nofar" }, description = "Do not use Sling FAR (if no Main Feature was provided) and use FM instead")
    private boolean nofar;

    @Option(names = { "-S", "--nofm" }, description = "Do not use Sling Feature Archive or Model file")
    private boolean nofm;

    @Option(names = { "-af", "--additionalFeature" }, description = "additional feature files", required = false)
    private List<String> additionalFeatureFile;

    @Option(names = { "-O", "--overrides" }, description = "Overrides in format <type>=<value>, type: C = artifact, CC = config, V = variable", required = false)
    private List<String> overrides;

    @Option(names = { "-j", "--control" }, description = "host and port to use for control connection in the format '[host:]port' (default 127.0.0.1:0)", required = false)
    private String controlAddress;

    @Option(names = { "-l", "--logLevel" }, description = "the initial loglevel (0..4, FATAL, ERROR, WARN, INFO, DEBUG)", required = false)
    private String logLevel;

    @Option(names = { "-f", "--logFile" }, description = "the log file, \"-\" for stdout (default logs/error.log)", required = false)
    private String logFile;

    @Option(names = { "-c", "--slingHome" }, description = "the sling context directory (default launcher)", required = false)
    private String slingHome;

    @Option(names = { "-a", "--address" }, description = "the interface to bind to (use 0.0.0.0 for any)", required = false)
    private String address;

    @Option(names = { "-p", "--port" }, description = "the port to listen to (default 8080)", required = false)
    private String port;

    @Option(names = { "-r", "--context" }, description = "the root servlet context path for the http service (default is /)", required = false)
    private String contextPath;

    @Option(names = { "-n", "--noShutdownHook" }, description = "don't install the shutdown hook")
    private boolean noShutdownHook;

    @Option(names = { "-v", "--verbose" }, description = "the feature launcher is verbose on launch", required = false)
    private boolean verbose;

    @Option(names = {"-D", "--define"}, description = "sets property n to value v. Make sure to use this option *after* the jar filename. " +
        "The JVM also has a -D option which has a different meaning", required = false)
    private Map<String, String> properties = new HashMap<>();

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display the usage message.")
    private boolean helpRequested;

    @Parameters(paramLabel = "COMMAND", description = "Optional Command for Server Instance Interaction, can be one of: 'start', 'stop', 'status' or 'threads'", arity = "0..1")
    private String command;

    /**
     * The configuration property setting the port on which the HTTP service
     * listens
     */
    private static final String PROP_PORT = "org.osgi.service.http.port";

    /**
     * The configuration property setting the context path where the HTTP service
     * mounts itself.
     */
    private static final String PROP_CONTEXT_PATH = "org.apache.felix.http.context_path";

    /**
     * Host name or IP Address of the interface to listen on.
     */
    private static final String PROP_HOST = "org.apache.felix.http.host";

    /** Path to default Sling Feature Model file **/
    private static final String DEFAULT_SLING_FEATURE_MODEL_FILE_PATH = "/default/org.apache.sling-12.json";

    /** Path to default Sling Feature Model feature archive **/
    private static final String DEFAULT_SLING_FEATURE_ARCHIVE_PATH = "/default/org.apache.sling.kickstart.far";

    private boolean started = false;

    @Override
    public void run() {
        try {
            URL mainFeatureURL = checkFeatureFile(mainFeatureFile);
            if(mainFeatureURL == null && !nofm) {
                if(nofar) {
                    mainFeatureURL = getClass().getResource(DEFAULT_SLING_FEATURE_MODEL_FILE_PATH);
                } else {
                    mainFeatureURL = getClass().getResource(DEFAULT_SLING_FEATURE_ARCHIVE_PATH);
                }
            }
            if(mainFeatureURL == null && (additionalFeatureFile == null || additionalFeatureFile.isEmpty())) {
                error("No Feature(s) Provided", null);
                return;
            }
            List<String> argumentList = new ArrayList<>();
            argumentList.add("-f");
            argumentList.add(mainFeatureURL.toString());
            if(additionalFeatureFile != null) {
                for (String additional : additionalFeatureFile) {
                    URL additionalURL = checkFeatureFile(additional);
                    if (additionalURL != null) {
                        argumentList.add("-f");
                        argumentList.add(additionalURL.toString());
                    }
                }
            }
            // Log File and Level must be configured through a Feature Model snippet and then
            // the configuration override must be set
            boolean hasLogLevel = StringUtils.isNotEmpty(logLevel);
            boolean hasLogFile = StringUtils.isNotEmpty(logFile);
            if(hasLogLevel || hasLogFile) {
                String addition = "";
                if(hasLogLevel) {
                    addition += "\"org.apache.sling.commons.log.level\":\"" + logLevel + "\"" + (hasLogFile ? ", " : "") + "\n";
                }
                if(hasLogFile) {
                    addition += "\"org.apache.sling.commons.log.file\":\"" + logFile + "\"\n";
                }
                String configFile = String.format(LOG_CONFIGURATION_FM, addition);
                // Write to temporary file and add link to arguments list including Configuration Class Override
                File temp = File.createTempFile("sling", ".json");
                FileWriter writer = new FileWriter(temp);
                writer.write(configFile);
                writer.close();
                URL tempUrl = temp.toURI().toURL();
                argumentList.add("-f");
                argumentList.add(tempUrl.toString());
                argumentList.add("-CC");
                argumentList.add("\"org.apache.sling.commons.log.LogManager=MERGE_LATEST\"");
            }
            if(overrides != null) {
                for (String override : overrides) {
                    int index = override.indexOf('=');
                    if (index > 0 && index < (override.length() - 1)) {
                        String type = override.substring(0, index);
                        String value = override.substring(index + 1);
                        //TODO: Check the type like 'CC' to avoid wrong types
                        argumentList.add('-' + type);
                        argumentList.add(value);
                    } else {
                        System.out.println("Wrong Override format: " + override + " -> ignored");
                    }
                }
            }
            if(StringUtils.isNotEmpty(port)) {
                addArgument(argumentList, PROP_PORT, port);
            }
            if(StringUtils.isNotEmpty(address)) {
                addArgument(argumentList, PROP_HOST, address);
            }
            if(StringUtils.isNotEmpty(contextPath)) {
                addArgument(argumentList, PROP_CONTEXT_PATH, contextPath);
            }
            if(StringUtils.isNotEmpty(slingHome)) {
                argumentList.add("-p");
                argumentList.add(slingHome);
                argumentList.add("-c");
                argumentList.add(slingHome + "/cache");
            }
            if(verbose) {
                argumentList.add("-v");
            }
            if(properties != null) {
                for(Entry<String, String> entry: properties.entrySet()) {
                    addArgument(argumentList, entry.getKey(), entry.getValue());
                }
            }
            System.out.println("Before Launching Feature Launcher, arguments: " + argumentList);
            // Now we have to handle any Start Option
            ControlAction controlAction = getControlAction(command);
            int answer = doControlAction(controlAction, controlAddress);
            if (answer >= 0) {
                doTerminateVM(answer);
                return;
            }

            // finally start Sling
            if (!doStart(argumentList)) {
                error("Failed to start Sling; terminating", null);
                doTerminateVM(1);
                return;
            }
        } catch(Throwable t) {
            System.out.println("Caught an Exception: " + t.getLocalizedMessage());
            t.printStackTrace();
        }
    }

    private void addArgument(List<String> list, String key, String value) {
        list.add("-D");
        list.add(key + "=" + value);
    }

    private URL checkFeatureFile(String featureFile) {
        URL answer = null;
        if(featureFile != null && !featureFile.isEmpty()) {
            try {
                URL check = new URL(featureFile);
                check.toURI();
                answer = check;
            } catch (MalformedURLException | URISyntaxException e) {
                // Try it as a file
                File check = new File(featureFile);
                if (!check.exists() || !check.canRead()) {
                    throw new RuntimeException("Given Feature File is not a valid URL or File: '" + featureFile + "'", e);
                }
                try {
                    answer = check.toURI().toURL();
                } catch (MalformedURLException ex) {
                    throw new RuntimeException("Given Feature File cannot be converted to an URL: '" + featureFile + "'", e);
                }
            }
        }
        return answer;
    }

    public static void main(String[] args) {
        CommandLine.run(new KickstartStarter(), args);
    }

    private int doControlAction(ControlAction controlAction, String controlAddress) {
        final ControlListener sl = new ControlListener(
            this,
            controlAddress
        );
        switch (controlAction) {
            case FOREGROUND:
                if (!sl.listen()) {
                    return -1;
                }
                break;
            case START:
                if (!sl.listen()) {
                    // assume service already running
                    return 0;
                }
                break;
            case STOP:
                return sl.shutdownServer();
            case STATUS:
                return sl.statusServer();
            case THREADS:
                return sl.dumpThreads();
        }
        return -1;
    }

    private boolean doStart(List<String> argumentList) {
        // prevent duplicate start
        if ( this.started) {
            info("Apache Sling has already been started", new Exception("Where did this come from"));
            return true;
        }

        info("Starting Apache Sling in " + slingHome, null);
        this.started = true;
        System.out.println("Start Command: '" + command + "'");
        try {
            Main.main(argumentList.toArray(new String[]{}));
        } catch(Error | RuntimeException e) {
            error("Launching Sling Feature failed", e);
            return false;
        }
        return true;
    }

    private ControlAction getControlAction(String command) {
        ControlAction answer = ControlAction.FOREGROUND;
        try {
            answer = ControlAction.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Given Control Action is not valid: '" + command.toUpperCase() + "'");
        } catch (NullPointerException e) {
            // Ignore as we set the default to FOREGROUND anyhow
        }
        return answer;
    }

    @Override
    public String getHome() {
        return slingHome;
    }

    @Override
    public void doStop() {
        info("Stop Application", null);
        System.exit(0);
    }

    @Override
    public void doTerminateVM(int status) {
        info("Terminate VM, status: " + status, null);
        System.exit(status);
    }

    @Override
    public void info(String message, Throwable t) {
        System.out.println(message);
        if(t != null) {
            t.printStackTrace();
        }
    }

    @Override
    public void error(String message, Throwable t) {
        System.err.println(message);
        if(t != null) {
            t.printStackTrace(System.err);
        }
    }
}
