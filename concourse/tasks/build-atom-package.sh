#!/bin/bash
set -e
set -v
workdir=`pwd`

url=`cat fatjar/url`

echo "The url of the fatjar is ${url}"

ls -la

cd package_sources

npm install ../sts4/atom-extensions/atom-commons

cat > properties.json << EOF
{
    jarUrl: ${url}
}
EOF

npm install

npm install bundle-deps

node ./node_modules/bundle-deps/bundle-deps .

npm pack

timestamp=`date -u +%Y%m%d%H%M`
for i in `ls *.tgz`; do
    basename=$(basename $i)
    cp $i $output/${basename/SNAPSHOT/$timestamp}
done

ls -la $output