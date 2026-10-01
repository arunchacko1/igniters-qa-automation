// Declarative pipeline mirroring .github/workflows/ci.yml, for a Jenkins
// agent with Docker, Java 21, and Node available. See docs/jenkins-setup.md
// for how to run this locally.
pipeline {
    agent any

    environment {
        POSTGRES_DB = 'igniters_qa'
        POSTGRES_USER = 'igniters'
        POSTGRES_PASSWORD = 'ci-password'
        QA_DEFECTS_ENABLED = 'false'
        BASE_URL = 'http://localhost:8080'
        API_BASE_URL = 'http://localhost:8080/api'
        DB_HOST = 'localhost'
        DB_PORT = '5432'
        SELENIUM_HEADLESS = 'true'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw -f sut-app/pom.xml -q -B package -DskipTests'
            }
        }

        stage('Start Environment') {
            steps {
                sh '''
                    docker compose up -d db
                    java -jar sut-app/target/sut-app.jar > sut.log 2>&1 &
                    for i in $(seq 1 30); do
                        curl -sf http://localhost:8080/login > /dev/null && exit 0
                        sleep 2
                    done
                    echo "SUT did not start in time" && cat sut.log && exit 1
                '''
            }
        }

        stage('Smoke') {
            steps {
                sh './mvnw -f qa-tests/pom.xml -B test -Psmoke'
            }
        }

        stage('Regression') {
            when {
                anyOf {
                    branch 'main'
                    changeRequest()
                }
            }
            steps {
                sh './mvnw -f qa-tests/pom.xml -B test -Pregression'
            }
        }

        stage('API') {
            steps {
                sh './mvnw -f qa-tests/pom.xml -B test -Papi'
                sh '''
                    npx --yes newman run postman/igniters-collection.json \
                        -e postman/igniters-environment.json \
                        --reporters cli,junit \
                        --reporter-junit-export newman-report/newman-junit.xml
                '''
            }
        }

        stage('Publish Reports') {
            steps {
                junit allowEmptyResults: true, testResults: 'qa-tests/target/surefire-reports/*.xml,newman-report/*.xml'
                archiveArtifacts artifacts: 'qa-tests/target/surefire-reports/**,qa-tests/target/screenshots/**,newman-report/**', allowEmptyArchive: true
            }
        }
    }

    post {
        always {
            stage('Teardown') {
                sh '''
                    pkill -f sut-app.jar || true
                    docker compose down -v || true
                '''
            }
        }
    }
}
