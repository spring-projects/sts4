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

cat properties.json