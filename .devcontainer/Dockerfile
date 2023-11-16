FROM ubuntu:22.04

LABEL org.openrichmedia="The Open Rich Media Initiative"
LABEL name="reg.docker.openrichmedia.org/agamis-fusion-jdk17lts-node18lts-dev:0923"
LABEL version="1.0"
LABEL description="An image for dev environment packed with OpenJDK 17, Node 18, PNPM, Plantuml, Git"

ARG PLANTUML_VERSION=1.2023.10
ARG NODEJS_VERSION=18
ARG SHELL=/bin/bash

# Install JDK & dependencies
RUN apt update && apt install -y openjdk-17-jdk graphviz ca-certificates curl gnupg git wget xz-utils
# Install SBT
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add
RUN apt-get update
RUN apt-get install sbt
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
# Install Insomnia
RUN echo "deb [trusted=yes arch=amd64] https://download.konghq.com/insomnia-ubuntu/ default all" \
    | tee -a /etc/apt/sources.list.d/insomnia.list
RUN apt update && apt install insomnia libgbm-dev -y
# Install Inso CLI
RUN mkdir /root/.local/share/insomnia-cli
WORKDIR /root/.local/share/insomnia-cli
RUN wget https://github.com/Kong/insomnia/releases/download/lib%408.1.0/inso-linux-8.1.0.tar.xz
RUN tar -xf inso-linux-8.1.0.tar.xz
ENV PATH=$PATH:/root/.local/share/insomnia-cli
RUN inso --version
RUN rm inso-linux-8.1.0.tar.xz
# Install Synth https://www.getsynth.com/docs/getting_started/installation
RUN curl --proto '=https' --tlsv1.2 -sSL https://getsynth.com/install | sh
ENV PATH=$PATH:/root/.local/bin

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

CMD tail -f /dev/null