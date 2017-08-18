#!/bin/bash
set -e
set -v
workdir=`pwd`
output=$workdir/out
atom_commons=$workdir/sts4/atom-extensions/atom-commons
atom_package=$workdir/package_sources

url=`cat fatjar/url`
fatjar_version=`cat fatjar/version`
package_version=${fatjar_version%-*}

cd $atom_commons
npm --no-git-tag-version version $package_version
npm install

cd $atom_package

npm install ${atom_commons}

cat > properties.json << EOF
{
    "jarUrl": "${url}"
}
EOF

npm install

npm install bundle-deps

node ./node_modules/bundle-deps/bundle-deps .

basename=$(npm pack | tee /dev/tty)

timestamp=`date -u +%Y%m%d%H%M`

length=${#basename}

newName=${basename:0:${length}-4}-$timestamp${basename:${length}-4:${length}}

cp ${basename} $output/${newName}

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git add .

git commit \
    -m "Use fatjar version ${fatjar_version}"

git clone $atom_package $workdir/out/repo
