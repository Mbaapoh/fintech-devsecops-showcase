FROM eclipse-temurin:17-jre-focal

ARG SONAR_SCANNER_VERSION=6.2.1.4610
ARG TARGETARCH

USER root

RUN apt-get update && \
    apt-get install -y curl unzip gnupg ca-certificates && \
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_18.x nodistro main" | tee /etc/apt/apt.sources.list.d/nodesource.list && \
    apt-get update && \
    apt-get install nodejs -y && \
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
