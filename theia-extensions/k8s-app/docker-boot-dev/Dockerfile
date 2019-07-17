FROM ubuntu:18.04

RUN apt-get update && apt-get install -y \
  build-essential \
  gettext-base \
  git \
  jq \
  openjdk-8-jdk=8u162-b12-1 openjdk-8-jre=8u162-b12-1 openjdk-8-jdk-headless=8u162-b12-1 openjdk-8-jre-headless=8u162-b12-1 openjdk-8-source=8u162-b12-1 \
  curl

RUN curl -sL https://deb.nodesource.com/setup_8.x | bash - \
  && apt-get install -y nodejs

RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - \
 && echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list \
 && apt-get install tar \
 && apt-get update \
 && apt-get install -y yarn

RUN adduser --disabled-password --gecos '' theia
RUN chmod g+rw /home && \
    mkdir -p /home/project && \
    chown -R theia:theia /home/theia && \
    chown -R theia:theia /home/project;

WORKDIR /home/theia

COPY theia-spring-boot.tgz ./theia-spring-boot.tgz
COPY package.json ./package.json
RUN chmod g+rw ./package.json && \
    chown -R theia:theia ./package.json;

# USER theia
RUN yarn add ./theia-spring-boot.tgz --cache-folder ./ycache && rm -rf ./ycache && yarn theia build
EXPOSE 3000
ENV SHELL /bin/bash
ENTRYPOINT [ "yarn", "theia", "start", "/home/project", "--hostname=0.0.0.0" ]