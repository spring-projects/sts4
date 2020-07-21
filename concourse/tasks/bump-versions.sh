#!/bin/bash
set -e
set -v
workdir=`pwd`

vscode_sources=$workdir/sts4/vscode-extensions
atom_sources=$workdir/sts4/atom-extensions
theia_sources=$workdir/sts4/theia-extensions

version=`cat version/version`
echo "version=$version"

# vscode extensions
cd $vscode_sources
for extension_id in $(ls -d vscode-*)
do
    cd $vscode_sources/$extension_id
    echo "Should update version of $extension_id to $version"
    npm version $version
    git add package.json
    echo ""
done

# atom extensions
# cd $atom_sources
# for extension_id in $(ls -d atom-*)
# do
#     if [ $extension_id != "atom-commons" ]; then
#         cd $atom_sources/$extension_id
#         echo "Should update version of $extension_id to $version"
#         npm version $version
#         git add package.json
#         echo ""
#     fi
# done

# theia extensions
# cd $workdir
# if [ -f theia-version/version ]; then
#     theia_version=`cat theia-version/version`
#     echo "theia-version=$theia_version"

#     cd $theia_sources
#     for extension_id in $(ls -d theia-*)
#     do
#         if [ $extension_id != "theia-commons" ]; then
#             echo "Should update version of $extension_id"
#             # skip over 'theia-'
#             ext_type=${extension_id:6}
#             # step into folder containg actual extension source
#             cd $theia_sources/$extension_id/$ext_type
#             # Lerna version command needs package to be modofoed since last release
#             # Change version in the README.md file to make necessaey change to make Lerna version command work
#             if grep -q '^\*\*Version: .*\*\*$' README.md;
#             then
#             # Change version in the README.md file
#             perl -p -i -e 's/^\*\*Version: .*\*\*$/**Version: '"$theia_version"'**/g' README.md
#             else
#             # add version string if isn't there
#             echo -e "\n\n**Version: ${theia_version}**" >> README.md
#             fi

#             # step into theia extension folder and run Lerna command to update appropriate versions in all projects
#             cd $theia_sources/$extension_id
#             lerna version $theia_version --exact --no-git-tag-version --no-push --yes
#             git add ./
#             echo ""
#         fi
#     done
# fi

cd $workdir/sts4/headless-services
$workdir/sts4/concourse/tasks/update-pom-versions.sh $version

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git add .

git commit \
    -m "Bump version to ${version}"

git clone $workdir/sts4 $workdir/out
