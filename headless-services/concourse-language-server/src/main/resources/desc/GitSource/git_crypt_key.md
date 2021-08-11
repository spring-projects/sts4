*Optional.* Base64 encoded
[git-crypt](https://github.com/AGWA/git-crypt) key. Setting this 
will unlock / decrypt the repository with `git-crypt`. To get the key simply
execute `git-crypt export-key -- - | base64` in an encrypted repository.