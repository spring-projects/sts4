set -e

pattern=$1

echo "Clearing S3 caches for path: ${s3_path}"

#Flush AWS Cloudfront Cache
echo 'aws cloudfront create-invalidation --distribution-id ECAO9Q8651L8M --output json --paths "/${pattern}"'

#invalidation_json=`aws cloudfront create-invalidation --distribution-id ECAO9Q8651L8M --output json --paths "/${pattern}"`
#echo "Invalidation response: ${invalidation_json}"
#invalidation_id=`echo $invalidation_json | jq -r '.Invalidation.Id'`
#invalidation_status=`echo $invalidation_json | jq -r '.Invalidation.Status'`
#echo "ID=${invalidation_id} Status=${invalidation_status}"
#while [ "${invalidation_status}" == "InProgress" ]
#do
#   echo "Invalidation status: ${invalidation_status}"
#   sleep 3
#   invalidation_status=`aws cloudfront get-invalidation --distribution-id ECAO9Q8651L8M --id $invalidation_id --output json | jq -r '.Invalidation.Status'`
#done
#echo "Final invalidation status: ${invalidation_status}"

# Flush CloudFlare Cache
s3_url=s3://dist.springsource.com/
files=`aws s3 cp ${s3_url} . --recursive --include "${pattern}" --dryrun`
counter=0
json=""
NL=$'\n'
FILES_BATCH=7
for file in $files
do
  if [[ "$counter" -eq 0 ]]; then
    json="{\"files\": [${NL}"
  fi
  if [[ "$file" =~ ^"s3://dist.springsource.com" ]]; then
    echo "Processing ${file}"
    counter=$((counter+1))
#    path=${file:26}
#    json="${json}\"http://dist.springsource.com${path}\",${NL}\"https://dist.springsource.com${path}\",${NL}\"http://download.springsource.com${path}\",${NL}\"https://download.springsource.com${path}\",${NL}"
  fi
  if [[ "$counter" -eq "$FILES_BATCH" ]]; then
#    json="${json%,*}${NL}]}"
#    echo $json
#
#    curl -X DELETE "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/purge_cache" \
#      -H "X-Auth-Email: spring-sysadmin@pivotal.io" \
#      -H "Authorization: Bearer ${CLOUDFLARE_CACHE_TOKEN}" \
#      -H "Content-Type: application/json" \
#      --data "${json}"

    json=""
    counter=0
  fi
done
#if ! [[ "$counter" -eq 0 ]]; then
#  json="${json%,*}${NL}]}"
#  echo $json
#
#  curl -X DELETE "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/purge_cache" \
#    -H "X-Auth-Email: spring-sysadmin@pivotal.io" \
#    -H "Authorization: Bearer ${CLOUDFLARE_CACHE_TOKEN}" \
#    -H "Content-Type: application/json" \
#    --data "${json}"
#fi


