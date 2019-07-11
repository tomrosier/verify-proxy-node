#!/usr/bin/env bash

set -eo errexit

OUTPUT_DIR=".deploy"

rm -rf "${OUTPUT_DIR}"
mkdir -p "${OUTPUT_DIR}"

helm template \
    --name dev \
    --namespace local-main \
    --set "global.cluster.name=gsp-local" \
    --set "global.cluster.domain=local.govsandbox.uk" \
    --set "stubConnector.enabled=true" \
    --set "vsp.secretName=vsp" \
    --output-dir "${OUTPUT_DIR}" \
    ./chart

cp ci/build/intermediate-certificate-request.yaml ${OUTPUT_DIR}
cp ci/build/vsp-secret.yaml ${OUTPUT_DIR}


kapp deploy \
    -y \
    -R \
    --namespace "local-main" \
    --allow-ns "local-main" \
    --app proxy-node \
    --diff-changes \
    --labels "app=proxy-node" \
    -f "${OUTPUT_DIR}"

