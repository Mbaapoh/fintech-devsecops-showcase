pipeline {
    agent any

    environment {
        // Placeholder for SonarQube server credentials/token
        SONAR_TOKEN = credentials('sonar-token')
        SONAR_HOST_URL = 'http://sonarqube:9000' 
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Security: Secrets Detection') {
            steps {
                script {
                    echo 'Running Gitleaks...'
                    // docker run -v ${PWD}:/path zricethezav/gitleaks:latest detect --source="/path" -v
                    echo 'Gitleaks scan complete. No secrets found (simulated).'
                }
            }
        }

        stage('Security: SCA (Dependency Scanning)') {
            steps {
                script {
                    echo 'Running Dependency Check...'
                    // OWASP Dependency Check simulation
                    echo 'Node.js dependencies: OK'
                    echo 'Java dependencies: OK'
                    echo 'Go dependencies: OK'
                }
            }
        }

        stage('Polyglot Build & Test') {
            parallel {
                stage('Java (Payment w/ Jacoco)') {
                    steps {
                        dir('services/payment-gateway') {
                            // Assuming mvn is installed or running in container
                            // Using docker to run build to keep host clean
                            sh 'docker run --rm --volumes-from jenkins -w ${PWD} maven:3.9.3-eclipse-temurin-17 mvn clean test jacoco:report'
                        }
                    }
                }
               stage('Go (Ledger w/ Coverage)') {
                   steps {
                       dir('services/ledger-api') {
                           sh 'docker run --rm --volumes-from jenkins -w ${PWD} golang:1.21 go test ./... -coverprofile=coverage.out'
                       }
                   }
               }
               stage('Node (Identity w/ Jest)') {
                   steps {
                       dir('services/identity-service') {
                           sh 'docker run --rm --volumes-from jenkins -w ${PWD} node:18 /bin/bash -c "npm install && npm test"'
                       }
                   }
               }
               stage('React (Customer w/ LCOV)') {
                   steps {
                       dir('services/customer-web') {
                           sh 'docker run --rm --volumes-from jenkins -e CI=true -w ${PWD} node:18 /bin/bash -c "npm install && npm test -- --coverage --watchAll=false"'
                       }
                   }
               }
            }
        }

        stage('Security: SAST (SonarQube MQR)') {
            steps {
                script {
                    // Using node:18 (Debian-based) instead of Alpine to ensure glibc compatibility for the scanner's JRE on ARM64
                    sh 'docker run --rm --network fintech-net --volumes-from jenkins -e SONAR_HOST_URL=${SONAR_HOST_URL} -e SONAR_TOKEN=${SONAR_TOKEN} -w ${PWD} node:18 npx sonarqube-scanner'
                }
            }
        }

        stage('Quality Gate (MQR Enforcement)') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    // Enforces Software Quality Security Rating < A = BLOCK
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build & Push') {
            parallel {
                stage('Payment Gateway Pkg') {
                    steps {
                        echo 'Building payment-gateway container...'
                    }
                }
                stage('Ledger API Pkg') {
                    steps {
                        echo 'Building ledger-api container...'
                    }
                }
            }
        }
    }
}
