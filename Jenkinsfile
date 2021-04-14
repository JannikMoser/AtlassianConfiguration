pipeline {
  agent none 

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15'))
    }

    parameters {
        choice(choices: ['Testumgebung', 'Produktion',], description: 'Umgebung auf welcher die Groovy Skripts deployed werden', name: 'Umgebung')
        
    }

  stages {
    

    stage('Stage 1') {
      agent any
      steps {
        timeout(time: 1, unit: 'MINUTES') {
          script {
            echo 'This stage does not block an executor because of "agent none"'
        }
      }
    }
    }
    stage('Stage 2') {
      agent any
      steps {
        script {
          echo ''
          sh 'sleep 15'
        }
      }
    }


    stage('Stage 3') {
      agent any
      steps {
        script {
          echo ''
          sh 'sleep 15'
        }
      }
    }


  }
}
