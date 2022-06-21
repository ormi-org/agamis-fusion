FROM apacheignite/ignite
RUN apk add git
RUN mkdir /tmp/agamis
WORKDIR /tmp/agamis
RUN git clone https://<user>:<token>@git.cloud.openrichmedia.org/git/agamis/fusion.git
RUN mkdir /ignite-config
RUN mv /tmp/agamis/fusion/ignite/ignite-default-config.xml /ignite-config
WORKDIR /