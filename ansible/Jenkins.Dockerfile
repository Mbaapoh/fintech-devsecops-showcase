FROM jenkins/jenkins:2.555.1-jdk21

USER root

RUN apt-get update && apt-get install -y \
    curl \
    git \
    make \
    uuid-runtime \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install plugins
COPY jenkins-config/plugins.txt /usr/share/jenkins/ref/plugins.txt

RUN jenkins-plugin-cli \
    --plugin-file /usr/share/jenkins/ref/plugins.txt \
    --verbose

# Install Microcks Jenkins plugin
RUN curl -fsSL -o /usr/share/jenkins/ref/plugins/microcks-jenkins-plugin.jpi \
    https://repo.maven.apache.org/maven2/io/github/microcks/microcks-jenkins-plugin/0.6.0/microcks-jenkins-plugin-0.6.0.hpi

# Create casc directory
RUN mkdir -p /var/jenkins_home/casc_configs

# Copy JCasC config
COPY jenkins-config/jenkins.yaml /var/jenkins_home/casc_configs/jenkins.yaml

# IMPORTANT: Copy Job DSL script
COPY jenkins-config/jobs.groovy /var/jenkins_home/jobs.groovy

# Disable setup wizard
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false \
               -Djava.awt.headless=true"

# JCasC config path
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc_configs/jenkins.yaml

USER jenkins