FROM selenium/standalone-chrome

USER root
RUN apt-get update \
    && apt-get -y install openjdk-8-jdk \
    && rm -rf /var/lib/apt/lists/*
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$PATH

RUN usermod -u 1000 seluser
RUN groupmod -g 1000 seluser
USER seluser

COPY --chown=seluser . /home/seluser/myapp
WORKDIR /home/seluser/myapp

USER root
RUN rm -rf ./**/build/
RUN rm -rf ./.gradle/
RUN rm -f user.defaults

USER seluser
RUN ./gradlew --rerun-tasks clean testClasses

WORKDIR /home/seluser
RUN rm -rf /home/seluser/myapp