#!/bin/bash -eux

DOCKER_REG=

DEPLOY=0
if [[ "$#" -gt 0 ]]; then
  if [ "$1" == "-d" ] || [ "$1" == "--deploy" ]; then
    DEPLOY=1
    shift
  fi
fi

DOCKER_FILE="pagemodel-headless-chrome.dockerfile"
if [[ "$#" -gt 0 ]]; then
  if [ "$1" == "-f" ] || [ "$1" == "--dockerfile" ]; then
    DOCKER_FILE="$"
    shift 2
  fi
fi

if [ -f ../../build.gradle ]; then
  VERSION="$(grep -o "version\s*=\s*'[^']*'" ../../build.gradle | cut -d"'" -f2)"
else
  VERSION="0.8.1"
fi
if [[ "$#" -gt 0 ]]; then
  if [ "$1" == "-v" ] || [ "$1" == "--version" ]; then
    VERSION="$1"
    shift 2
  fi
fi

cd "$(dirname "$0")"
DOCKER_NAME="${DOCKER_FILE%.*}"
docker build -f "${DOCKER_FILE}" -t ${DOCKER_NAME}:${VERSION} ../..
if [ "${DEPLOY}" == "1" ] && [ "${DOCKER_REG}" ]; then
  IMG_HASH="$(docker images | head | grep -o "^${DOCKER_NAME} *${VERSION//./\.} *[a-zA-Z0-9]*" | xargs | cut -d' ' -f3)"
  docker tag ${IMG_HASH} ${DOCKER_REG}/${DOCKER_NAME}:${VERSION}
  docker push ${DOCKER_REG}/${DOCKER_NAME}:${VERSION}
fi
