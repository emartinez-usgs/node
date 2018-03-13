ARG FROM_IMAGE=usgs/centos:latest
FROM $FROM_IMAGE

LABEL maintainer="Eric Martinez <emartinez@usgs.gov>" \
      dockerfile_version="v0.3.0"


# configure npm "cafile" via environment
ARG NODE_VERSION='lts/*'
ENV NODE_VERSION ${NODE_VERSION}
ENV NPM_CONFIG_CAFILE ${SSL_CERT_FILE}


# RUN yum install -y \
#       curl && \
#     yum clean all

# Install NVM
RUN export NVM_DIR="/nvm" && \
  curl -o- \
    https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh \
       | /bin/bash && \
    echo 'export NVM_DIR=/nvm' >> /etc/profile.d/nvm.sh && \
    echo '. ${NVM_DIR}/nvm.sh' >> /etc/profile.d/nvm.sh && \
    /bin/bash --login -c "nvm install ${NODE_VERSION}"
