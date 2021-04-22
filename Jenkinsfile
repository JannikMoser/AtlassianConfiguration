pipeline {
  agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15'))
    }

    parameters {
        choice(choices: ['Testumgebung', 'Produktion',], description: 'Umgebung auf welcher die Groovy Skripts deployed werden', name: 'Umgebung')
    }

  stages {
    stage('Stage 2') {
      steps {
        notifyBitBucket state: 'INPROGRESS'
        sh def getXsrfToken(auth, env) 
    String url = "http://${env}jira.baloisenet.com:8080/atlassian/secure/admin/EditAnnouncementBanner!default.jspa"
    HttpCookie.parse("Set-Cookie:"+http_head(url, auth)['Set-Cookie'].join(', ')).find{it.name == 'atlassian.xsrf.token'}.value    

        
      }
    }
  }

 post {
        success {
            notifyBitBucket state: 'SUCCESSFUL'
        }

        fixed {
            mailTo status: 'SUCCESS', actuator: true, recipients: ['jannik.moser@baloise.ch'], logExtract: true
        }

        failure {
            notifyBitBucket state: 'FAILED', description: 'Der Pipelinebuild ist fehlgeschlagen'
            junit allowEmptyResults: true, testResults: '**/target/*-reports/TEST*.xml'
            mailTo status: 'FAILURE', actuator: true, recipients: ['jannik.moser@baloise.ch'], logExtract: true
        }
 }
}
