set -e
if [ -d "$1" ]
then
  echo "Sign JARs in directory: $1"
  files=`ls $1`
  for file in $files
  do
#    echo "Found $file..."
    if ! [ -d $file ]
    then
#      echo "Looking at: $1/$file"
      extension="${file##*.}"
#      echo "Detected extension = $extension"
      if [ "$extension" = "jar" ]
      then
        echo "Copying $file to remote machine..."
        scp -i $SSH_KEY $1/$file $SSH_USER@vm-tools.spring.vmware.com:/opt/bamboo
        echo "Signing $file..."
        ssh -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com -- /build/apps/signing/signserver/signc -v --input=/opt/bamboo/$file --keyid=jarsign_vmware --signmethod="jdk-1.8.0_121" --output=/opt/bamboo/$file
        echo "Copying **signed** $file back... (into $1/$file)"
        scp -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com:/opt/bamboo/signed-$file $1/$file
        echo "Cleaning up remote machine..."
        ssh -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com -- rm /opt/bamboo/$file
        echo "Successfully signed $file"
      fi
    fi
  done
fi
