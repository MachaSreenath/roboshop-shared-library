def call(Map configMap){
pipeline {
    agent {
        node {
            label 'AGENT-1'
        }
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
                    echo "unit test here"
                """
            }
        }
        stage('Sonar Scan') {
            steps {
                sh """
                    echo "here the command is sonar-scanner"
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
        stage('Publishing Artifact') {
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
                    params.Deploy
                }
            }
            steps {
                script {
                        def params = [
                            string(name: 'version', value:"$packageVersion"),
                            string(name: 'environment', value:"dev")
                        ]
                        build job: "../${configMap.component}-deploy", wait: true, parameters: params
                        }
                    }
                }
             }
    // post build
    post { 
        always { 
            echo 'pipeline fails r success i will execute'
            deleteDir()
        }
        failure { 
            echo 'your pipeline is failed'
        }
        success{
            echo 'your pipeline is success'
        }
    }
}
}

// in 93rd line there we put "../" because we created a folder in jenkins, inside that folder there are two folders again catalogue and catalogue-deploy. Now this pipeline is in the catalogue so to go to the deployment process it has to go one step back and go to the catalogue-deploy folder, that's why we wrote.