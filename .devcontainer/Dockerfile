FROM ubuntu:22.04

LABEL org.openrichmedia="The Open Rich Media Initiative"
LABEL name="reg.docker.openrichmedia.org/agamis-fusion-jdk17lts-node18lts-dev:0923"
LABEL version="1.0"
LABEL description="An image for dev environment packed with OpenJDK 17, Node 18, PNPM, Plantuml, Git"


ARG PLANTUML_VERSION=1.2023.10
ARG NODEJS_VERSION=18
ARG SHELL=/bin/bash

# Install JDK & dependencies
RUN apt update && apt install -y openjdk-17-jdk graphviz ca-certificates curl gnupg git
# Install NODE
RUN mkdir -p /etc/apt/keyrings
RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
RUN echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODEJS_VERSION.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list
RUN apt update && apt install nodejs -y
# Install PNPM
RUN curl -fsSL https://get.pnpm.io/install.sh | sh -
# Install Plantuml JAR
RUN mkdir -p /root/bin/plantuml &&\
curl -fsSL https://github.com/plantuml/plantuml/releases/download/v${PLANTUML_VERSION}/plantuml-${PLANTUML_VERSION}.jar \
--output /root/bin/plantuml/plantuml.jar

CMD tail -f /dev/null