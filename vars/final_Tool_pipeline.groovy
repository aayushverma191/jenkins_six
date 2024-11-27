def call( String branch , String repoUrl ) {
  pipeline {
      agent any
      
      tools {
          ansible 'ansible'
      }
      environment {
          ANSIBLE_PLAY_CR_PATH = "${WORKSPACE}/assignment5/toolbook.yml"
          ANSIBLE_PLAY_DT_PATH = "${WORKSPACE}/assignment5/deletedata.yml"
          ANSIBLE_INVENTORY_PATH = "${WORKSPACE}/assignment5/aws_ec2.yml"
      }
  
      parameters {
          choice(name: 'select_environment', choices: ['create', 'delete'], description: 'Choose the action Create or Delete the Table')
      }
  
      stages {
          stage('clone'){
              steps{
                  echo "git......!! ${WORKSPACE} "
                    echo "Cloning repository..."
                      checkout([$class: 'GitSCM', branches: [[name: "*/${branch}"]],
                                userRemoteConfigs: [[url: repoUrl]]])
              }
          }
          stage('User_Approval'){
              steps{
                  input message: 'You need approval to execute further pipelines', ok: 'Approved'
                  echo "Approval Done "
              }
          }
          stage("for_create") {
              when { 
                  expression { params.select_environment == 'create' }
              }
              steps {
                    ansiblePlaybook credentialsId: '8d996de9-8b5a-4483-8b1a-4805fa3933df' , disableHostKeyChecking: true, installation: 'ansible', 
                    inventory: '${ANSIBLE_INVENTORY_PATH}', playbook: '${ANSIBLE_PLAY_CR_PATH}'
              }
          }
          stage("for_delete") {
              when { 
                  expression { params.select_environment == 'delete' }
              }
              steps {
                     ansiblePlaybook credentialsId: '8d996de9-8b5a-4483-8b1a-4805fa3933df' , disableHostKeyChecking: true, installation: 'ansible', 
                    inventory: '${ANSIBLE_INVENTORY_PATH}', playbook: '${ANSIBLE_PLAY_DT_PATH}'
              }
          }
      }
      post {
            success {
                    slackSend(channel: 'info', message: "Build Successful: JOB-Name:- ${JOB_NAME} Build_No.:- ${BUILD_NUMBER} & Build-URL:- ${BUILD_URL}")
                }
            failure {
                    slackSend(channel: 'info', message: "Build Failure: JOB-Name:- ${JOB_NAME} Build_No.:- ${BUILD_NUMBER} & Build-URL:- ${BUILD_URL}")
                }
      }
  }
}
