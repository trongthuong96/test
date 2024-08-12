node("linux-agent-with-docker") {
    docker.image('maven:3.6.3-jdk-8').inside {
        git 'https://github.com/takari/maven-wrapper.git'
        // Unset MAVEN_CONFIG if necessary
        sh 'unset MAVEN_CONFIG && ./mvnw dependency:go-offline -B'
    }
}
