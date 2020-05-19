#!/bin/sh -ex

# The name of the genrated segment store is not matching the libs segmentstore as the role is added to its name
# so we create a symoblic link here
cd sling/sling-composite/repository-libs
ln -s segmentstore segmentstore-composite-mount-libs
cd ../../..

java -jar target/org.apache.sling.kickstart-0.0.3-SNAPSHOT.jar -af src/main/resources/feature-two-headed-runtime.json -c sling/sling-composite

