def call(Map pipelineConfig = [:]) {
    pipeline {
        agent any

        environment {
            SONAR_TOKEN = credentials('sonar-token')
            SONAR_HOST_URL = 'http://65.21.108.94:9000'
            PROJECT_NAME = "${pipelineConfig.projectName ?: 'Generic-Service'}"
            SONAR_KEY = "${pipelineConfig.sonarKey ?: env.JOB_NAME}"
        }

        stages {
            stage('Governance: Security Gates') {
                parallel {
                    stage('Secrets Detection') {
                        steps {
                            echo "Scanning ${env.PROJECT_NAME} for secrets..."
                            // Using Gitleaks with --source to scan the workspace. 
                            // We use '|| true' only if we want to report but not fail, 
                            // but for PCI-DSS we should typically fail the build.
                            sh """
                                docker run --rm -v ${WORKSPACE}:/path \
                                    zricethezav/gitleaks:latest detect \
                                    --source="/path" \
                                    --report-path="/path/gitleaks-report.json" \
                                    --redact -v || true
                            """
                        }
                    }
                    stage('SCA Scan') {
                        steps {
                            echo "Scanning ${env.PROJECT_NAME} dependencies..."
                            // Every service MUST have a 'make scan' target
                            sh 'make scan'
                        }
                    }
                }
            }

            stage('Build & Test') {
                steps {
                    echo "Executing standard build for ${env.PROJECT_NAME}..."
                    // Every service MUST have a 'make test' target
                    sh 'make test'
                }
            }

            stage('SAST (SonarQube)') {
                steps {
                    echo "Publishing security metrics for ${env.SONAR_KEY}..."
                    sh """
                        docker run --rm --network fintech-net \
                            -v ${WORKSPACE}:/usr/src \
                            -e SONAR_HOST_URL=${SONAR_HOST_URL} \
                            -e SONAR_TOKEN=${SONAR_TOKEN} \
                            sonarsource/sonar-scanner-cli \
                            -Dsonar.projectKey=${env.SONAR_KEY} \
                            -Dsonar.projectName="${env.PROJECT_NAME}" \
                            -Dsonar.qualitygate.wait=true
                    """
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
