FROM eclipse-temurin:17-jre-focal

ARG SONAR_SCANNER_VERSION=6.2.1.4610
ARG TARGETARCH

USER root

RUN apt-get update && \
    apt-get install -y curl unzip xz-utils && \
    ARCH=$(uname -m) && \
    if [ "$ARCH" = "x86_64" ]; then NODE_ARCH="x64"; elif [ "$ARCH" = "aarch64" ]; then NODE_ARCH="arm64"; else NODE_ARCH="x64"; fi && \
    curl -fsSL https://nodejs.org/dist/v20.10.0/node-v20.10.0-linux-$NODE_ARCH.tar.xz | tar -xJ -C /usr/local --strip-components=1 && \
    node -v && \
    rm -rf /var/lib/apt/lists/*

# Map Docker architecture names to SonarScanner download names
RUN if [ "$TARGETARCH" = "arm64" ]; then \
    SCANNER_ARCH="linux-x64"; \
    # Note: As of late 2024, SonarSource provides native aarch64 zips
    # but they are often named differently in the download URL.
    # For maximum production stability across Intel/ARM, we download the universal/native binary
    curl -Lo /tmp/sonar-scanner.zip https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux-aarch64.zip; \
    else \
    curl -Lo /tmp/sonar-scanner.zip https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux-x64.zip; \
    fi && \
    unzip /tmp/sonar-scanner.zip -d /opt && \
    rm /tmp/sonar-scanner.zip && \
    mv /opt/sonar-scanner-${SONAR_SCANNER_VERSION}-linux-* /opt/sonar-scanner

ENV PATH="/opt/sonar-scanner/bin:${PATH}"

WORKDIR /usr/src

ENTRYPOINT ["sonar-scanner"]
