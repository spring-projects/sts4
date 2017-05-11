#!/bin/bash
set -e
workdir=`pwd`
vsix_files=`ls ${workdir}/s3-*/*.vsix`

for vsix_file in $vsix_files
do
    echo "****************************************************************"
    echo "*** Publishing : ${vsix_file}"
    echo "****************************************************************"
    echo ""
    echo "We should be runing the following command:"
    echo ""
    echo "     vsce publish -p $vsce_token --packagePath $vsix_file"
    echo ""
    echo "But this is just a practice run..."
    echo ""
done

exit 99 ## take this line out once the publishing code is functional and does something real.