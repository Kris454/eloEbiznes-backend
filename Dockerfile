FROM ubuntu:18.04

ENV TZ=Europe/Warsaw

RUN apt-get update && apt-get install -y \ 
    vim \
    git \ 
    wget \
    openjdk-8-jdk \
    curl

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
RUN export JAVA_HOME

RUN wget https://downloads.lightbend.com/scala/2.12.3/scala-2.12.3.deb &&\
    dpkg -i scala-2.12.3.deb

RUN mkdir -p "/usr/local/sbt"
RUN wget -qO - --no-check-certificate "https://github.com/sbt/sbt/releases/download/v1.5.2/sbt-1.5.2.tgz" | tar xz -C /usr/local/sbt --strip-components=1
ENV SBT_HOME /usr/local/sbt
ENV PATH $SBT_HOME:$PATH

EXPOSE 9000

RUN useradd -ms /bin/bash kskiba
RUN adduser kskiba sudo

USER root
WORKDIR /opt/
RUN mkdir /opt/ebiznes-backend
WORKDIR /opt/ebiznes-backend
COPY . ./
