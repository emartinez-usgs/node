ARG FROM_IMAGE=usgs/centos:latest
FROM $FROM_IMAGE

LABEL maintainer="Eric Martinez <emartinez@usgs.gov>" \
  dockerfile_version="v0.3.0"


ENV NPM_CONFIG_CAFILE ${SSL_CERT_FILE}
ENV NVM_DIR=/home/usgs-user/nvm


# Install and configure NVM
RUN export NVM_DIR=${NVM_DIR} && \
  mkdir -p ${NVM_DIR} && \
  curl -s -o- \
  https://raw.githubusercontent.com/creationix/nvm/v0.34.0/install.sh \
  | /bin/bash && \
  chown -R usgs-user:usgs-user ${NVM_DIR} && \
  echo "export NVM_DIR=${NVM_DIR}" > /etc/profile.d/nvm.sh && \
  echo "source ${NVM_DIR}/nvm.sh" >> /etc/profile.d/nvm.sh

# Install seleted version of Node
USER usgs-user
WORKDIR /home/usgs-user

ARG NODE_VERSION='lts/*'
ENV NODE_VERSION ${NODE_VERSION}
RUN /bin/bash --login -c "nvm install --no-progress ${NODE_VERSION}"