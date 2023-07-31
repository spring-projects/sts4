set -e

s3_path=$1
aws cloudfront create-invalidation --distribution-id ECAO9Q8651L8M --paths /release/TOOLS/sts4-language-server-integrations/2023-07-31/*
invalidation_json=`aws cloudfront create-invalidation --distribution-id ECAO9Q8651L8M --output json --paths /${s3_path}/*`
echo "Invalidation response: ${invalidation_json}"
invalidation_id=`echo $invalidation_json | jq -r '.Invalidation.Id'`
invalidation_status=`echo $invalidation_json | jq -r '.Invalidation.Status'`
echo "ID=${invalidation_id} Status=${invalidation_status}"
while [ "${invalidation_status}" == "InProgress" ]
do
   echo "Invalidation status: ${invalidation_status}"
   sleep 3
   invalidation_status=`aws cloudfront get-invalidation --distribution-id ECAO9Q8651L8M --id $invalidation_id --output json | jq -r '.Invalidation.Status'`
done
echo "Final invalidation status: ${invalidation_status}"
