#!/bin/bash

cd peer
echo -e "\n\n\nBuilding peer server project"
mvn package
cd ..
cd central
echo -e "\n\n\nBuilding central server project"
mvn package
cd ..
pwd

echo -e "\n\n\nCreating Test Directory"
rm -rf Testing
mkdir -p Testing/central/RFC

for number in {1..6}
do
mkdir -p Testing/peer$number/RFC
done

echo -e "\n\n\nCopying central and peers jar into testing directory"
cp central/target/central-0.0.1-jar-with-dependencies.jar Testing/central

for number in {1..6}
do
cp peer/target/peer-0.0.1-jar-with-dependencies.jar Testing/peer$number
done

exit 0