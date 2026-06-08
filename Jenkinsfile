pipeline {
    agent any

    environment {
        // These match the IDs you create in Jenkins > Manage Jenkins > Credentials
        // KEYSTORE_PASSWORD: The password for the .jks file
        // KEY_PASSWORD: The password for the specific alias inside the .jks
        // FIREBASE_TOKEN: Your Firebase CLI CI token
        // These IDs match the ones shown in your screenshot (171.png)
        KEYSTORE_PASSWORD = credentials('KEYSTORE_PASSWORD')
        KEY_PASSWORD = credentials('KEY_PASSWORD')
        FIREBASE_TOKEN = credentials('FIREBASE_TOKEN')
        FIREBASE_JSON = credentials('beautyparlor-firebase')
    }

    stages {
        stage('Clean') {
            steps {
                // Ensure a fresh build environment
                sh './gradlew clean'
            }
        }

        stage('Build Release APK') {
            steps {
                // Use withCredentials to safely expose the JSON key file path
                withCredentials([file(credentialsId: 'beautyparlor-firebase', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                    sh './gradlew assembleRelease'
                }
            }
        }

        stage('Distribute to Firebase') {
            steps {
                // Upload to Firebase App Distribution
                // The FIREBASE_TOKEN environment variable is automatically picked up by the plugin
                sh './gradlew appDistributionUploadRelease'
            }
        }
    }

    post {
        failure {
            echo "Build failed. Check the logs."
        }
        success {
            echo "Build and Distribution successful!"
        }
    }
}