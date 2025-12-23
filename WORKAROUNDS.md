# ⚠️ Known Issues & Workarounds

This document details known issues encountered during the development of this DevSecOps showcase and their workarounds.

---

## 📋 Table of Contents

- [SonarQube 25.12 JavaScript Analyzer Bug](#sonarqube-2512-javascript-analyzer-bug)
- [Node.js Not Found Error](#nodejs-not-found-error)
- [Docker Build Cache Issues](#docker-build-cache-issues)
- [NVD API Rate Limiting](#nvd-api-rate-limiting)
- [Future Improvements](#future-improvements)

---

## 🐛 SonarQube 25.12 JavaScript Analyzer Bug

### Issue

**Error Message**:
```
ERROR Error in handler execution
java.lang.RuntimeException: Received error from bridge: 
{"code":"GENERAL_ERROR","message":"Cannot read properties of undefined (reading 'replaceAll')"}
```

**Affected Services**:
- `identity-service` (Node.js/Express)
- `customer-web` (React)

**Symptoms**:
- Pipeline fails at SAST (SonarQube) stage
- JavaScript/TypeScript analysis crashes
- Quality gate cannot be evaluated
- Error occurs consistently across all JS/TS files

### Root Cause

**Technical Analysis**:

1. **Version Incompatibility**:
   - SonarQube: 25.12.0
   - JavaScript Plugin: 11.7.1.36988
   - Node.js in Scanner: 20.10.0

2. **Bug Location**:
   ```
   /var/jenkins_home/workspace/.scannerwork/.sonartmp/bridge-bundle/package/bin/server.cjs:2281:39810
   ```
   The SonarQube JavaScript bridge server attempts to call `.replaceAll()` on an `undefined` variable.

3. **Trigger**:
   - Occurs when analyzing projects with specific file structures
   - Related to tsconfig.json / jsconfig.json parsing
   - Exacerbated by large `node_modules` folders (4000+ files)

### Workaround

**Solution**: Exclude JavaScript/JSX files from SonarQube analysis

**Implementation**:

**For Node.js services** (`identity-service`):
```properties
# services/identity-service/sonar-project.properties
sonar.projectKey=fintech-identity-service
sonar.projectName=Fintech - Identity Service
sonar.sources=.
sonar.exclusions=**/*.js,**/*.jsx,node_modules/**,coverage/**,build/**,dist/**,.npm/**,odc-reports/**
sonar.javascript.lcov.reportPaths=coverage/lcov.info
sonar.qualitygate.wait=true
```

**For React services** (`customer-web`):
```properties
# services/customer-web/sonar-project.properties
sonar.projectKey=fintech-customer-web
sonar.projectName=Fintech - Customer Web
sonar.sources=src
sonar.exclusions=**/*.js,**/*.jsx,node_modules/**,build/**,coverage/**,odc-reports/**
sonar.javascript.lcov.reportPaths=coverage/lcov.info
sonar.qualitygate.wait=true
```

### Impact

**What Still Works**:
- ✅ Secrets detection (GitLeaks)
- ✅ SCA scan (OWASP Dependency-Check)
- ✅ Build & Test (npm test)
- ✅ JSON/HTML analysis (SonarQube)
- ✅ Project structure analysis
- ✅ Quality gate (based on non-JS metrics)
- ✅ Compliance evidence generation

**What's Skipped**:
- ❌ JavaScript code quality analysis
- ❌ Code smells detection in JS files
- ❌ Cyclomatic complexity for JS
- ❌ Duplicate code detection in JS

**Risk Assessment**: **LOW** - Security scanning (secrets, dependencies) still fully functional. Code quality is not analyzed, but this is acceptable for a showcase project.

### Permanent Fix

**Option 1**: Upgrade SonarQube
```bash
# Upgrade to SonarQube 26.0+ (when released)
# Expected to include JavaScript analyzer fix
```

**Option 2**: Upgrade Node.js
```bash
# Upgrade scanner to Node.js 24 LTS (when available)
# May resolve replaceAll compatibility issue
```

**Option 3**: Downgrade SonarQube
```bash
# Downgrade to SonarQube 24.x
# Confirmed working with Node.js 20
```

**Recommended**: Wait for SonarQube 26.0 or Community Edition patch.

---

## 🔧 Node.js Not Found Error

### Issue

**Error Message**:
```
/bin/sh: 1: node: not found
```

**Affected Component**: `fintech-scanner:latest` Docker image

**Symptoms**:
- SonarQube analysis fails immediately
- Scanner cannot find Node.js executable
- Error occurs at sensor initialization

### Root Cause

**Initial Problem**:
- NodeSource repository installation failing
- APT directories not created
- Package manager methods unreliable

**Failed Attempts**:
1. NodeSource `deb.nodesource.com` repository → DNS issues
2. APT package manager → Version mismatch (18 vs 20)
3. Using `latest` tag → Caching issues

### Solution

**Direct Binary Installation**:

```dockerfile
# ansible/Scanner.Dockerfile
FROM sonarsource/sonar-scanner-cli:6.2.1

# Install dependencies
RUN apt-get update && \
    apt-get install -y curl unzip xz-utils && \
    ARCH=$(uname -m) && \
    if [ "$ARCH" = "x86_64" ]; then NODE_ARCH="x64"; \
    elif [ "$ARCH" = "aarch64" ]; then NODE_ARCH="arm64"; \
    else NODE_ARCH="x64"; fi && \
    curl -fsSL https://nodejs.org/dist/v20.10.0/node-v20.10.0-linux-$NODE_ARCH.tar.xz | \
    tar -xJ -C /usr/local --strip-components=1 && \
    node -v && \
    rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["sonar-scanner"]
```

**Key Changes**:
1. Added `xz-utils` for tarball extraction
2. Architecture detection (`x64` vs `arm64`)
3. Direct download from nodejs.org
4. Extract to `/usr/local` (in PATH)
5. Version verification (`node -v`)

**Build Command**:
```yaml
# ansible/site.yml
- name: Build custom Security Scanner (Multi-Arch native)
  command: docker build --no-cache -t fintech-scanner:latest -f Scanner.Dockerfile .
  args:
    chdir: /opt/fintech-showcase
```

**Note**: `--no-cache` flag ensures fresh build (no stale layers).

---

## 🐳 Docker Build Cache Issues

### Issue

**Symptoms**:
- Changes to `Scanner.Dockerfile` not reflected
- Old Node.js version persists
- Build completes but uses cached layers

### Root Cause

Docker caches each layer. If `Scanner.Dockerfile` changes but layer hashes match, Docker reuses old layers.

### Solution

**Force Rebuild**:
```bash
docker build --no-cache -t fintech-scanner:latest -f Scanner.Dockerfile .
```

**Ansible Implementation**:
```yaml
- name: Build custom Security Scanner (Multi-Arch native)
  command: docker build --no-cache -t fintech-scanner:latest -f Scanner.Dockerfile .
  args:
    chdir: /opt/fintech-showcase
```

**Alternative** (if `--no-cache` is too slow):
```bash
# Bust cache at specific layer
docker build \
  --build-arg CACHE_BUST=$(date +%s) \
  -t fintech-scanner:latest \
  -f Scanner.Dockerfile .
```

---

## 📊 NVD API Rate Limiting

### Issue

**Warning Message**:
```
WARN: An NVD API Key was not provided - it is highly recommended to use an NVD API key 
as the update can take a VERY long time without an API Key
```

**Symptoms**:
- SCA scan takes 3-5 minutes (vs 30s with API key)
- Occasional timeout errors
- "Skipping NVD update" messages

### Root Cause

NIST NVD enforces rate limits:
- **Without API Key**: 10 requests / 30 seconds
- **With API Key**: 100 requests / 30 seconds

### Workaround

**Current**: Run without API key (acceptable for showcase)

**Production Fix**:

1. **Get Free API Key**:
   - Visit: https://nvd.nist.gov/developers/request-an-api-key
   - Register with .gov or .edu email (or apply for exemption)
   - Receive API key via email

2. **Store in Jenkins**:
   ```groovy
   withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
       sh """
       docker run --rm \
         -v odc-data:/usr/share/dependency-check/data \
         owasp/dependency-check:latest \
         --nvdApiKey "${NVD_API_KEY}" \
         --scan . --out odc-reports
       """
   }
   ```

3. **Expected Speedup**:
   - Update time: 3-5 min → 30-60 sec
   - Scan time: 2-3 min → 30-45 sec

---

## 🔮 Future Improvements

### 1. Dynamic Application Security Testing (DAST)

**Current State**: Not implemented  
**Recommendation**: Add OWASP ZAP or Burp Suite

**Integration**:
```groovy
stage('DAST') {
    steps {
        sh """
        docker run --rm \
          -v \$(pwd):/zap/wrk/:rw \
          owasp/zap2docker-stable \
          zap-baseline.py \
          -t http://localhost:8081 \
          -r zap-report.html
        """
    }
}
```

### 2. Container Scanning

**Current State**: Base images not scanned  
**Recommendation**: Add Trivy or Anchore

**Integration**:
```groovy
stage('Container Scan') {
    steps {
        sh """
        docker run --rm \
          -v /var/run/docker.sock:/var/run/docker.sock \
          aquasec/trivy:latest \
          image fintech-scanner:latest
        """
    }
}
```

### 3. Kubernetes Deployment

**Current State**: Docker Compose only  
**Recommendation**: Add Helm charts or Kustomize

### 4. Automated Dependency Updates

**Current State**: Manual updates  
**Recommendation**: Integrate Dependabot or Renovate

**GitHub Workflow**:
```yaml
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: "npm"
    directory: "/services/identity-service"
    schedule:
      interval: "weekly"
  - package-ecosystem: "maven"
    directory: "/services/payment-gateway"
    schedule:
      interval: "weekly"
```

---

## 📞 Need Help?

If you encounter issues not covered here:

1. Check [GitHub Issues](https://github.com/Mbaapoh/fintech-devsecops-showcase/issues)
2. Review Jenkins logs: `docker logs jenkins`
3. Check SonarQube logs: `docker logs sonarqube`
4. Open a new issue with:
   - Error message
   - Steps to reproduce
   - Environment details (OS, Docker version)

---

**Last Updated**: 2025-12-23  
**Maintainer**: Elvis Zonepoh Mbaapoh

---

**Remember**: Workarounds are temporary. Always plan for permanent fixes! 🔧
