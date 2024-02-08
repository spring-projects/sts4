set -e

dist_path=$1
eclipse_version=$2
build_type=$3

# Akamai tools-spring-io bucket
s3_url=s3://tools-spring-io/${dist_path}
downloads_html="sts4-nightly-${eclipse_version}.html"
files=`aws s3 cp ${s3_url} . --recursive --exclude "*" --include "spring-tool-suite-4*.zip" --include "spring-tool-suite-4*.dmg" --include "spring-tool-suite-4*.self-extracting.jar" --include "spring-tool-suite-4*.tar.gz" --exclude "*/*" --dryrun`
rm -f ./${downloads_html}
echo '<ul>' >> $downloads_html
for file in $files
do
  if [[ "$file" =~ ^"s3://tools-spring-io" ]]; then
    download_url=https://cdn.spring.io/spring-tools${file:20} #
    filename=${file:${#s3_url}+1}
    echo '  <li><a href="'${download_url}'">'${filename}'</li>' >> $downloads_html
  fi
done
echo '</ul>' >> $downloads_html
cat ./$downloads_html
aws s3 mv ./$downloads_html s3://tools-spring-io/${build_type}/STS4/ --no-progress
