set -e

file=$1
echo "Clearing S3 caches for file: ${file}"

#Flush AWS Cloudfront Cache
echo "aws cloudfront create-invalidation --distribution-id ECAO9Q8651L8M --output json --paths '${file}'"

#invalidation_json=`aws cloudfront create-invalidation --distribution-id ECAO9Q8651L8M --output json --paths "${path_with_pattern}"`
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
json="{\"files\": [ \"s3://dist.springsource.com/${file}\" ] }"
echo $json
#    curl -X DELETE "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/purge_cache" \
#      -H "X-Auth-Email: spring-sysadmin@pivotal.io" \
#      -H "Authorization: Bearer ${CLOUDFLARE_CACHE_TOKEN}" \
#      -H "Content-Type: application/json" \
#      --data "${json}"

#  curl -X DELETE "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/purge_cache" \
#    -H "X-Auth-Email: spring-sysadmin@pivotal.io" \
#    -H "Authorization: Bearer ${CLOUDFLARE_CACHE_TOKEN}" \
#    -H "Content-Type: application/json" \
#    --data "${json}"
#fi


