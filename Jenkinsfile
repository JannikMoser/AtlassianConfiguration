pipeline {
  agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15'))
    }

    parameters {
      choice(choices: ['T', 'P'], description: 'Environment', name: 'env')
      string(description: 'Version', name: 'version')

    }

  stages {
        stage('Init') {
            steps {
                script {
                    currentBuild.displayName = "${params.env} / ${params.version}"
                }
            }
        }

stage("Send Mail") {
      steps {
        script {
          currentBuild.displayName = "${params.env} / ${params.version}"
        }
        sendDeployMail(params.version, params.env,)
      }
}


  }

def sendDeployMail(version, env) {
  if (version == null || version.isEmpty()) {
      error 'Version not provided!'
  }
  def umgebung = [T: 'Testumgebung', P: 'Produktionsumgebung'][env]
  if (umgebung == null) {
      error 'Environment not provided!'
  }
 
  }
  def linksParagraph = links.isEmpty() ? '' : '<p>' + links.join('<br/>') + '</p>'
  def additionalText = (env == 'T') ? ' und steht zum Testen bereit' : '';
  emailext from: 'group.ch_it_el@baloise.ch', 
           to: 'jannik.moser@baloise.ch',
           subject: "${version} wurde nach ${umgebung} ausgeliefert",
           body: """
