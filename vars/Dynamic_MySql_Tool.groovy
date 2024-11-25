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
        REMOTE_USER = "ubuntu"
        //ANSIBLE_SSH_ARGS='-o StrictHostKeyChecking=no' ansible -m ping -i /var/lib/jenkins/workspace/project-1/assignment5/aws_ec2.yml all --private-key /var/lib/jenkins/workspace/project-1/assignment5/ninja.pem -u root
    }
    
    stages{
         stage('clone'){
            steps{
                echo "git......!! ${WORKSPACE} "
                  echo "Cloning repository..."
                    checkout([$class: 'GitSCM',
                              branches: [[name: "*/${branch}"]],
                              userRemoteConfigs: [[url: repoUrl]]])
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
                    sh """
                        chmod 400 ${ANSIBLE_KEY}
                        ansible-playbook -i ${ANSIBLE_INVENTORY_PATH} ${ANSIBLE_PLAY_PATH} --private-key ${ANSIBLE_KEY} -u ${REMOTE_USER}
                    """
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

             
