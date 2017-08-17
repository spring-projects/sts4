#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sts4/eclipse-distribution/common/html
target=$workdir/website
# consider passing this in from pipeline as a env var if we are also going to use it for 'release' builds
dist_type=snapshot

#cp -r "${sources}/stylesheet.css" "$target"
#cp -r ${sources}/*.js "$target"
#cp s3-manifest-yaml-vsix/*.vsix "$target"
#cp s3-boot-properties-vsix/*.vsix "$target"

packages=$(ls -d s3-*)
echo "package=${packages}"

echo "<ul>" > $target/atom-packages-snippet.html
for p in $packages; do
export pkg_url=`cat $p/url`
export pkg_name=$(basename $pkg_url) 
envsubst >> "$target/vscode-extensions-snippet.html" << XXXXXX
    <li><a href="${pkg_url}">${pkg_name}</a>
XXXXXX
done
echo "<ul>" >> $target/atom-packages-snippet.html
export atom_snippet=`cat "$target/atom-extensions-snippet.html"`

envsubst > "$target/atom-extensions.html" << XXXXXX
<!DOCTYPE html>
<html>
<body>

<h1>STS4 Vscode Extensions</h1>

$atom_snippet

</body>
</html>
XXXXXX

echo "============== atom-extensions.html =============="
cat $target/atom-extensions.html
echo "============== atom-extensions-snippet.html =============="
cat $target/atom-extensions-snippet.html
