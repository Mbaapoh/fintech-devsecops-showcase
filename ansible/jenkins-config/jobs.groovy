pipelineJob('mtn-momo-contract-sync') {
    description('Enterprise API Contract Sync')

    definition {
        cps {
            script("""
pipeline {
    agent any
    stages {
        stage('Start') {
            steps {
                echo 'Starting MTN MoMo Contract Sync'
            }
        }
        stage('Validate OpenAPI') {
            steps {
                echo 'Validating OpenAPI specification'
            }
        }
        stage('Microcks Push') {
            steps {
                echo 'Pushing specification to Microcks'
                microcksImport(
                    serverName: 'microcks-demo',
                    specificationFiles: 'api-specs/mtn-momo-collections-v1.yaml'
                )
            }
        }
        stage('Cleanup') {
            steps {
                cleanWs()
            }
        }
    }
}
""")
            sandbox()
        }
    }
}