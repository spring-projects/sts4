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

export vscode_manifest_yaml=$(basename s3-manifest-yaml-vsix-${dist_type}/*.vsix)
echo "vscode_manifest_yaml=$vscode_manifest_yaml"
export vscode_spring_boot=$(basename s3-spring-boot-vsix-${dist_type}/*.vsix)
echo "vscode_spring_boot=$vscode_spring_boot"
export vscode_concourse=$(basename s3-concourse-vsix-${dist_type}/*.vsix)
echo "vscode_concourse=$vscode_concourse"
export vscode_bosh=$(basename s3-bosh-vsix-${dist_type}/*.vsix)
echo "vscode_bosh=$vscode_bosh"

envsubst > "$target/vscode-extensions-snippet.html" << XXXXXX
<ul>
   <li>Spring Boot Language Server: 
       <a href="https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/vscode-extensions/${dist_type}s/${vscode_spring_boot}">${vscode_spring_boot}</a> 
   </li>
   <li>Cloud Foundry Manifest Language Server: 
       <a href="https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/vscode-extensions/${dist_type}s/${vscode_manifest_yaml}">${vscode_manifest_yaml}</a> 
   </li>
   <li>Concourse CI Language Server: 
       <a href="https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/vscode-extensions/${dist_type}s/${vscode_concourse}">${vscode_concourse}</a> 
   </li>
   <li>Bosh Language Server: 
       <a href="https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/vscode-extensions/${dist_type}s/${vscode_bosh}">${vscode_bosh}</a> 
   </li>
</ul>
XXXXXX
export vscode_snippet=`cat "$target/vscode-extensions-snippet.html"`

envsubst > "$target/vscode-extensions.html" << XXXXXX
<!DOCTYPE html>
<html>
<body>

<h1>STS4 Vscode Extensions</h1>

$vscode_snippet

</body>
</html>
XXXXXX

cat $target/vscode-extensions.html
