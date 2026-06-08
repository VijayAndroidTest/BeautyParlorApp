pipeline {
    agent any

    environment {
        KEYSTORE_PASSWORD = credentials('KEYSTORE_PASSWORD')
        KEY_PASSWORD = credentials('KEY_PASSWORD')
        FIREBASE_TOKEN = credentials('FIREBASE_TOKEN')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/VijayAndroidTest/BeautyParlorApp.git'
            }
        }

        stage('Debug') {
            steps {
                echo 'Checking workspace...'
                bat 'dir'
            }
        }

        stage('Clean and Build') {
            steps {
                // If gradlew.bat is in the root, remove 'dir' and use: bat 'gradlew.bat clean assembleRelease'
                // If it is in a folder (e.g., 'app'), keep dir('folderName')
                bat 'gradlew.bat clean assembleRelease'
            }
        }

        stage('Distribute to Firebase') {
            steps {
                withCredentials([file(credentialsId: 'beautyparlor-firebase', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                    bat 'gradlew.bat appDistributionUploadRelease'
                }
            }
        }
    }

    post {
        failure { echo "Build failed. Check the logs." }
        success { echo "Build and Distribution successful!" }
    }
}