Theia Docker Image
==================

We currently publish a docker image that contains:

- a full-featured `theia` set of package for version `next`.
- all 4 of our vscode extensions as latest snapshot version.

This image is published to docker hub as `kdvolder/sts4-theia-snapshot:latest`.

You can run it as follows:

```
docker run -it --init  -p 3000:3000 -p 8080:8080 -v "$(pwd):/home/project:cached" kdvolder/sts4-theia-snapshot:latest
```

Then open `http://localhost:3000` to access it.

Developer Notes:
================

package.json file based on this one: 
https://github.com/theia-ide/theia-apps/blob/master/theia-full-docker/latest.package.json

If it breaks in the future it is often a good idea to go back to the source and see what 
the Theia folks themselves have done to address it.