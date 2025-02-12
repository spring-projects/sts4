set -e
in_file=$1
out_file=$2
id=$3
wait_time=$4
timeout=$5

in_filename="$(basename -- $in_file)"
echo "Copying ${in_file} to s3 s3://${AWS_S3_BUCKET}/exes-to-sign/${id}.exe for signing"
aws s3 cp $in_file s3://$AWS_S3_BUCKET/exes-to-sign/$id.exe --no-progress  --checksum-algorithm CRC32
for (( i=wait_time; i<timeout; i+=wait_time )) ; {
  sleep $wait_time
  aws s3api head-object --bucket $CDN_BUCKET --key spring-tools/exes-signed/$id.exe >/dev/null 2>&1 || not_exist=true
  if [ $not_exist ]; then
    echo "Waited ${i} seconds but ${in_filename} hasn't been signed yet..."
  else
    echo "Successfully signed file ${in_filename}"
    break
  fi
  unset not_exist
}
aws s3 mv s3://$AWS_S3_BUCKET/exes-signed/$id.exe $out_file --no-progress

