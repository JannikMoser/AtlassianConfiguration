pipeline {
  agent none 
  stages {

    stage('Stage 0') {
      agent any
      steps {
        script {
	def deployRestEndPoint(name, auth, env = '') {
	println "deploying $name to $env"
	String url  = "https://${env}jira.baloisenet.com/atlassian/rest/scriptrunner/latest/custom/customadmin/com.onresolve.scriptrunner.canned.common.rest.CustomRestEndpoint"
	String scriptText = filePath("src/RESTEndpoints/$name").readToString()
	String payload = """{"FIELD_INLINE_SCRIPT":"${StringEscapeUtils.escapeJavaScript(scriptText)}","canned-script":"com.onresolve.scriptrunner.canned.common.rest.CustomRestEndpoint"}"""
	http_post(url, auth, payload, 'application/json')
	
          echo 'This stage is blocking the executor because of the "agent any"'
          milestone 1
            inputResponse = input([
              message           : 'Bitte Bestätigen',
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
      }
    }

    stage('Stage 2') {
      agent none
      steps {
        timeout(time: 1, unit: 'MINUTES') {
          script {
            echo 'This stage does not block an executor because of "agent none"'
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
