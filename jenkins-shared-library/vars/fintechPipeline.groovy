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
                    script {
                        echo "Generating ISO 27001 / PCI-DSS Audit Evidence for ${env.PROJECT_NAME}..."
                        // Extract the centralized compliance script from the library's resources
                        def complianceScript = libraryResource 'scripts/export_compliance.sh'
                        writeFile file: 'export_compliance.sh', text: complianceScript
                        sh "chmod +x export_compliance.sh"
                        
                        // Execute the script using centralized credentials and environment variables
                        sh "./export_compliance.sh ${SONAR_HOST_URL} ${SONAR_KEY} ${SONAR_TOKEN}"
                    }
                }
                post {
                    always {
                        // Archive the evidence as a permanent record for auditors
                        archiveArtifacts artifacts: 'compliance_evidence.md', fingerprint: true
                    }
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
