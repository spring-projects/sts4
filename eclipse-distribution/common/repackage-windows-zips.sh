#!/bin/sh
#We are assuming this script will be called by bamboo from the root of the git repo
set -ev
echo "Repackaging Windows Distribution Zip files as self extracting jars..."
cd eclipse-distribution
echo pwd = $(pwd)
find . -name "*.zip"
for zipfile in $(find $(pwd) -name "*.win32.x86_64.zip"); do
    java -jar common/self-extracting-jar-creator.jar "$zipfile"
done
echo "Repackaging Windows Distribution Zip files DONE"
