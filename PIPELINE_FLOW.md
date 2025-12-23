# 🔄 DevSecOps Pipeline Flow

Complete visual documentation of the CI/CD pipeline stages, execution flow, and decision points.

---

## 📊 High-Level Pipeline Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                   USER: Commits Code to GitHub                   │
│                    (git push origin main)                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────▼──────────────┐
                │    GitHub Webhook         │
                │   (Trigger Jenkins)       │
                └────────────┬──────────────┘
                             │
        ┌────────────────────▼────────────────────┐
        │   Jenkins: Orchestrator Fleet          │
        │   Discovers all microservices          │
        │   Triggers parallel builds             │
        └────────────┬───────────────────────────┘
                     │
     ┌───────────────┴──────────────┬──────────────┬───────────────┐
     │                              │              │               │
┌────▼────────┐  ┌────────────────▼──┐  ┌────────▼─────┐  ┌─────▼──────────┐
│ Payment     │  │  Ledger API       │  │ Identity     │  │ Customer Web   │
│ Gateway     │  │  (Go)             │  │ Service      │  │ (React)        │
│ (Java)      │  │                   │  │ (Node.js)    │  │                │
└────┬────────┘  └─────────┬─────────┘  └──────┬───────┘  └────────┬───────┘
     │                     │                    │                   │
     └─────────────────────┴────────────────────┴───────────────────┘
                                    │
                        ┌───────────▼──────────────┐
                        │   All Services PASSED    │
                        │   ✅ Quality Gate Met    │
                        │   ✅ Security Scanned    │
                        │   ✅ Compliance Evidence │
                        └───────────┬──────────────┘
                                    │
                        ┌───────────▼──────────────┐
                        │  Orchestrator: SUCCESS   │
                        │  Deployment Complete     │
                        └──────────────────────────┘
```

---

## 🎯 Individual Service Pipeline

Each microservice follows the same 5-stage pipeline:

### Stage Flow Diagram

```
START
  │
  ▼
┌─────────────────────────────────────────────────────────────┐
│ STAGE 1: Governance & Security Gates (Parallel)            │
│                                                             │
│  ┌──────────────────────┐    ┌──────────────────────────┐ │
│  │ Secrets Detection    │    │    SCA Scan              │ │
│  │ (GitLeaks)           │    │ (OWASP Dependency-Check) │ │
│  │                      │    │                          │ │
│  │ ⏱ ~40ms              │    │ ⏱ 12-171s (Java: longest)│ │
│  │ ❌ FAIL → HALT       │    │ ⚠️  WARN → Continue      │ │
│  └──────────┬───────────┘    └──────────┬───────────────┘ │
│             └──────────┬──────────────────┘                │
└────────────────────────┼─────────────────────────────────┘
                         │
                         ▼ ✅ PASS
┌─────────────────────────────────────────────────────────────┐
│ STAGE 2: Build & Test                                       │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Language-Specific Build:                            │  │
│  │  • Java:   mvn clean package + mvn test             │  │
│  │  • Go:     go build + go test -coverprofile         │  │
│  │  • Node:   npm install + npm test --coverage        │  │
│  │  • React:  npm install + npm test --coverage        │  │
│  │                                                      │  │
│  │  ⏱ 1-3 minutes                                       │  │
│  │  ❌ FAIL → HALT                                      │  │
│  └──────────────────────────┬───────────────────────────┘  │
└─────────────────────────────┼──────────────────────────────┘
                              │
                              ▼ ✅ PASS
┌─────────────────────────────────────────────────────────────┐
│ STAGE 3: SAST (SonarQube)                                   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  1. Connect to SonarQube Server (port 9000)         │  │
│  │  2. Upload source code + test coverage              │  │
│  │  3. Execute analysis (language-specific sensors)    │  │
│  │  4. Calculate quality metrics                       │  │
│  │  5. Wait for quality gate result                    │  │
│  │                                                      │  │
│  │  Decision Point:                                     │  │
│  │                                                      │  │
│  │  ┌──────────────────┐         ┌────────────────┐   │  │
│  │  │ Quality Gate     │         │ Quality Gate   │   │  │
│  │  │ STATUS: PASSED   │         │ STATUS: FAILED │   │  │
│  │  └──────┬───────────┘         └────────┬───────┘   │  │
│  │         │                              │            │  │
│  │         ▼ ✅ Continue                   ▼ ❌ HALT   │  │
│  │                                                      │  │
│  │  ⏱ 11-16 seconds                                     │  │
│  └──────────────────────────┬───────────────────────────┘  │
└─────────────────────────────┼──────────────────────────────┘
                              │
                              ▼ ✅ PASS
