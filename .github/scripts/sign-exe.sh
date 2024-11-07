set -e
in_file=$1
out_file=$2
id=$3
wait_time=$4
number_of_sleeps=$5

in_filename="$(basename -- $in_file)"
echo "Copying ${in_file} to s3 s3://${AWS_S3_BUCKET}/exes-to-sign/${id}.exe for signing"
aws s3 cp $in_file s3://$AWS_S3_BUCKET/exes-to-sign/$id.exe
for i in {1..$number_of_sleeps}
do
  sleep $wait_time
  aws s3api head-object --bucket $CDN_BUCKET --key spring-tools/exes-signed/$id.exe || NOT_EXIST=true
  if [ $NOT_EXIST ]; then
    echo "Waited ${wait_time} seconds but ${in_filename} hasn't been signed yet..."
  else
    echo "Successfully signed file ${in_filename}"
    break
  fi
done
aws mv s3://$AWS_S3_BUCKET/exes-signed/$id.exe $out_file

