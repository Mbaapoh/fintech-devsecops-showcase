def call(Map pipelineConfig = [:]) {
    pipeline {
        agent {
            // Default to 'any' for simulation/single-node mastery
            // Allow override via 'agentLabel' for distributed production setups
            label "${pipelineConfig.agentLabel ?: ''}"
        }

        environment {
            // Use withCredentials in stages for better resilience in simulation envs
            SONAR_HOST_URL = 'http://65.21.108.94:9000'
            PROJECT_NAME = "${pipelineConfig.projectName ?: 'Generic-Service'}"
            SONAR_KEY = "${pipelineConfig.sonarKey ?: env.JOB_NAME}"
            SERVICE_DIR = "${pipelineConfig.serviceDir ?: '.'}"
            // Use env.JENKINS_HOME if available, otherwise fallback to standard path
            CACHE_DIR = "${env.JENKINS_HOME ?: '/var/jenkins_home'}"
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
                                script {
                                    echo "Scanning ${env.PROJECT_NAME} dependencies in ${env.SERVICE_DIR}..."
                                    try {
                                        withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
                                            sh 'make scan'
                                        }
                                    } catch (Exception e) {
                                        echo "WARNING: 'nvd-api-key' missing. Running SCA without rate-limit protection."
                                        sh 'make scan'
                                    }
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
                        script {
                            echo "Publishing security metrics for ${env.SONAR_KEY}..."
                            
                            // Calculate Hybrid Bridge Logic (DooD)
                            def isContainer = sh(script: "[ -f /.dockerenv ] && echo true || echo false", returnStdout: true).trim()
                            def hostname = sh(script: "hostname", returnStdout: true).trim()
                            def dockerUser = sh(script: "id -u", returnStdout: true).trim() + ":" + sh(script: "id -g", returnStdout: true).trim()
                            
                            def mountOpts = (isContainer == "true") ? "--volumes-from ${hostname}" : "-v ${WORKSPACE}/${env.SERVICE_DIR}:/usr/src"
                            def workDir = (isContainer == "true") ? "${WORKSPACE}/${env.SERVICE_DIR}" : "/usr/src"

                            try {
                                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                                    sh "docker network create fintech-net || true"
                                    sh """
                                        docker run --rm --network fintech-net \
                                            --user ${dockerUser} \
                                            ${mountOpts} \
                                            -w ${workDir} \
                                            -e SONAR_HOST_URL=${SONAR_HOST_URL} \
                                            -e SONAR_TOKEN=${SONAR_TOKEN} \
                                            sonarsource/sonar-scanner-cli:latest \
                                            -Dsonar.projectKey=${env.SONAR_KEY} \
                                            -Dsonar.projectName="${env.PROJECT_NAME}" \
                                            -Dsonar.projectBaseDir=${workDir} \
                                            -Dsonar.qualitygate.wait=true
                                    """
                                }
                            } catch (Exception e) {
                                error "CRITICAL: SonarQube analysis failed. Details: ${e.message}"
                            }
                        }
                    }
                }
            }

            stage('Compliance Evidence') {
                steps {
                    script {
                        echo "Generating ISO 27001 / PCI-DSS Audit Evidence for ${env.PROJECT_NAME}..."
                        try {
                            withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                                def complianceScript = libraryResource 'scripts/export_compliance.sh'
                                writeFile file: 'export_compliance.sh', text: complianceScript
                                sh "chmod +x export_compliance.sh"
                                sh "./export_compliance.sh ${SONAR_HOST_URL} ${SONAR_KEY} ${SONAR_TOKEN}"
                            }
                        } catch (Exception e) {
                            echo "WARNING: Could not generate compliance report. (Requires 'sonar-token')."
                        }
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
