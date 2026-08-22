pipeline {
    agent any

    environment {
        DEPLOY_DIR = "/srv/backbone"
        JAR_NAME = "backbone.jar"
        HEALTH_URL = "http://127.0.0.1:8000/actuator/health"
        COMPOSE_PROFILE = "prod"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage("Clean Workspace") {
            steps {
                cleanWs()  // Deletes everything in workspace
            }
        }

        stage("Checkout") {
            steps { checkout scm }
        }

        stage("Build JAR") {
            steps {
                sh '''
                    set -e
                    if [ -f mvnw ]; then
                        ./mvnw -q -DskipTests package
                    elif [ -f gradlew ]; then
                        ./gradlew -q bootJar -x test
                    else
                        echo "No mvnw/gradlew found. Add one, or adjust Jenkinsfile."
                        exit 1
                    fi
                '''
            }
        }

        stage("Locate Built JAR") {
            steps {
                script {
                // Try common outputs
                def jarPath = sh(
                script: '''
                    set -e
                    JAR=""
                    if ls target/*.jar > /dev/null 2>&1; then
                        JAR=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*.original" | head -n 1)
                    elif ls build/libs/*.jar > /dev/null 2>&1; then
                        JAR=$(find build/libs -maxdepth 1 -type f -name "*.jar" ! -name "*-plain.jar" | head -n 1)
                    fi
                    [ -n "$JAR" ] || (echo "Could not find built jar in target/ or build/libs/" && exit 1)
                    jar tf "$JAR" | grep -q '^BOOT-INF/lib/' || (echo "Built artifact is not an executable Spring Boot jar: $JAR" && exit 1)
                    echo "$JAR"
                ''',
                returnStdout: true
                ).trim()

                env.BUILT_JAR = jarPath
                echo "Built jar:  ${env.BUILT_JAR}"
                }
            }
        }

        stage("Backup current JAR (for rollback)") {
            steps {
                sh '''
                    set -e
                    mkdir -p "${DEPLOY_DIR}/backups" "${DEPLOY_DIR}/target"

                    if [ -f "${DEPLOY_DIR}/target/${JAR_NAME}" ]; then
                        TS=$(date +%Y%m%d_%H%M%S)
                        cp "${DEPLOY_DIR}/target/${JAR_NAME}" "${DEPLOY_DIR}/backups/${JAR_NAME}.${TS}"
                        echo "Backed up existing jar to backups/${JAR_NAME}.${TS}"
                    else
                        echo "No existing jar to backup (first deploy ?)"
                    fi
                '''
            }
        }

        stage("Deploy new JAR + docker compose up") {
          steps {
            sh '''
              set -e
              mkdir -p "${DEPLOY_DIR}/target" "${DEPLOY_DIR}/backups" "${DEPLOY_DIR}/otel"

              # Publish atomically so a running JVM never reads a half-written jar.
              cp "${BUILT_JAR}" "${DEPLOY_DIR}/target/${JAR_NAME}.new"
              mv -f "${DEPLOY_DIR}/target/${JAR_NAME}.new" "${DEPLOY_DIR}/target/${JAR_NAME}"

              # Publish the OTel Java agent the same way, atomically.
              cp .otel/opentelemetry-javaagent.jar "${DEPLOY_DIR}/otel/opentelemetry-javaagent.jar.new"
              mv -f "${DEPLOY_DIR}/otel/opentelemetry-javaagent.jar.new" "${DEPLOY_DIR}/otel/opentelemetry-javaagent.jar"

              # Copy compose into deploy dir so docker compose can find it
              cp compose.yaml "${DEPLOY_DIR}/compose.yaml"

              cd "${DEPLOY_DIR}"
              ls -la
              docker compose -f compose.yaml --profile "${COMPOSE_PROFILE}" up -d --force-recreate --remove-orphans
              docker compose -f compose.yaml ps
            '''
          }
        }



        stage("Health Check") {
            steps {
                script {
                    def ok = sh(
                        script: '''
                            set +e
                            for i in $(seq 1 20); do
                                curl -fsS "${HEALTH_URL}" | grep -q '"status":"UP"'
                                if [ $? -eq 0 ]; then
                                    echo "UP"
                                    exit 0
                                fi
                                echo "Waiting for app... ($i/20)"
                                sleep 3
                            done
                            echo "DOWN"
                            exit 1
                        ''',
                        returnStatus: true
                    ) == 0

                    if (!ok) {
                        error("Health check failed")
                    }
                }
            }
        }
    }

    post {
        success {
            withCredentials([string(credentialsId: 'discord_webhook', variable: 'DISCORD_WEBHOOK')]) {
                    sh '''
                        set -e
                        MSG="✅ Deploy SUCCESS: backbone is healthy on https://backbone.keteku.dev/actuator/health"
                        curl -fsS -H "Content-Type: application/json" \
                             -d "{\\"content\\":\\"${MSG}\\"}" \
                             ${DISCORD_WEBHOOK}
                    '''
            }
        }

        failure {
            echo "Deploy failed - attempting rollback.."

            // rollback logic: restore most recent backup, redeploy compose, recheck health, notify discord
            script {
                    def rolledBack = sh(
                      script: '''
                        set +e
                        cd "${DEPLOY_DIR}"

                        LAST_BACKUP=$(ls -1t "${DEPLOY_DIR}/backups/${JAR_NAME}."* 2>/dev/null | head -n 1)
                        if [ -z "$LAST_BACKUP" ]; then
                          echo "NO_BACKUP"
                          exit 0
                        fi

                        echo "Rolling back using $LAST_BACKUP"
                        cp "$LAST_BACKUP" "${DEPLOY_DIR}/target/${JAR_NAME}.rollback"
                        mv -f "${DEPLOY_DIR}/target/${JAR_NAME}.rollback" "${DEPLOY_DIR}/target/${JAR_NAME}"

                        docker compose -f compose.yaml --profile "${COMPOSE_PROFILE}" up -d --force-recreate --remove-orphans

                        # Re-check health after rollback
                        for i in $(seq 1 20); do
                          curl -fsS "${HEALTH_URL}" | grep -q '"status":"UP"'
                          if [ $? -eq 0 ]; then
                            echo "ROLLBACK_OK"
                            exit 0
                          fi
                          sleep 3
                        done

                        echo "ROLLBACK_FAILED"
                        exit 0
                      ''',
                      returnStdout: true
                    ).trim()

                    withCredentials([string(credentialsId: 'discord_webhook', variable: 'DISCORD_WEBHOOK')]) {
                              if (rolledBack.contains("NO_BACKUP")) {
                                sh '''
                                  set -e
                                  MSG="❌ Deploy FAILED and NO BACKUP found to rollback. Check Jenkins logs + container logs."
                                  curl -fsS -H "Content-Type: application/json" \
                                    -d "{\"content\":\"${MSG}\"}" \
                                    "$DISCORD_WEBHOOK" >/dev/null
                                '''
                              } else if (rolledBack.contains("ROLLBACK_OK")) {
                                sh '''
                                    set -e
                                    MSG="⚠️ Deploy FAILED, but ROLLBACK SUCCEEDED. Service is back UP on https://backbone.keteku.dev/actuator/health."
                                    curl -fsS -H "Content-Type: application/json" \
                                         -d "{\\"content\\":\\"${MSG}\\"}" \
                                         ${DISCORD_WEBHOOK}
                                '''
                              } else {
                                sh '''
                                    set -e
                                    MSG="🚨 Deploy AND Rollback FAILED! Manual intervention required."
                                    curl -fsS -H "Content-Type: application/json" \
                                         -d "{\\"content\\":\\"${MSG}\\"}" \
                                         ${DISCORD_WEBHOOK}
                                '''
                              }
                    }
            }
        }

        always {
              sh '''
                set +e
                cd "${DEPLOY_DIR}" || exit 0
                docker compose -f compose.yaml ps || true
                docker logs --tail=1000 spring-backbone || true
              '''}
    }
}
