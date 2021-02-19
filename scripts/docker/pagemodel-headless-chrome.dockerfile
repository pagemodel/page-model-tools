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

COPY --chown=seluser . /home/seluser/page-model-tools
WORKDIR /home/seluser/page-model-tools

USER root
RUN rm -rf ./**/build/
RUN rm -rf ./.gradle/
USER seluser
RUN cd org.pagemodel.gen.gradle && ../gradlew --rerun-tasks publishToMavenLocal --console=plain
RUN ./gradlew --rerun-tasks clean build test publishToMavenLocal -Dbrowser=headless --parallel --console=plain
RUN rm -rf /home/seluser/page-model-tools
