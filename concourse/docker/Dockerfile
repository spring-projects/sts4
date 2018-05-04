FROM ubuntu:16.04

ADD npmrc /root/.npmrc

RUN apt-get update && apt-get install -y \
  build-essential \
  gettext-base \
  git \
  jq \
  openjdk-8-jdk \
  curl \
  xvfb \
  icewm icewm-themes

RUN curl -sL https://deb.nodesource.com/setup_8.x | bash - \
  && apt-get install -y nodejs

RUN npm install -g vsce

CMD /bin/bash  
