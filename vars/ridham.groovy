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
        ANSIBLE_KEY = "${WORKSPACE}/assignment5/ninja.pem"
        //REMOTE_USER = "ubuntu"
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
                script {
                    REMOTE_USER = input(
                        message: 'Enter Remote User', 
                        parameters: [choice(
                            choices: ['ubuntu', 'ec2-user'], 
                            description: 'Enter user name like: ubuntu for Debian and ec2-user for RedHat', 
                            name: 'user_name'
                        )]
                    )
                    echo "Using Remote User: ${REMOTE_USER}"
                    sh """
                        chmod 400 ${ANSIBLE_KEY}
                        ANSIBLE_SSH_ARGS='-o StrictHostKeyChecking=no' ansible-playbook -i ${ANSIBLE_INVENTORY_PATH} ${ANSIBLE_PLAY_CR_PATH} --private-key ${ANSIBLE_KEY} -u ${REMOTE_USER}
                    """
                }
            }
        }
        stage("for_delete") {
            when { 
                expression { params.select_environment == 'delete' }
            }
            steps {
                script {
                    REMOTE_USER = input(
                        message: 'Enter Remote User', 
                        parameters: [choice(
                            choices: ['ubuntu', 'ec2-user'], 
                            description: 'Enter user name like: ubuntu for Debian and ec2-user for RedHat', 
                            name: 'user_name'
                        )]
                    )
                    echo "Using Remote User: ${REMOTE_USER}"
                    sh """
                        chmod 400 ${ANSIBLE_KEY}
                        ANSIBLE_SSH_ARGS='-o StrictHostKeyChecking=no' ansible-playbook -i ${ANSIBLE_INVENTORY_PATH} ${ANSIBLE_PLAY_DT_PATH} --private-key ${ANSIBLE_KEY} -u ${REMOTE_USER}
                    """
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
