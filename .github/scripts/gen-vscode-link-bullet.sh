id=$1
label=$2
download_url_root=$3

vsix_file=`aws s3 cp s3://$AWS_S3_BUCKET/snapshot/vscode-extensions/$id/ . --exclude "*" --include "*.vsix" --exclude "*/*" --dryrun | head -n 1`
if [ ! -z "${vsix_file}" ]; then
  echo "<li>${label}: <a href=\"${download_url_root}/snapshot/vscode-extensions/${id}/${vsix_file}\">${vsix_file}</a></li>"
fi