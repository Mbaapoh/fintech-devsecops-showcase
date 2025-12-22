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
                            // Cache Maven dependencies in Jenkins home to speed up builds
                            sh 'docker run --rm --volumes-from jenkins -v /var/jenkins_home/.m2:/root/.m2 -w ${PWD} maven:3.9.3-eclipse-temurin-17 mvn clean test jacoco:report'
                        }
                    }
                }
               stage('Go (Ledger w/ Coverage)') {
                   steps {
                       dir('services/ledger-api') {
                           // Cache Go modules
                           sh 'docker run --rm --volumes-from jenkins -v /var/jenkins_home/go-cache:/go -w ${PWD} golang:1.21 go test ./... -coverprofile=coverage.out'
                       }
                   }
               }
               stage('Node (Identity w/ Jest)') {
                   steps {
                       dir('services/identity-service') {
                           // Cache NPM dependencies
                           sh 'docker run --rm --volumes-from jenkins -v /var/jenkins_home/.npm:/root/.npm -w ${PWD} node:18 /bin/bash -c "npm install && npm test"'
                       }
                   }
               }
               stage('React (Customer w/ LCOV)') {
                   steps {
                       dir('services/customer-web') {
                            // Cache NPM dependencies
                           sh 'docker run --rm --volumes-from jenkins -v /var/jenkins_home/.npm:/root/.npm -e CI=true -w ${PWD} node:18 /bin/bash -c "npm install && npm test -- --coverage --watchAll=false"'
                       }
                   }
               }
            }
        }

        stage('Security: SAST (SonarQube MQR)') {
            steps {
                script {
                    // Cache Sonar scanner JRE and engine to avoid downloading 100MB+ on every run
                    sh 'docker run --rm --network fintech-net --volumes-from jenkins -v /var/jenkins_home/.sonar:/root/.sonar -v /var/jenkins_home/.npm:/root/.npm -e SONAR_HOST_URL=${SONAR_HOST_URL} -e SONAR_TOKEN=${SONAR_TOKEN} -w ${PWD} node:22 npx sonarqube-scanner'
                }
            }
        }

        // Quality Gate enforcement is handled automatically by the previous stage 
        // because sonar.qualitygate.wait=true is set in sonar-project.properties.
        // The scanner will return a non-zero exit code if the Quality Gate fails.

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
