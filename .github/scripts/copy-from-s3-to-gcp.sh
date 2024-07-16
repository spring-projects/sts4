s3_url=$1
gcp_url=$2

# Process s3_url remove trailing / and get the last segment without the trailing /
file_name=${s3_url%/}
file_name=${file_name##*/}

# Download from S3 then upload to GCP
aws s3 cp $s3_url . --recursive --no-progress
echo "-------"
ls
echo "-------"
gcp storage cp $gcp_url ./$file_name --recursive
rm -rf $file_name