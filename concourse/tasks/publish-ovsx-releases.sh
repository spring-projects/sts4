#!/bin/bash
set -e
workdir=`pwd`

#Uncomments the below to publish all vsix files in the task inputs:
#vsix_files=`ls ${workdir}/s3-*/*.vsix`

#Uncomment the below to publish only concourse vxix
#vsix_files=`ls ${workdir}/s3-*/vscode-concourse-*.vsix`

#Uncomment the below to publish all vsix files
vsix_files=`ls ${workdir}/s3-*/vscode-*.vsix`

for vsix_file in $vsix_files
do
    echo "****************************************************************"
    echo "*** Publishing : ${vsix_file} to https://open-vsx.org/"
    echo "****************************************************************"
    echo ""
    echo "We are runing the following command:"
    echo ""
    echo "      ovsx publish -p $ovsx_token $vsix_file"
    echo ""
    ovsx publish -p $ovsx_token $vsix_file
done
