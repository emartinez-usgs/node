#!/usr/bin/env grooy

node {
  def CONTAINER_BASE = null
  def FROM_IMAGE = null
  def INTERNAL_IMAGE_NAME = null
  def PUBLIC_IMAGE_NAME = null
  def NODE_VERSION = null

  CONTAINER_BASE = "${GITLAB_INNERSOURCE_REGISTRY}/devops/images"
  FROM_IMAGE = "${CONTAINER_BASE}/${params.FROM_IMAGE}"

  INTERNAL_IMAGE_NAME = "${CONTAINER_BASE}/${params.IMAGE_NAME}"
  PUBLIC_IMAGE_NAME = "${params.IMAGE_NAME}"
  NODE_VERSION = params.IMAGE_NAME.split(':')
  NODE_VERSION = NODE_VERSION[1]

  try {
    stage('Initialize') {
      cleanWs()

      checkout scm

      if (params.GIT_BRANCH != '') {
        sh "git checkout --detach ${params.GIT_BRANCH}"
      }

      if (NODE_VERSION == 'latest') {
        NODE_VERSION = 'lts/*'
      }
    }

    stage('Build') {
      ansiColor('xterm') {
        // Build tag for internal registry
        sh """
          docker build \
            --build-arg FROM_IMAGE=${FROM_IMAGE} \
            --build-arg NODE_VERSION=${NODE_VERSION} \
            -t ${INTERNAL_IMAGE_NAME} .
        """

        // Re-tag for default public Docker Hub
        sh """
          docker tag \
            ${INTERNAL_IMAGE_NAME} \
            ${PUBLIC_IMAGE_NAME}
        """
      }
    }

    stage('Publish') {
      docker.withRegistry(
        "https://${GITLAB_INNERSOURCE_REGISTRY}",
        'innersource-hazdev-cicd'
      ) {
        ansiColor('xterm') {
          sh "docker push ${INTERNAL_IMAGE_NAME}"
        }
      }

      docker.withRegistry('', 'usgs-docker-hub-credentials') {
        ansiColor('xterm') {
          sh "docker push ${PUBLIC_IMAGE_NAME}"
        }
      }
    }
  } catch (err) {
    try {
      mail([
        to: 'gs-haz_dev_team_group@usgs.gov',
        from: 'noreply@jenkins',
        subject: "Jenkins Pipeline Failed: ${env.BUILD_TAG}",
        body: "Details: ${err}"
      ])
    } catch (inner) {
      echo "An error occured while sending email. '${inner}'"
    }

    currentBuild.result = 'FAILURE'
    throw err
  }
}
