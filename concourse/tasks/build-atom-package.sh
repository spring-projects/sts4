#!/bin/bash
set -e
set -v
workdir=`pwd`
output=$workdir/out
#atom_commons=$workdir/sts4/atom-extensions/atom-commons
atom_package=$workdir/sts4/atom-extensions/$package_name

if [ -e $fatjar ]; then
    url=`cat fatjar/url`
    fatjar_version=`cat fatjar/version`
fi

#cd $atom_commons
#npm install

cd $atom_package

#npm install ${atom_commons}

timestamp=`date -u +%Y%m%d%H%M`

if [ -e $fatjar ]; then
cat > properties.json << EOF
{
    "jarUrl": "${url}"
}
EOF
else
cat > properties.json << EOF
{
    "timestamp": "${timestamp}"
}
EOF
fi

npm install

# push code to release repository

cd $workdir
mkdir -p out/repo
# enable hidden files for * matcher
shopt -s dotglob
cp -a $atom_package/* out/repo
cp -a release_repo/.git out/repo/.git
# disable hidden files for * matcher
shopt -u dotglob
cp $atom_package/.gitignore-release out/repo/.gitignore
rm -f out/repo/.gitignore-release

cd out/repo

git config user.email "aboyko@pivotal.io"
git config user.name "Alex Boyko"

git add .

if [ -e $fatjar ]; then
    git commit \
        -m "Publish $fatjar_version"
else
    git commit \
        -m "Publish $timestamp"
fi

# Publish linkable artifact to S3

cd $atom_package
npm install bundle-deps

node ./node_modules/bundle-deps/bundle-deps .

basename=$(npm pack | tee /dev/tty)

length=${#basename}

newName=${basename:0:${length}-4}-$timestamp${basename:${length}-4:${length}}

cp ${basename} $output/${newName}
