dist_path=$1
eclipse_version=$2
build_type=$3

s3_url=s3://dist.springsource.com/${dist_path}
downloads_html="sts4-nightly-${eclipse_version}.html"
files=`aws s3 cp ${s3_url} . --recursive --exclude "*" --include "spring-tool-suite-4*.zip" --include "spring-tool-suite-4*.dmg" --include "spring-tool-suite-4*.self-extracting.jar" --include "spring-tool-suite-4*.tar.gz" --exclude "*/*" --dryrun`
rm -f ./${downloads_html}
echo '<ul>' >> $downloads_html
for file in $files
do
  if [[ "$file" =~ ^"s3://dist." ]]; then
    download_url=https://download${file:9}
    filename=${file:${#s3_url}+1}
    echo '  <li><a href="'${download_url}'">'${filename}'</li>' >> $downloads_html
  fi
done
echo '</ul>' >> $downloads_html
cat ./$downloads_html
aws s3 mv ./$downloads_html s3://dist.springsource.com/${build_type}/STS4/ --acl public-read --no-progress
