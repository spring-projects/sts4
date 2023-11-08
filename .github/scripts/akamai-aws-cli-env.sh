# Akamai supports assumeRole for Cloudgate S3 access
# The script asks for temp credentials to be able to upload to Akamai S3 origin bucket
# The AWS CLI environment variables are then updated with temp credentials values
session_name=$1
duration_seconds=$2
export $(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
$(aws sts assume-role \
--role-arn arn:aws:iam::${{ secrets.TOOLS_CLOUDGATE_ACCOUNT_ID }}:role/${{ secrets.TOOLS_CLOUDGATE_USER }} \
--role-session-name $session_name \
--duration-seconds $duration_seconds \
--query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
--output text))
