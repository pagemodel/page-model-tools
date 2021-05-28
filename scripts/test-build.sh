#!/bin/bash -ex

cd "$(dirname "$0")"
rm -rf pmt-test XYZTests

git clone https://github.com/pagemodel/page-model-tools.git pmt-test
cd pmt-test/org.pagemodel.gen.gradle
../gradlew --rerun-tasks clean build publishToMavenLocal --console=plain
cd ..

./gradlew --rerun-tasks clean build publishToMavenLocal -x test --parallel --console=plain
cd ..

cp ./pmt-test/org.pagemodel.gen.project/build/libs/org.pagemodel.gen.project-0.8.1-SNAPSHOT.jar .
java -jar org.pagemodel.gen.project-0.8.1-SNAPSHOT.jar XYZ com.example.xyz.test ./XYZTests/
cd XYZTests/
./gradlew --rerun-tasks build -x test --console=plain
cd ..

cd pmt-test/
docker build -f "scripts/docker/pagemodel-headless-chrome.dockerfile" -t pagemodel-headless-chrome:0.8.1 .
cd ../XYZTests/
docker build -f "scripts/docker/xyz-headless-chrome.dockerfile" -t xyz-headless-chrome:1.0.0 .
docker run --rm -ti -u seluser:seluser -v $(pwd):/home/seluser/dev:rw,delegated -w /home/seluser/dev xyz-headless-chrome:1.0.0 ./gradlew --rerun-tasks test --console=plain -Dbrowser=headless

# cd pmt-test/
# ./scripts/docker/build-docker.sh
# cd ../XYZTests/
# ./scripts/docker/build-docker.sh
# ./scripts/dtest ./gradlew --rerun-tasks test -Dbrowser=headless --console=plain


# cd "$(dirname "$0")/../org.pagemodel.gen.gradle"
# ../gradlew --rerun-tasks clean build publishToMavenLocal --console=plain
# cd ..
# ./gradlew --rerun-tasks clean build publishToMavenLocal -x test --parallel --console=plain
# cd ..
# cp page-model-tools/org.pagemodel.gen.project/build/libs/org.pagemodel.gen.project-0.8.1-SNAPSHOT.jar .
# java -jar org.pagemodel.gen.project-0.8.1-SNAPSHOT.jar XYZ com.example.xyz.test ./XYZTests/
# cd XYZTests/
# ./gradlew --rerun-tasks build test --console=plain
# cd ..


echo "Complete: $SECONDS seconds"
