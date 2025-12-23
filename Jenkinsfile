// Master Orchestrator - Governance-as-an-Interface
// This root Jenkinsfile discovers all services and runs the decentralized governance check for each.

@Library('fintech-pipeline-library') _

pipeline {
    agent any
    
    stages {
        stage('Initialize Fleet') {
            steps {
                script {
                    // Automatically find all service directories
                    def serviceDirs = sh(script: "ls -d services/*", returnStdout: true).trim().split('\n')
                    env.SERVICES = serviceDirs.join(',')
                    echo "Found services for governance audit: ${env.SERVICES}"
                }
            }
        }

        stage('Trigger Governance Fleet') {
            steps {
                script {
                    def services = env.SERVICES.split(',')
                    def fleet = [:]

                    for (int i = 0; i < services.size(); i++) {
                        def servicePath = services[i]
                        def serviceName = servicePath.split('/').last()

                        fleet[serviceName] = {
                            echo "Triggering Governance Job for: ${serviceName}"
                            // Trigger the individual Jenkins job we created earlier
                            build job: serviceName, wait: true, propagate: true
                        }
                    }
                    parallel fleet
                }
            }
        }
    }
}
