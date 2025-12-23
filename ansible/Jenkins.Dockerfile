FROM jenkins/jenkins:lts
USER root
# Install make and other build essentials
RUN apt-get update && apt-get install -y make build-essential && apt-get clean
USER jenkins