┌─────────────────────────────────────────────────────────────┐
│ STAGE 4: Compliance Evidence                                │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  1. Execute export_compliance.sh script             │  │
│  │  2. Connect to SonarQube API                        │  │
│  │  3. Fetch project metrics & quality gate status     │  │
│  │  4. Generate compliance_evidence.md                 │  │
│  │  5. Archive artifacts in Jenkins                    │  │
│  │                                                      │  │
│  │  Generated Files:                                    │  │
│  │  • compliance_evidence.md                           │  │
│  │  • dependency-check-report.html                     │  │
│  │  • gitleaks-report.json                             │  │
│  │                                                      │  │
│  │  ⏱ 2-5 seconds                                       │  │
│  └──────────────────────────┬───────────────────────────┘  │
└─────────────────────────────┼──────────────────────────────┘
                              │
                              ▼ ✅ COMPLETE
┌─────────────────────────────────────────────────────────────┐
│ STAGE 5: Package & Push (OPTIONAL)                          │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Condition: GIT_BRANCH == 'main'                    │  │
│  │                                                      │  │
│  │  If TRUE:                                            │  │
│  │  1. Build Docker image                              │  │
│  │  2. Tag: service:${GIT_COMMIT}-${BUILD_NUMBER}      │  │
│  │  3. Push to container registry                      │  │
│  │  4. Update deployment manifests                     │  │
│  │                                                      │  │
│  │  If FALSE:                                           │  │
│  │  • Stage skipped                                     │  │
│  │                                                      │  │
│  │  ⏱ 30-60 seconds (if enabled)                        │  │
│  └──────────────────────────┬───────────────────────────┘  │
└─────────────────────────────┼──────────────────────────────┘
                              │
                              ▼
                         ┌────────┐
                         │ SUCCESS│
                         └────────┘
```

---

## ⏱️ Execution Timing Breakdown

### Per-Service Timings

```
┌──────────────────┬──────────┬────────┬─────────┬──────────┬────────────┐
│ Service          │ Secrets  │  SCA   │  Build  │   SAST   │ Compliance │
├──────────────────┼──────────┼────────┼─────────┼──────────┼────────────┤
│ payment-gateway  │  ~40ms   │  12s   │  45s    │   14s    │    3s      │
│ ledger-api       │  ~40ms   │  12s   │  30s    │   11s    │    3s      │
│ identity-service │  ~40ms   │  12s   │  25s    │   13s    │    3s      │
│ customer-web     │  ~45ms   │ 171s   │  35s    │   17s    │    3s      │
└──────────────────┴──────────┴────────┴─────────┴──────────┴────────────┘

Total Sequential: ~74s (payment-gateway) to ~269s (customer-web)
Total Parallel (Orchestrator): ~269s (limited by slowest service)
```

**Note**: SCA timing varies based on:
- Number of dependencies (customer-web: 1303, identity-service: 267)
- NVD database freshness (first run slower)
- Docker volume caching

---

## 🔀 Decision Points & Exit Conditions

### Critical Gates

```
┌────────────────────────────────────────────────────────────┐
│                  Decision Matrix                            │
├──────────────────┬─────────────┬───────────┬───────────────┤
│ Stage            │ Condition   │ Action    │ Exit Code     │
├──────────────────┼─────────────┼───────────┼───────────────┤
│ Secrets Detect   │ Leaks Found │ ❌ HALT   │ FAILURE       │
│ Secrets Detect   │ Clean       │ ✅ Pass   │ Continue      │
├──────────────────┼─────────────┼───────────┼───────────────┤
│ SCA Scan         │ Critical+   │ ⚠️  Warn  │ Continue*     │
│ SCA Scan         │ Completed   │ ✅ Pass   │ Continue      │
├──────────────────┼─────────────┼───────────┼───────────────┤
│ Build & Test     │ Tests Fail  │ ❌ HALT   │ FAILURE       │
│ Build & Test     │ Tests Pass  │ ✅ Pass   │ Continue      │
├──────────────────┼─────────────┼───────────┼───────────────┤
│ SAST             │ Gate FAILED │ ❌ HALT   │ FAILURE       │
│ SAST             │ Gate PASSED │ ✅ Pass   │ Continue      │
├──────────────────┼─────────────┼───────────┼───────────────┤
│ Compliance       │ Error       │ ⚠️  Warn  │ Continue      │
│ Compliance       │ Success     │ ✅ Pass   │ Continue      │
├──────────────────┼─────────────┼───────────┼───────────────┤
│ Package & Push   │ Branch≠main │ ⏭️  Skip  │ Continue      │
│ Package & Push   │ Branch=main │ 🐳 Build  │ Continue      │
└──────────────────┴─────────────┴───────────┴───────────────┘

