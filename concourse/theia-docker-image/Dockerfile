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

ADD plugins /theia-app/plugins

WORKDIR /theia-app
ENV SHELL /bin/bash
ENTRYPOINT [ "yarn",  "theia", "start", "--hostname=0.0.0.0", "--plugins=local-dir:/theia-app/plugins/", "/home/project" ]
EXPOSE 3000 
