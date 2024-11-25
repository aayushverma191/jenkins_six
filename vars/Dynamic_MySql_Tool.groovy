def call(String branch, String repoUrl) {

pipeline {
    agent any
    
    tools {
        ansible 'ansible'
    }

     environment {
        ANSIBLE_PLAY_PATH = "${WORKSPACE}/assignment5/toolbook.yml"
        ANSIBLE_INVENTORY_PATH = "${WORKSPACE}/assignment5/aws_ec2.yml"
        ANSIBLE_KEY = "${WORKSPACE}/assignment5/ninja.pem"
    }
    
    stages{
        stage('clone'){
            steps{
                echo "git......!! ${WORKSPACE} "
                  echo "Cloning repository..."
                    checkout([$class: 'GitSCM',
                              branches: [[name: "*/${branch}"]],
                              userRemoteConfigs: [[url: repoUrl]]])
               // git branch: 'main' , url: 'https://github.com/aayushverma191/ansible_tool.git'
            }
        }
        stage('User_Approval'){
            steps{
                input message: 'do you want to install MYSQL', ok: 'Approved'
                echo "Approval Done "
            }
        }
        stage('Playbook_Execution'){
            steps {
                script{
                        sh """
                            ansible-playbook -i ${ANSIBLE_INVENTORY_PATH} ${ANSIBLE_PLAY_PATH} --private-key ${ANSIBLE_KEY}
                        """
                    //}
                }
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
