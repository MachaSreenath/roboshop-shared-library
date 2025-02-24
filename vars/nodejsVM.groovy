def call(Map configMap){
pipeline {
    agent {
        node {
            label 'AGENT-1'
        }
    }
    environment {
        packageVersion = ''
        nexusUrl = '187.21.8.89:8081' //private_ip of nexus instance, here we must put port number also.
    }
    options {
        timeout(time: 1, unit: 'HOURS')
        disableConcurrentBuilds()
    }
    parameters {

        booleanParam(name: 'Deploy', defaultValue: false, description: 'Toggle this value')

    }
    // build
    stages {
        stage('Get the version') {
            steps {
                script {
                    def packageJson = readJSON file: 'package.json' 
                    packageVersion = packageJson.version
                    echo "application version: $packageVersion"
                    }
                }
            }
        stage('Install Dependencies') {
            steps {
                sh """
                    npm install
                """
            }
        }
        stage('Unit tests') {
            steps {
                sh """
                    echo "unit test will run here"
                """
            }
        }
        stage('Sonar Scan') {
            steps {
                sh """
                    sonar-scanner
                """
            }
        }  
        stage('Build') {
            steps {
                sh """
                    ls -la
                    zip -q -r ${configMap.component}.zip ./* -x ".git" -x "*.zip"
                    ls -ltr
                """
            }
        }
        stage('Publish Artifact') {
            steps {
                 nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: pipelineGlobals.nexusURL(),
                    groupId: 'com.roboshop',
                    version: "${packageVersion}",
                    repository: "${configMap.component}",
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId: "${configMap.component}",
                        classifier: '',
                        file: "${configMap.component}.zip",
                        type: 'zip']
                                ]
                             )
                    }
                }
        stage('Deploy') {
            when {
                expression {
                    params.Deploy == 'true'
                }
            }
            steps {
                script {
                        def params = [
                            string(name: 'version', value:"$packageVersion"),
                            string(name: 'environment', value:"dev")
                        ]
                        build job: "${configMap.component}-deploy", wait: true, parameters: params
                        }
                    }
                }
             }
    // post build
    post { 
        always { 
            echo 'I will always say Hello again!'
            deleteDir()
        }
        failure { 
            echo 'this runs when pipeline is failed, used generally to send some alerts'
        }
        success{
            echo 'I will say Hello when pipeline is success'
        }
    }
}
}