pipeline {
  agent none // agent can only be overwritten if the initial value is 'none'
  stages {

    stage('Stage 1') {
      agent any
      steps {
        script {
          echo 'This stage is blocking the executor because of the "agent any"'
        }
      }
    }

    stage('Stage 2') {
      agent none
      steps {
        timeout(time: 1, unit: 'MINUTES') {
          script {
            echo 'This stage does not block an executor because of "agent none"'
            milestone 1
            inputResponse = input([
              message           : 'Bitte Bestätigen',
              submitterParameter: 'submitter',
              parameters        : [
                [$class: 'ChoiceParameterDefinition', choices: 'Produktion\Integration', name: 'Umgebung:', description: 'Code-Änderungen werden in die ausgewählte Umgebung gepushed']
              
            ])
            milestone 2
            echo "Input response: ${inputResponse}"
          }
        }
      }
    }

    stage('Stage 3') {
      agent any
      steps {
        script {
          echo 'This stage is blocking the executor because of the "agent any"'
          sh 'sleep 15'
        }
      }
    }


  }
}
