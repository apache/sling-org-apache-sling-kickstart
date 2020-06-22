#!/bin/sh -e

echo "-----------------------------------"
echo "| PLEASE SHUTDOWN SLING MANUALLY  |"
echo "| AFTER STARTUP IS COMPLETE       |"
echo "-----------------------------------"

set +x
sleep 3

DIR=`pwd`
FOLDER=`basename $DIR`
if [ "$FOLDER" == "bin" ]; then
  DIR=`cd ..;pwd`
fi
cd ${DIR}

rm -rf launcher
rm -rf sling

additionalFeatures=""
for features in "$@"; do
  additionalFeatures="${additionalFeatures}-af ${features} "
done

java -jar \
    target/org.apache.sling.kickstart-0.0.4.jar \
    -s src/main/resources/composite-nodes/feature-sling12-two-headed.json \
    -af src/main/resources/composite-nodes/feature-two-headed-seed.json \
    ${additionalFeatures} \
    -c sling/sling-composite
