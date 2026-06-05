pineline{
    agent any

    environment {
        IMAGE_NAME = "okits02/identity-service"
        IMAGE_TAG = "v1.0.0"

        APP_EC2_USER = "ubuntu"
        APP_EC2_HOST = "54.252.192.111"
        APP_DIR = "/home/ubuntu/identity-service"
    }

    stages{
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Prepare') {
            steps {
                sh 'chmod +x mvnw'
                sh 'java -version'
                sh 'docker --version'
            }
        }

        stage('Unit Test') {
            steps {
                sh './mvnw test'
            }
            post {
                 always {
                   junit 'target/surefire-reports/*.xml'
                 }
            }
        }

        stage('Integration Test') {
            steps {
                sh './mvnw verify'
            }

            post{
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }

        stage('OWASP Dependency Scan') {
            steps {
               sh './mvnw org.owasp:dependency-check-maven:check -DskipTests'
            }
        }

        stage('Build docker image') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG'
            }
        }

        stage('Docker Image Scan') {
             steps {
                 sh 'trivy image --severity HIGH,CRITICAL $IMAGE_NAME:$IMAGE_TAG || true'
             }
        }

        stage('Push docker image') {
            steps {
                withCredentials([usernamePassword(
                credentialsId: 'Docker-credentials',
                usernameVariable: 'phamanhtu13042002@gmail.com',
                passwordVariable: 'Phamtu13042002')
                ]) {
                    sh '''
                        echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
                        docker push $IMAGE_NAME:$IMAGE_TAG
                    '''
                }
            }
        }

        stage('Deploy to App EC2') {
            steps {
                sshagent(['app-ec2-ssh-key']) {
                sh '''
                   ssh -o StrictHostKeyChecking=no $APP_EC2_USER@$APP_EC2_HOST "
                       cd $APP_DIR &&
                       sudo docker compose pull identity-service &&
                       sudo docker compose up -d
                   "
                '''
                }
            }
        }
    }
}