* MQR (Minimum Quality Requirements) can be configured to fail on Critical CVEs
```

---

## 🔄 Orchestrator Execution Flow

### Parallel Service Deployment

```
Jenkins Orchestrator Job
         │
         ├─ Stage 1: Initialize Fleet
         │  └─ Discover services (ls -d services/*)
         │
         ├─ Stage 2: Trigger Governance Fleet (PARALLEL)
         │  ├─────────────────┬────────────┬──────────────┬─────────────┐
         │  │                 │            │              │             │
         │  ▼                 ▼            ▼              ▼             │
         │  customer-web      ledger-api   identity-      payment-      │
         │  (React)           (Go)         service        gateway       │
         │                                 (Node.js)      (Java)        │
         │  │                 │            │              │             │
         │  │ Run Full        │ Run Full   │ Run Full     │ Run Full    │
         │  │ Pipeline        │ Pipeline   │ Pipeline     │ Pipeline    │
         │  │ (5 stages)      │ (5 stages) │ (5 stages)   │ (5 stages)  │
         │  │                 │            │              │             │
         │  ▼                 ▼            ▼              ▼             │
         │  ✅ SUCCESS        ✅ SUCCESS   ✅ SUCCESS     ✅ SUCCESS     │
         │  │                 │            │              │             │
         │  └─────────────────┴────────────┴──────────────┴─────────────┘
         │                            │
         └────────────────────────────┘
                                      │
                                      ▼
                         ┌────────────────────────┐
                         │ Orchestrator: SUCCESS  │
                         │ All 4 Services Deployed│
                         └────────────────────────┘
```

**Wait Strategy**: Jenkins `build job` step with `wait: true`
- Each service build waits for completion
- Parallel execution continues independent of others
- Orchestrator completes when ALL services finish

---

## 📦 Artifact Flow

### From Code to Deployment

```
┌──────────────────────────────────────────────────────────────┐
│                     Source Code (GitHub)                      │
└────────────────────────────┬─────────────────────────────────┘
                             │
                ┌────────────▼──────────────┐
                │  Jenkins Checkout         │
                │  (Local workspace)        │
                └────────────┬──────────────┘
                             │
        ┌────────────────────┴────────────────────┐
        │                                         │
        ▼                                         ▼
┌──────────────────┐                   ┌──────────────────┐
│ Build Artifacts  │                   │ Security Reports │
│ • JAR files      │                   │ • gitleaks.json  │
│ • Go binaries    │                   │ • odc-*.html     │
│ • npm packages   │                   │ • compliance.md  │
└───────┬──────────┘                   └────────┬─────────┘
        │                                       │
        └───────────────┬───────────────────────┘
                        │
                ┌───────▼────────┐
                │  SonarQube DB  │
                │  (PostgreSQL)  │
                │  • Code metrics│
                │  • Issues      │
                │  • History     │
                └───────┬────────┘
                        │
                ┌───────▼────────┐
                │ Jenkins Archive│
                │ • compliance.md│
                │ • Build logs   │
                │ • Test results │
                └───────┬────────┘
                        │
                ┌───────▼────────┐
                │ Docker Registry│
                │ (Optional)     │
                │ • Tagged images│
                └────────────────┘
```

---

## 🔒 Security Checkpoints

### Multi-Layer Security Validation

```
Layer 1: Pre-Build
  ↓
  ├─ GitLeaks Scan
  │  └─ Regex pattern matching for secrets
  │     Exit: Immediate failure if found
  ↓
Layer 2: Build Time
  ↓
  ├─ OWASP Dependency-Check
  │  └─ CVE database lookup for all dependencies
  │     Exit: Warning (configurable to fail)
  ↓
Layer 3: Post-Build
  ↓
  ├─ SonarQube SAST
  │  └─ Static code analysis + quality gate
  │     Exit: Failure if gate not met
  ↓
Layer 4: Compliance
  ↓
  ├─ Evidence Generation
  │  └─ ISO 27001 / PCI-DSS report
  │     Exit: Warning only (non-blocking)
  ↓
Layer 5: Deployment (Optional)
  ↓
  └─ Container Security Scan (Future)
     └─ Image vulnerability scanning
        Exit: Failure on critical issues
```

---

## 📊 Metrics Collection Points

### Where Data is Gathered

```
┌─────────────────────────────────────────────────────────────┐
│                    Metrics Collection                        │
├────────────┬────────────────────────────────────────────────┤
│ Stage      │ Metrics Collected                              │
├────────────┼────────────────────────────────────────────────┤
│ Secrets    │ • Scan duration                                │
│            │ • Secrets found count                          │
│            │ • Patterns matched                             │
├────────────┼────────────────────────────────────────────────┤
│ SCA        │ • Dependencies scanned                         │
│            │ • CVEs found (by severity)                     │
│            │ • Analysis duration                            │
│            │ • Database age                                 │
├────────────┼────────────────────────────────────────────────┤
│ Build/Test │ • Build duration                               │
│            │ • Test count (passed/failed)                   │
│            │ • Code coverage %                              │
│            │ • Artifact size                                │
├────────────┼────────────────────────────────────────────────┤
│ SAST       │ • Lines of code                                │
│            │ • Bugs / Vulnerabilities / Code Smells         │
│            │ • Technical debt                               │
│            │ • Quality gate status                          │
│            │ • Security rating                              │
├────────────┼────────────────────────────────────────────────┤
│ Overall    │ • Total pipeline duration                      │
│            │ • Success/failure rate                         │
│            │ • Mean time to recovery (MTTR)                 │
└────────────┴────────────────────────────────────────────────┘
```

**Visualization**: Available in:
- Jenkins → Build Trends
- SonarQube → Projects Dashboard
- Compliance Evidence (archived artifacts)

---

## 🔄 Retry & Rollback Strategy

### Error Handling

```
┌────────────────────────────────────┐
│     Pipeline Failure Occurred      │
└────────────┬───────────────────────┘
             │
    ┌────────▼────────┐
    │ Stage Failed?   │
    └────────┬────────┘
             │
   ┌─────────┴──────────┐
   │                    │
   ▼                    ▼
Transient          Persistent
Error              Error
   │                    │
   ├─ Network timeout   ├─ Build failure
   ├─ API rate limit    ├─ Test failure
   └─ Service restart   ├─ Quality gate fail
   │                    └─ Secrets detected
   │                    │
   ▼                    ▼
Retry                No Retry
(3 attempts)         (Immediate halt)
   │                    │
   ▼                    ▼
Success           ┌──────────────┐
   │              │ Notify Team  │
   └──────────────►│ • Email      │
                  │ • Slack      │
                  │ • Dashboard  │
                  └──────────────┘
```

**Rollback**:
- Not automated (showcase project)
- Production: Use blue-green deployment
- Kubernetes: `kubectl rollout undo`

---

## 📈 Continuous Improvement Loop

```
┌──────────────────────────────────────────────────────┐
│                  CI/CD Feedback Loop                  │
└───────────────────┬──────────────────────────────────┘
                    │
    ┌───────────────▼──────────────┐
    │  1. Commit & Push to GitHub  │
    └───────────────┬──────────────┘
                    │
    ┌───────────────▼──────────────┐
    │  2. Jenkins Pipeline Runs    │
    └───────────────┬──────────────┘
                    │
    ┌───────────────▼──────────────┐
    │  3. Security Scans Execute   │
    └───────────────┬──────────────┘
                    │
    ┌───────────────▼──────────────┐
    │  4. Quality Gate Evaluated   │
    └───────────────┬──────────────┘
                    │
          ┌─────────┴─────────┐
          │                   │
          ▼                   ▼
    ┌──────────┐        ┌──────────┐
    │  PASSED  │        │  FAILED  │
    └─────┬────┘        └─────┬────┘
          │                   │
          │            ┌──────▼──────┐
          │            │ 5. Review   │
          │            │ Findings    │
          │            └──────┬──────┘
          │                   │
          │            ┌──────▼──────┐
          │            │ 6. Fix Code │
          │            │ or Config   │
          │            └──────┬──────┘
          │                   │
          │                   │
          └───────────────────┘
                    │
    ┌───────────────▼──────────────┐
    │  7. Deploy to Environment    │
    └───────────────┬──────────────┘
                    │
    ┌───────────────▼──────────────┐
    │  8. Monitor & Collect Metrics│
    └───────────────┬──────────────┘
                    │
    ┌───────────────▼──────────────┐
    │  9. Optimize Pipeline        │
    │  (Based on metrics)          │
    └──────────────────────────────┘
```

**Optimization Targets**:
- Reduce SCA scan time (NVD API key)
- Improve test coverage
- Add DAST scanning
- Implement canary deployments

---

**Last Updated**: 2025-12-23  
**Pipeline Version**: v1.0  
**Maintained By**: Elvis Zonepoh Mbaapoh

---

**The pipeline is always evolving. Measure, improve, repeat!** 📈
