#!/usr/bin/env grooy

node {
  def CONTAINER_BASE = null
  def FROM_IMAGE = null
  def IMAGE_NAME = null
  def NODE_VERSION = null
  def SCM_VARS = null

  CONTAINER_BASE = "${GITLAB_INNERSOURCE_REGISTRY}/devops/images"
  FROM_IMAGE = "${CONTAINER_BASE}/usgs/centos"
  IMAGE_NAME = "${CONTAINER_BASE}/usgs/node"
  NODE_VERSION = params.NODE_VERSION

  try {
    stage('Initialize') {
      clearnWs()

      SCM_VARS = checkout scm

      if (params.GIT_BRANCH != '') {
        sh "git checkout --detach ${params.GIT_BRANCH}"

        SCM_VARS.GIT_BRANCH = params.GIT_BRANCH
        SCM_VARS.GIT_COMMIT = sh(
          returnStdout: true,
          script: "git rev-parse HEAD"
        )
      }

      // Map "latest" --> "lts/*"
      if (NODE_VERSION == 'latest') {
        NODE_VERSION = 'lts/*'
      }
    }

    stage('Build') {
      ansiColor('xterm') {
        // Tag for internal registry
        sh """
          docker build \
            --build-arg FROM_IMAGE=${FROM_IMAGE} \
            --build-arg NODE_VERSION=${NODE_VERSION} \
            -t ${IMAGE_NAME}:${NODE_VERSION} .
        """

        // Tag for default public Docker Hub
        sh """
          docker tag \
            ${IMAGE_NAME}:${NODE_VERSION} \
            usgs/node:${NODE_VERSION}
        """
      }
    }

    stage('Scan') {
      echo 'TODO :: Implement security scanning.'
    }

    stage('Publish') {
      docker.withRegistry(
        "https://${GITLAB_INNERSOURCE_REGISTRY}",
        'innersource-hazdev-cicd'
      ) {
        ansiColor('xterm') {
          sh "docker push ${IMAGE_NAME}:${NODE_VERSION}"
        }
      }

      docker.withRegistry('', 'usgs-docker-hub-credentials') {
        ansiColor('xterm') {
          sh "docker push usgs/node:${NODE_VERSION}"
        }
      }
    }
  } catch (err) {
    try {
      // mail([
      //   to: 'gs-haz_dev_team_group@usgs.gov',
      //   from: 'noreply@jenkins',
      //   subject: "Jenkins Pipeline Failed: ${env.BUILD_TAG}",
      //   body: "Details: ${err}"
      // ])
    } catch (inner) {
      echo "An error occured while sending email. '${inner}'"
    }

    currentBuild.result = 'FAILURE'
    throw err
  }
}
