set -e
in_file=$1
out_file=$2
id=$3

in_filename="$(basename -- $in_file)"
echo "Setting up folder ${id} on the remote machine"
ssh -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com -- mkdir /opt/bamboo/$id
echo "Copying $in_filename to remote machine into /opt/bamboo/${id}..."
scp -i $SSH_KEY $in_file $SSH_USER@vm-tools.spring.vmware.com:/opt/bamboo/$id
echo "Signing $in_filename..."
ssh -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com -- /build/apps/signing/signserver/signc -v --input=/opt/bamboo/$id/$in_filename --keyid=authenticode_SHA2 --signmethod="winddk-8.1" --output=/opt/bamboo/$id/$in_filename --hash sha256
echo "Copying **signed** $in_filename back... (into $out_file)"
scp -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com:/opt/bamboo/$id/$in_filename $out_file
echo "Cleaning up remote machine..."
ssh -i $SSH_KEY $SSH_USER@vm-tools.spring.vmware.com -- rm -f /opt/bamboo/$id
echo "Successfully signed $in_filename"

