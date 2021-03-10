pipeline {
  agent none 
  stages {

    stage('Stage 0') {
      agent any
      steps {
          echo 'This stage is blocking the executor because of the "agent any"'
          milestone 1
            inputResponse = input([
              message: 'Bitte Bestätigen',
              submitterParameter: 'submitter',
              parameters: [
                [$class: 'ChoiceParameterDefinition', choices: 'Produktion\nTestumgebung', name: 'Umgebung:', description: 'Code-Änderungen werden in die ausgewählte Umgebung gepushed']
                ]
            ])
            milestone 2
            echo "Input response: ${inputResponse}"
          
        
        }
      }
    

    stage('Stage 1') {
      agent none
      steps {
        timeout(time: 1, unit: 'MINUTES') {
          script {
            echo 'This stage does not block an executor because of "agent none"'
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
}
