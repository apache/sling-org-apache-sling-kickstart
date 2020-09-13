#!/bin/sh -ex

DIR=`pwd`
FOLDER=`basename $DIR`
if [ "$FOLDER" == "bin" ]; then
  DIR=`cd ..;pwd`
fi
cd ${DIR}

rm -rf launcher

# The name of the generated segment store is not matching the libs segment store as the role is added to its name
# so we create a symbolic link here
cd sling/sling-composite/repository-libs
ln -s segmentstore segmentstore-composite-mount-libs
cd ../../..

additionalFeatures=""
for features in "$@"; do
  additionalFeatures="${additionalFeatures}-af ${features} "
done

java -jar \
    target/org.apache.sling.kickstart-0.0.8.jar \
    -s src/main/resources/composite-nodes/feature-sling12-two-headed.json \
    -af src/main/resources/composite-nodes/feature-two-headed-runtime.json \
    ${additionalFeatures} \
    -c sling/sling-composite

