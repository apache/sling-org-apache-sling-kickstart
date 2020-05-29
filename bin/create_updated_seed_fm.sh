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
rm -rf sling/sling-composite/repository-libs

additionalFeatures=""
for features in "$@"; do
  additionalFeatures="${additionalFeatures}-af ${features} "
done

if [ "x${additionalFeatures}x" == "xx" ]; then
    echo "For an Update a least one Feature needs to be added"
    return
fi

java -jar \
    target/org.apache.sling.kickstart-0.0.3-SNAPSHOT.jar \
    -s src/main/resources/feature-sling12-two-headed.json \
    -af src/main/resources/feature-two-headed-seed.json \
    ${additionalFeatures} \
    -c sling/sling-composite
