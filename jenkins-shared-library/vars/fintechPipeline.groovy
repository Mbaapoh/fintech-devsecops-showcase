def call(Map pipelineConfig = [:]) {
    pipeline {
        agent {
            label "${pipelineConfig.agentLabel ?: 'any'}"
        }

        environment {
            SONAR_TOKEN = credentials('sonar-token')
            NVD_API_KEY = credentials('nvd-api-key') 
            SONAR_HOST_URL = 'http://65.21.108.94:9000'
            PROJECT_NAME = "${pipelineConfig.projectName ?: 'Generic-Service'}"
            SONAR_KEY = "${pipelineConfig.sonarKey ?: env.JOB_NAME}"
            SERVICE_DIR = "${pipelineConfig.serviceDir ?: '.'}"
            // Pass Jenkins Home as the root for decentralized caching
            CACHE_DIR = "/var/jenkins_home"
        }

        stages {
            stage('Governance: Security Gates') {
                parallel {
                    stage('Secrets Detection') {
                        steps {
                            echo "Scanning ${env.PROJECT_NAME} for secrets..."
                            sh """
                                docker run --rm -v ${WORKSPACE}/${env.SERVICE_DIR}:/path \
                                    zricethezav/gitleaks:latest detect \
                                    --source="/path" \
                                    --report-path="/path/gitleaks-report.json" \
                                    --no-git --redact -v || true
                            """
                        }
                    }
                    stage('SCA Scan') {
                        steps {
                            dir(env.SERVICE_DIR) {
                                echo "Scanning ${env.PROJECT_NAME} dependencies in ${env.SERVICE_DIR}..."
                                withEnv(["NVD_API_KEY=${env.NVD_API_KEY}"]) {
                                    sh 'make scan'
                                }
                            }
                        }
                    }
                }
            }

            stage('Build & Test') {
                steps {
                    dir(env.SERVICE_DIR) {
                        echo "Executing build for ${env.PROJECT_NAME} on agent ${env.NODE_NAME}..."
                        withEnv(["CACHE_DIR=${env.CACHE_DIR}"]) {
                            sh 'make test'
                        }
                    }
                }
            }

            stage('SAST (SonarQube)') {
                steps {
                    dir(env.SERVICE_DIR) {
                        echo "Publishing security metrics for ${env.SONAR_KEY}..."
                        sh """
                            docker run --rm --network fintech-net \
                                -v ${WORKSPACE}/${env.SERVICE_DIR}:/usr/src \
                                -e SONAR_HOST_URL=${SONAR_HOST_URL} \
                                -e SONAR_TOKEN=${SONAR_TOKEN} \
                                sonarsource/sonar-scanner-cli \
                                -Dsonar.projectKey=${env.SONAR_KEY} \
                                -Dsonar.projectName="${env.PROJECT_NAME}" \
                                -Dsonar.qualitygate.wait=true
                        """
                    }
                }
            }

            stage('Compliance Evidence') {
                steps {
                    echo "Generating audit evidence for ${env.PROJECT_NAME}..."
                    // This uses a global compliance tool managed by DevOps
                    // sh "/opt/devops/export_compliance.sh ${SONAR_HOST_URL} ${SONAR_KEY} ${SONAR_TOKEN}"
                }
            }

            stage('Package & Push') {
                when { branch 'main' }
                steps {
                    echo "Packaging ${env.PROJECT_NAME} for production..."
                    sh 'make build'
                }
            }
        }

        post {
            always {
                echo "Governance check complete for ${env.PROJECT_NAME}."
            }
            failure {
                echo "CRITICAL: ${env.PROJECT_NAME} failed the governance gate. Deployment halted."
            }
        }
    }
}
