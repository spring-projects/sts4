#!/bin/bash
s3cmd expire s3://s3-test.spring.io \
    --expiry-prefix mvn-caches --expiry-days 7 \

# Note: it seems we can only apply one rule to a bucket.
# We would really want to expire other folders as well:
#    --expiry-prefix sts3/commons/snapshots --expiry-days 7 \
#    --expiry-prefix sts4/vscode-extensions/snapshots --expiry-days 30
# But since that doesn't work, I only added one rule for the biggest space waster.


