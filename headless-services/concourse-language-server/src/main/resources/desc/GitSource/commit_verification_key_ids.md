*Optional.* Array of GPG public key ids that
the resource will check against to verify the commit (details below). The
corresponding keys will be fetched from the key server specified in
`gpg_keyserver`. The ids can be short id, long id or fingerprint.

If `commit_verification_keys` or `commit_verification_key_ids` is specified in
the source configuration, it will additionally verify that the resulting commit
has been GPG signed by one of the specified keys. It will error if this is not
the case.