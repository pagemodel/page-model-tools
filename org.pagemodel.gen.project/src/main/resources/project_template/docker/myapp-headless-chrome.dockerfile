FROM pagemodel-headless-chrome:0.8.0

USER seluser
COPY --chown=seluser . /home/seluser/myapp
WORKDIR /home/seluser/myapp

USER root
RUN rm -rf ./**/build/
RUN rm -rf ./.gradle/
USER seluser
RUN ./gradlew --rerun-tasks clean build test -Dbrowser=headless

WORKDIR /home/seluser
RUN rm -rf /home/seluser/myapp
