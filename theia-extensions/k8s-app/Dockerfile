FROM ubuntu:18.04
RUN apt-get update && apt-get install -y \
  build-essential \
  gettext-base \
  git \
  jq \
  openjdk-8-jdk \
  curl

RUN curl -sL https://deb.nodesource.com/setup_10.x | bash -
RUN apt-get install -y nodejs

RUN npm install -g yarn

RUN cd /usr/local/bin && \
  curl https://s3-us-west-1.amazonaws.com/cf-cli-releases/releases/v6.47.2/cf-cli_6.47.2_linux_x86-64.tgz | tar zxvf -

RUN mkdir /theia-app
ADD package.json /theia-app
RUN cd /theia-app && \
    yarn && \
    yarn theia build

## Install vscode plugins
RUN mkdir /theia-app/plugins && \
    cd /theia-app/plugins && \
    curl --compressed -o vscode-spring-boot.vsix \
      https://marketplace.visualstudio.com/_apis/public/gallery/publishers/Pivotal/vsextensions/vscode-spring-boot/1.13.0/vspackage && \
    curl --compressed -o vscode-concourse.vsix \
      https://marketplace.visualstudio.com/_apis/public/gallery/publishers/Pivotal/vsextensions/vscode-concourse/1.13.0/vspackage && \
    curl --compressed -o vscode-manifest-yaml.vsix \
      https://marketplace.visualstudio.com/_apis/public/gallery/publishers/Pivotal/vsextensions/vscode-manifest-yaml/1.13.0/vspackage && \
    curl --compressed -o vscode-bosh.vsix \
      https://marketplace.visualstudio.com/_apis/public/gallery/publishers/Pivotal/vsextensions/vscode-bosh/1.13.0/vspackage

WORKDIR /theia-app
ENV SHELL /bin/bash
ENTRYPOINT [ "yarn",  "theia", "start", "--hostname=0.0.0.0", "--plugins=local-dir:/theia-app/plugins/", "/home/project" ]
EXPOSE 3000 
