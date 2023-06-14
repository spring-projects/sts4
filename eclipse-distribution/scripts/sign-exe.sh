set -e
in_file=$1
out_file=$2

in_filename="$(basename -- $in_file)"
echo "Copying $in_filename to remote machine..."
echo "Input file: $in_file"
scp -i $SSH_KEY $in_file $SSH_USER@vm-tools.spring.vmware.com:/opt/bamboo
echo "Signing $in_filename..."
ssh -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com -- /build/apps/signing/signserver/signc -v --input=/opt/bamboo/$in_filename --keyid=authenticode_SHA2 --signmethod="winddk-8.1" --output=/opt/bamboo/$in_filename --hash sha256
echo "Copying **signed** $in_filename back... (into $out_file)"
scp -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com:/opt/bamboo/$in_filename $out_file
echo "Cleaning up remote machine..."
ssh -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com -- rm /opt/bamboo/$in_filename
echo "Successfully signed $in_filename"

