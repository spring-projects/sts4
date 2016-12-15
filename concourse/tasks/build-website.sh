#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sts4/eclipse-distribution/common/html
target=$workdir/website

#cp -r "${sources}/stylesheet.css" "$target"
#cp -r ${sources}/*.js "$target"
#cp s3-manifest-yaml-vsix/*.vsix "$target"
#cp s3-boot-properties-vsix/*.vsix "$target"

export vscode_manifest_yaml=$(basename s3-manifest-yaml-vsix/*.vsix)
echo "vscode_manifest_yaml=$vscode_manifest_yaml"
export vscode_boot_properties=$(basename s3-boot-properties-vsix/*.vsix)
echo "vscode_boot_properties=$vscode_boot_properties"
export vscode_concourse=$(basename s3-concourse-vsix/*.vsix)
echo "vscode_concourse=$vscode_concourse"

envsubst > "$target/vscode-extensions-snippet.html" << XXXXXX
<ul>
   <li>Spring Boot Property Language Server: 
       <a href="http://s3-test.spring.io/sts4/vscode-extensions/${vscode_boot_properties}">${vscode_boot_properties}</a> 
   </li>
   <li>Cloud Foundry Manifest Language Server: 
       <a href="http://s3-test.spring.io/sts4/vscode-extensions/${vscode_manifest_yaml}">${vscode_manifest_yaml}</a> 
   </li>
   <li>Concourse CI Language Server: 
       <a href="http://s3-test.spring.io/sts4/vscode-extensions/${vscode_concourse}">${vscode_manifest_yaml}</a> 
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
