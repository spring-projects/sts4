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
WORKDIR /theia-app
# using "NODE_OPTIONS=..." to avoid out-of-memory problem in CI


ENV SHELL=/bin/bash \
    THEIA_DEFAULT_PLUGINS=local-dir:/theia-app/plugins

RUN yarn --cache-folder ./ycache && rm -rf ./ycache && \
    NODE_OPTIONS="--max_old_space_size=8192" yarn theia build ; \
    yarn theia download:plugins

ADD plugins/*.vsix /theia-app/plugins/

ENTRYPOINT [ "yarn",  "theia", "start", "--hostname=0.0.0.0", "/home/project" ]
EXPOSE 3000 
