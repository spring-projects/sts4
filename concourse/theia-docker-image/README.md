Theai Docker Image
==================

We currently plublish a docler image that contains:

- a full-featured `theai` set of package for version `next`.
- all 4 of our vscode extensions as latest snapshot version.

This image is published to docker hub as `kdvolder/sts4-theia-snapshot:latest`.

You can run it as follows:

```
docker run -it --init  -p 3000:3000 -p 8080:8080 -v "$(pwd):/home/project:cached" kdvolder/sts4-theia-snapshot:latest
```

Then open `http://localhost:3000` to access it.