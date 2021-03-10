pipeline {
  agent none 
  stages {
    
    stage('Stage 0') {
      agent none
      steps {
        timeout(time: 1, unit: 'MINUTES') {
          script {
            echo 'In dieser Phase kann ausgewählt werden, wohin die Scriptrunner Add-ons deployed werden'
            milestone 1
            inputResponse = input([
              message           : 'Bitte bestätigen.',
              submitterParameter: 'submitter',
              parameters        : [
                [$class: 'ChoiceParameterDefinition', choices: 'Produktion\nTestumgebung', name: 'Umgebung:', description: 'Code-Änderungen werden in die ausgewählte Umgebung gepushed']
                
              ]
            ])
            milestone 2
            echo "Input response: ${inputResponse}"
          }
        }
      }
  post {
  aborted {
    echo 'Der User hat keine Auswahl getroffen --> Pipeline wird erneut gebaut'
  }
  failure {
   echo   'Es gibt einen Fehler in der Pipeline --> Pipeline wird erneut gebaut' 
  }
  unsuccessful {
    echo 'Die Pipeline konnte nicht richtig gebaut werden --> Pipeline wird erneut gebaut'
  }
}
    }

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

