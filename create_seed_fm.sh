#!/bin/sh -e

echo "-----------------------------------"
echo "| PLEASE SHUTDOWN SLING MANUALLY  |"
echo "| AFTER STARTUP IS COMPLETE       |"
echo "-----------------------------------"

set +x
sleep 3

rm -rf launcher
rm -rf sling
java -jar target/org.apache.sling.kickstart-0.0.3-SNAPSHOT.jar -af src/main/resources/feature-two-headed-seed.json -c sling/sling-composite

