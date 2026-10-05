pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'ls -la && ls -la hrms'   // confirm pom.xml is where we expect
      }
    }
    stage('Build & Test') {
      steps { dir('hrms') { sh 'mvn -B clean verify' } }
    }
    stage('sonarqube') {
      steps {
        dir('hrms') {
          withCredentials([string(credentialsId: 'sonar-cred', variable: 'T')]) {
            sh 'mvn -B sonar:sonar -Dsonar.host.url=http://13.127.79.203:9000 -Dsonar.token=$T'
          }
        }
      }
    }
    stage('Deploy') {
      steps { dir('hrms') { sh 'docker compose up -d --build' } }
    }
  }
}
