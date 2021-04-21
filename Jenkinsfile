pipeline {
  agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15'))
    }

    parameters {
        choice(choices: ['Testumgebung', 'Produktion',], description: 'Umgebung auf welcher die Groovy Skripts deployed werden', name: 'Umgebung')
        String formUrlencode(Map params) {
      	params.collect { k,v -> URLEncoder.encode(k, "UTF-8") + "="  + URLEncoder.encode(v, "UTF-8")}.join('&')
}
        
    }

  stages {
    
    stage('Stage 2 Codeanalyse') {
      steps {
          script {
            def getXsrfToken(auth, env) {
	          String url = "http://${env}jira.baloisenet.com:8080/atlassian/secure/admin/EditAnnouncementBanner!default.jspa"
	          HttpCookie.parse("Set-Cookie:"+http_head(url, auth)['Set-Cookie'].join(', ')).find{it.name == 'atlassian.xsrf.token'}.value	
}
           
        }
      
    }
    }
    stage('Stage 2') {
      steps {
        script {
          echo ''
          sh 'sleep 15'
        }
      }
    }


    stage('Stage 3') {
      steps {
        script {
          echo ''
          sh 'sleep 15'
        }
      }
    }


  }
}
