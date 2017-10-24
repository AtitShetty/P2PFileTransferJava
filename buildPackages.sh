#!/bin/bash

cd peer
echo "\n\n\nBuild peer project\n\n\n"
mvn package
cd ..
cd central
echo "\n\n\nBuild central project\n\n\n"
mvn package