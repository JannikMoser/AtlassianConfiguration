pipeline {
  agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15'))
    }

    //Bei den Parametern, habe ich mich für den choice-Parameter entschieden, weil ich mehrere Umgebungen zur Auswahl habe
    parameters {
        choice(description: '', name: 'env', choices: 'Testumgebung\nProduktionsumgebung')
        string(name: 'name', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
    }

  //In dieser Stage, ist konfiguriert, wann und wie die Pipeline getriggered wird
  stages {
    stage('Stage 1') {
      steps {
        notifyBitBucket state: 'INPROGRESS'
      }
    }
    //In dieser Stage, ist der Deploymentschritt der Groovy Skripte definiert
    stage('Stage 2 - Deployment Groovy Skripts') {
    steps {
      script {
          deployRestEndPoint(params.name, params.env = '') {
          println "deploying $name to $env"
          String url  = "https://${env}jira.baloisenet.com/atlassian/rest/scriptrunner/latest/custom/customadmin/com.onresolve.scriptrunner.canned.common.rest.CustomRestEndpoint"
          String scriptText = filePath("src/RESTEndpoints/$name").readToString()
          String payload = """{"FIELD_INLINE_SCRIPT":"${StringEscapeUtils.escapeJavaScript(scriptText)}","canned-script":"com.onresolve.scriptrunner.canned.common.rest.CustomRestEndpoint"}"""
          http_post(url, payload, 'application/json')
          }
        getXsrfToken(env) {
        String url = "http://${env}jira.baloisenet.com:8080/atlassian/secure/admin/EditAnnouncementBanner!default.jspa"
        HttpCookie.parse('Set-Cookie:' + http_head(url)['Set-Cookie'].join(', ')).find { it.name == 'atlassian.xsrf.token' }.value
        }
      }
    }
    }
  }

 post {
        success {
            notifyBitBucket state: 'SUCCESSFUL'
        }

        fixed {
            emailext body: '''Hallo Jannik! Der Build der Pipeline ist vollständig durchgelaufen und die RESTEndpoints wurden deployed
            Mit freundlichen Grüssen''', subject: 'Automatisierte Verteilung von Atlassian Tool Updates Jira', to: 'jannik.moser@baloise.ch'
        }

        failure {
            notifyBitBucket state: 'FAILED', description: 'Der Pipelinebuild ist fehlgeschlagen'
            junit allowEmptyResults: true, testResults: '**/target/*-reports/TEST*.xml'
            emailext attachLog: true, body: '''Hallo Jannik! Der Build der Pipeline ist fehlgeschlagen.
      Bitte überprüfe die Logfiles, welche sich im Anhang der Mail befinden.
      Mit freundlichen Grüssen''', subject: 'Automatisierte Verteilung von Atlassian Tool Updates Jira', to: 'jannik.moser@baloise.ch'
        }
 }
}



