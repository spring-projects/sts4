*Optional.* The URL (scheme and domain) of your CloudFront distribution that is fronting this bucket (e.g
`https://d5yxxxxx.cloudfront.net`).  This will affect `in` but not `check`
and `put`. `in` will ignore the `bucket` name setting, exclusively using the
`cloudfront_url`.  When configuring CloudFront with versioned buckets, set
`Query String Forwarding and Caching` to `Forward all, cache based on all` to
ensure S3 calls succeed.