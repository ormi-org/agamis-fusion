FROM debian:bullseye

ARG PLANTUML_VERSION=1.2023.10
ARG NODEJS_VERSION=20

# Install JDK & dependencies
RUN apt update && apt install -y openjdk-17-jdk graphviz
# Install NODE
RUN curl -fsSL https://deb.nodesource.com/setup_${NODEJS_VERSION}.x | sh - && apt-get install -y nodejs
# Install PNPM
RUN curl -fsSL https://get.pnpm.io/install.sh | sh -
# Install Plantuml JAR
RUN mkdir -p /root/bin/plantuml &&\
curl -fsSL https://github.com/plantuml/plantuml/releases/download/v${PLANTUML_VERSION}/plantuml-${PLANTUML_VERSION}.jar \
--output /root/bin/plantuml/plantuml.jar

CMD tail -f /dev/null