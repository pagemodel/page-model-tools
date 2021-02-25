#!/bin/bash -eux

DOCKER_REG=

DEPLOY=0
if [[ "$#" -gt 0 ]]; then
  if [ "$1" == "-d" ] || [ "$1" == "--deploy" ]; then
    DEPLOY=1
    shift
  fi
fi

if [[ "$#" -gt 0 ]]; then
  DOCKER_FILE="$1"
  shift
else
  DOCKER_FILE="pagemodel-headless-chrome.dockerfile"
fi
if [[ "$#" -gt 0 ]]; then
  VERSION="$1"
  shift
else
  VERSION="0.8.0"
fi

cd "$(dirname "$0")"
DOCKER_NAME="${DOCKER_FILE%.*}"
docker build -f "${DOCKER_FILE}" -t ${DOCKER_NAME}:${VERSION} ../..
if [ "${DEPLOY}" == "1" ] && [ "${DOCKER_REG}" ]; then
  IMG_HASH="$(docker images | head | grep -o "^${DOCKER_NAME} *${VERSION//./\.} *[a-zA-Z0-9]*" | xargs | cut -d' ' -f3)"
  docker tag ${IMG_HASH} ${DOCKER_REG}/${DOCKER_NAME}:${VERSION}
  docker push ${DOCKER_REG}/${DOCKER_NAME}:${VERSION}
fi
