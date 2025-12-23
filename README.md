# 🏦 Fintech DevSecOps Showcase

[![Jenkins](https://img.shields.io/badge/Jenkins-2.479-D24939?logo=jenkins&logoColor=white)](https://www.jenkins.io/)
[![SonarQube](https://img.shields.io/badge/SonarQube-25.12-4E9BCD?logo=sonarqube&logoColor=white)](https://www.sonarqube.org/)
[![Docker](https://img.shields.io/badge/Docker-27.0-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> **Enterprise-grade DevSecOps pipeline demonstrating security-first microservices deployment with multi-language support, comprehensive SAST/SCA scanning, and automated compliance evidence generation.**

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Security Features](#-security-features)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Pipeline Stages](#-pipeline-stages)
- [Microservices](#-microservices)
- [Security Scanning](#-security-scanning)
- [Compliance & Governance](#-compliance--governance)
- [Metrics & Monitoring](#-metrics--monitoring)
- [Known Issues & Workarounds](#-known-issues--workarounds)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview

This project demonstrates a **production-ready DevSecOps pipeline** that orchestrates the deployment of 4 polyglot microservices with integrated security scanning, quality gates, and compliance evidence generation.

### Key Highlights

- ✅ **Multi-Language Support**: Java, Go, Node.js, React
- ✅ **Security-First**: Secrets detection, SCA, SAST at every stage
- ✅ **Quality Gates**: Automated enforcement via SonarQube
- ✅ **Compliance**: ISO 27001 / PCI-DSS evidence generation
- ✅ **Orchestration**: Parallel deployment of microservices
- ✅ **Infrastructure as Code**: Ansible-driven deployment
- ✅ **Containerization**: Docker-based build & scan tools

---

## 🏗 Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         JENKINS ORCHESTRATOR                         │
│                    (Parallel Multi-Service Deployment)               │
└────────────┬────────────────────────────────────────────────────────┘
             │
    ┌────────┴────────┬────────────┬────────────┬─────────────┐
    │                 │            │            │             │
┌───▼──────────┐ ┌───▼────────┐ ┌─▼──────────┐ ┌▼────────────────┐
│Payment Gateway│ │ Ledger API │ │ Identity   │ │  Customer Web   │
│ (Spring Boot) │ │   (Echo)   │ │ (Express)  │ │    (React)      │
│   Port 8081   │ │ Port 8082  │ │ Port 3001  │ │   Port 3000     │
└───────────────┘ └────────────┘ └────────────┘ └─────────────────┘
        │                │             │               │
    ┌───▼────────────────▼─────────────▼───────────────▼──────┐
    │              DevSecOps Pipeline Stages                   │
    │  ┌─────────────────────────────────────────────────┐    │
    │  │ 1. Governance & Security Gates                  │    │
    │  │    → Secrets Detection (GitLeaks)               │    │
    │  │    → SCA Scan (OWASP Dependency-Check)          │    │
    │  ├─────────────────────────────────────────────────┤    │
    │  │ 2. Build & Test                                 │    │
    │  │    → Unit Tests + Coverage                      │    │
    │  ├─────────────────────────────────────────────────┤    │
    │  │ 3. SAST (SonarQube)                            │    │
    │  │    → Code Quality Analysis                      │    │
    │  │    → Quality Gate Enforcement                   │    │
    │  ├─────────────────────────────────────────────────┤    │
    │  │ 4. Compliance Evidence                          │    │
    │  │    → ISO 27001 / PCI-DSS Reports               │    │
    │  │    → Artifact Archival                         │    │
    │  ├─────────────────────────────────────────────────┤    │
    │  │ 5. Package & Push (Optional)                    │    │
    │  │    → Docker Image Build                         │    │
    │  │    → Container Registry Push                    │    │
    │  └─────────────────────────────────────────────────┘    │
    └──────────────────────────────────────────────────────────┘
                             │
                ┌────────────▼──────────────┐
                │   Infrastructure Layer     │
                │  ┌──────────────────────┐ │
                │  │ SonarQube 25.12      │ │
                │  │ (Port 9000)          │ │
                │  ├──────────────────────┤ │
                │  │ PostgreSQL 16        │ │
                │  │ (Port 5432)          │ │
                │  ├──────────────────────┤ │
                │  │ Jenkins 2.479        │ │
                │  │ (Port 8080)          │ │
                │  └──────────────────────┘ │
                └───────────────────────────┘
```

### Network Topology

```
External Access
     │
┌────▼─────────────────────────────────────────────┐
│            Host Machine (Public IP)              │
│                                                   │
│  ┌──────────────────────────────────────────┐   │
│  │         Docker Bridge Network             │   │
│  │                                           │   │
│  │  ┌─────────────┐    ┌─────────────┐     │   │
│  │  │  Jenkins    │───▶│  SonarQube  │     │   │
│  │  │  :8080      │    │  :9000      │     │   │
│  │  └─────────────┘    └──────┬──────┘     │   │
│  │         │                   │            │   │
│  │         │            ┌──────▼──────┐    │   │
│  │         │            │ PostgreSQL  │    │   │
│  │         │            │  :5432      │    │   │
│  │         │            └─────────────┘    │   │
│  │         │                                │   │
│  │  ┌──────▼──────────────────────────┐   │   │
│  │  │    fintech-scanner:latest       │   │   │
│  │  │  (Custom Security Scanner)      │   │   │
│  │  │  - SonarScanner CLI 6.2.1       │   │   │
│  │  │  - Node.js 20 LTS               │   │   │
│  │  │  - Java 17 + Java 21            │   │   │
│  │  └─────────────────────────────────┘   │   │
│  └──────────────────────────────────────────┘   │
└───────────────────────────────────────────────────┘
```

---

## 🛠 Tech Stack

### Core Technologies

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **CI/CD** | Jenkins | 2.479 | Orchestration & automation |
| **SAST** | SonarQube | 25.12 | Static code analysis |
| **Database** | PostgreSQL | 16 | SonarQube backend |
| **Container** | Docker | 27.0+ | Containerization |
| **IaC** | Ansible | 2.10+ | Infrastructure provisioning |

### Microservices Stack

| Service | Language/Framework | Build Tool | Port |
|---------|-------------------|------------|------|
| `payment-gateway` | Java 21 / Spring Boot | Maven 3.9 | 8081 |
| `ledger-api` | Go 1.21 / Echo | Go Modules | 8082 |
| `identity-service` | Node.js 18 / Express | npm | 3001 |
| `customer-web` | React 18 | npm | 3000 |

### Security Tools

| Tool | Version | Purpose |
|------|---------|---------|
| **GitLeaks** | Latest | Secrets detection |
| **OWASP Dependency-Check** | Latest | SCA (dependency vulnerabilities) |
| **SonarScanner CLI** | 6.2.1 | SAST integration |
| **RetireJS** | (via ODC) | JavaScript vulnerability scanning |

---

## 🔒 Security Features

### Defense in Depth

```
┌─────────────────────────────────────────────────────────┐
│                    Security Layers                       │
├─────────────────────────────────────────────────────────┤
│ 1. Pre-Commit                                           │
│    → Git hooks (future enhancement)                     │
├─────────────────────────────────────────────────────────┤
│ 2. Secrets Detection                                    │
│    → GitLeaks scans every commit                        │
│    → Redacted output for security                       │
├─────────────────────────────────────────────────────────┤
│ 3. Software Composition Analysis (SCA)                  │
│    → OWASP Dependency-Check                            │
│    → NVD CVE database integration                       │
│    → Multi-format reports (XML, JSON, SARIF, HTML)     │
├─────────────────────────────────────────────────────────┤
│ 4. Static Application Security Testing (SAST)          │
│    → SonarQube quality gate enforcement                │
│    → Code coverage tracking                            │
│    → Security hotspot detection                        │
├─────────────────────────────────────────────────────────┤
│ 5. Quality Gates                                        │
│    → Mandatory quality gate pass                       │
│    → Pipeline halts on failure                         │
│    → Compliance evidence generated                     │
└─────────────────────────────────────────────────────────┘
```

### Security Scanning Coverage

- **Secrets**: API keys, passwords, tokens, credentials
- **Dependencies**: CVEs, known vulnerabilities, outdated packages
- **Code Quality**: Bugs, code smells, security hotspots
- **Compliance**: ISO 27001, PCI-DSS evidence

See [SECURITY.md](./SECURITY.md) for detailed security controls.

---

## 📦 Prerequisites

### System Requirements

- **OS**: Linux (tested on Ubuntu 22.04) or macOS
- **CPU**: 4+ cores recommended
- **RAM**: 8GB minimum, 16GB recommended
- **Disk**: 20GB free space
- **Network**: Internet access for Docker images & NVD database

### Software Dependencies

```bash
# Required
Docker >= 27.0
Docker Compose >= 2.20
Ansible >= 2.10
Git >= 2.30

# Optional (for local development)
Java 21 (for payment-gateway)
Go 1.21+ (for ledger-api)
Node.js 18+ (for identity-service & customer-web)
```

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/Mbaapoh/fintech-devsecops-showcase.git
cd fintech-devsecops-showcase
```

### 2. Deploy Infrastructure

```bash
cd ansible
ansible-playbook -i localhost, site.yml
```

This will deploy:
- ✅ PostgreSQL database
- ✅ SonarQube server
- ✅ Jenkins with pre-configured jobs
- ✅ Custom security scanner image

### 3. Access Services (via Reverse Proxy)

If you deployed using the Nginx reverse proxy role, services are available at:

| Service | URL | Backend Port |
|---------|-----|--------------|
| Jenkins | https://jenkins.okay.cm | 8080 |
| SonarQube | https://sonarqube.okay.cm | 9000 |
| Payment Gateway | https://payment.okay.cm | 8081 |
| Ledger API | https://ledger.okay.cm | 8082 |
| Identity Service | https://identity.okay.cm | 3001 |
| Customer Web | https://customer.okay.cm | 3000 |

*Note: Replace `okay.cm` with your actual domain in `ansible/playbooks/reverse-proxy.yml`.*

### 4. Local Access (Direct)

| Service | URL | Credentials |
|---------|-----|-------------|
| Jenkins | http://localhost:8080 | Auto-configured |
| SonarQube | http://localhost:9000 | admin / admin |
| Payment Gateway | http://localhost:8081 | N/A |
| Ledger API | http://localhost:8082 | N/A |
| Identity Service | http://localhost:3001 | N/A |
| Customer Web | http://localhost:3000 | N/A |

### 4. Run the Orchestrator

```bash
# In Jenkins UI:
# Navigate to "orchestrator-fleet" job
# Click "Build Now"
```

The orchestrator will deploy all 4 microservices in parallel.

---

## 🔄 Pipeline Stages

### Stage 1: Governance & Security Gates

**Parallel Execution**:

```
┌─────────────────────┐    ┌─────────────────────┐
│ Secrets Detection   │    │    SCA Scan         │
│                     │    │                     │
│ • GitLeaks scan     │    │ • ODC analysis      │
│ • Zero tolerance    │    │ • NVD CVE check     │
│ • Redacted logs     │    │ • Multi-format      │
└─────────────────────┘    └─────────────────────┘
```

**Exit Criteria**: No secrets found, dependencies analyzed

---

### Stage 2: Build & Test

```
┌─────────────────────────────────────┐
│         Build & Test                │
│                                     │
│ • Language-specific build           │
│ • Unit tests execution              │
│ • Code coverage collection          │
│ • Artifact generation               │
└─────────────────────────────────────┘
```

**Per-Service Commands**:

| Service | Build Command | Test Command |
|---------|--------------|--------------|
| `payment-gateway` | `mvn clean package -DskipTests` | `mvn test` |
| `ledger-api` | `go build` | `go test -v -coverprofile=coverage.out ./...` |
| `identity-service` | `npm install` | `npm test -- --coverage --watchAll=false` |
| `customer-web` | `npm install` | `npm test -- --coverage --watchAll=false` |

---

### Stage 3: SAST (SonarQube)

```
┌─────────────────────────────────────────┐
│       SonarQube Analysis                │
│                                         │
│ 1. Connect to SonarQube server          │
│ 2. Upload source code & coverage        │
│ 3. Execute quality analysis             │
│ 4. Wait for quality gate result         │
│ 5. ✅ PASS → Continue                   │
│    ❌ FAIL → Halt deployment            │
└─────────────────────────────────────────┘
```

**Quality Gate Criteria**:
- Code coverage > 0% (configured per project)
- No critical/blocker issues
- Security hotspots reviewed
- Maintainability rating ≥ A

---

### Stage 4: Compliance Evidence

```
┌─────────────────────────────────────────┐
│    Compliance Evidence Generation       │
│                                         │
│ • Export SonarQube metrics              │
│ • Generate ISO 27001 evidence           │
│ • Archive compliance reports            │
│ • Upload to Jenkins artifacts           │
└─────────────────────────────────────────┘
```

**Generated Artifacts**:
- `compliance_evidence.md` (ISO 27001 / PCI-DSS)
- `dependency-check-report.html` (SCA)
- `gitleaks-report.json` (Secrets scan)

---

### Stage 5: Package & Push (Optional)

```
┌─────────────────────────────────────────┐
│      Docker Image Build & Push          │
│                                         │
│ • Build optimized Docker image          │
│ • Tag with Git SHA + build number       │
│ • Push to container registry            │
│ • Update deployment manifests           │
└─────────────────────────────────────────┘
```

**Note**: Enabled only when `GIT_BRANCH == 'main'`

---

## 🎯 Microservices

### 1. Payment Gateway (Java/Spring Boot)

**Path**: `services/payment-gateway`

```yaml
Technology: Java 21, Spring Boot 3.2
Build Tool: Maven 3.9
Port: 8081
Purpose: Core payment processing service
Key Features:
  - RESTful API for payment transactions
  - Spring Boot Actuator health checks
  - Maven-based dependency management
```

**Endpoints**:
- `GET /actuator/health` - Health check
- `POST /api/payments` - Process payment (placeholder)

---

### 2. Ledger API (Go/Echo)

**Path**: `services/ledger-api`

```yaml
Technology: Go 1.21, Echo Framework
Build Tool: Go Modules
Port: 8082
Purpose: Transaction ledger & audit trail
Key Features:
  - High-performance Go-based API
  - Echo framework for routing
  - Comprehensive test coverage
```

**Endpoints**:
- `GET /health` - Health check
- `GET /api/ledger` - Fetch ledger entries (placeholder)

---

### 3. Identity Service (Node.js/Express)

**Path**: `services/identity-service`

```yaml
Technology: Node.js 18, Express.js
Build Tool: npm
Port: 3001
Purpose: User authentication & authorization
Key Features:
  - Express-based REST API
  - JWT token management (planned)
  - Session handling
```

**Endpoints**:
- `GET /health` - Health check
- `POST /api/auth/login` - User login (placeholder)

---

### 4. Customer Web (React)

**Path**: `services/customer-web`

```yaml
Technology: React 18, Create React App
Build Tool: npm
Port: 3000
Purpose: Customer-facing web interface
Key Features:
  - Modern React SPA
  - Responsive design
  - Integration with backend APIs
```

**Routes**:
- `/` - Home page
- `/login` - Login page (placeholder)
- `/dashboard` - User dashboard (placeholder)

---

## 🔍 Security Scanning

### GitLeaks (Secrets Detection)

**Configuration**: Default rules + custom patterns

```bash
# Scanned patterns:
- API keys (AWS, Azure, GCP)
- Database credentials
- Private keys (RSA, SSH)
- OAuth tokens
- Generic secrets (password=, secret=)
```

**Output**: `gitleaks-report.json` (redacted)

---

### OWASP Dependency-Check (SCA)

**NVD Database**: Synchronized every 4 hours

```bash
# Analyzers enabled:
- Node.js Package Analyzer
- Maven Central Analyzer
- Go Modules Analyzer
- RetireJS Analyzer
```

**Reports Generated**:
- HTML (human-readable)
- JSON (machine-parseable)
- XML (CI/CD integration)
- SARIF (GitHub Code Scanning)
- CSV (data analysis)

**Suppression**: Configured via `dependency-check-suppression.xml` (optional)

---

### SonarQube (SAST)

**Analyzers**:
- Java: PMD, FindBugs, Checkstyle rules
- Go: Go Vet, GoLint rules
- JavaScript/TypeScript: ESLint rules
- HTML/CSS: W3C validators

**Quality Profiles**: "Sonar way" (default)

**Known Issue**: SonarQube 25.12 has a bug with JavaScript analysis on Node.js 20.10. See [WORKAROUNDS.md](./WORKAROUNDS.md).

---

## 📊 Compliance & Governance

### Automated Evidence Generation

Every pipeline run generates compliance evidence mapped to:

- **ISO 27001**: A.12.6.1, A.14.2.8
- **PCI-DSS**: Requirement 6.3.2, 11.3

**Evidence Includes**:
```yaml
Project Information:
  - Project key
  - Quality gate status
  - Last analysis date

Code Metrics:
  - Lines of code
  - Code coverage %
  - Duplicated lines %

Quality:
  - Bugs count
  - Vulnerabilities count
  - Code smells count
  - Technical debt

Security:
  - Security hotspots
  - Security rating
```

**Access**: Jenkins → Build Artifacts → `compliance_evidence.md`

---

## 📈 Metrics & Monitoring

### Pipeline Performance

| Metric | Value | Target |
|--------|-------|--------|
| Average Pipeline Duration | ~6-8 min | < 10 min |
| Parallel Service Builds | 4 concurrent | - |
| SCA Scan Time (per service) | 12-171s | < 3 min |
| SonarQube Analysis Time | 11-16s | < 30s |
| Build Success Rate | 100% | > 95% |

### Service-Level Metrics

```
┌─────────────────────┬─────────┬──────────┬─────────┐
│ Service             │ Build   │ Coverage │ Quality │
├─────────────────────┼─────────┼──────────┼─────────┤
│ payment-gateway     │ ✅ PASS │ 0%       │ PASSED  │
│ ledger-api          │ ✅ PASS │ 90%+     │ PASSED  │
│ identity-service    │ ✅ PASS │ 0%       │ PASSED  │
│ customer-web        │ ✅ PASS │ 0%       │ PASSED  │
└─────────────────────┴─────────┴──────────┴─────────┘
```

**View Live**: SonarQube Dashboard → Projects

---

## ⚠️ Known Issues & Workarounds

### SonarQube 25.12 JavaScript Analyzer Bug

**Issue**: `TypeError: Cannot read properties of undefined (reading 'replaceAll')`

**Affected Services**: `identity-service`, `customer-web`

**Root Cause**: Incompatibility between SonarQube 25.12 JS plugin and Node.js 20.10

**Workaround Applied**:
```properties
# sonar-project.properties
sonar.exclusions=**/*.js,**/*.jsx,node_modules/**,coverage/**,build/**,dist/**,.npm/**,odc-reports/**
```

This excludes JavaScript files from analysis while still scanning JSON, HTML, and project structure.

**Impact**: JS code quality is not analyzed, but security scans (secrets, SCA) still run.

**Permanent Fix**: Upgrade to SonarQube 26+ or Node.js 24 LTS (when available)

See [WORKAROUNDS.md](./WORKAROUNDS.md) for detailed technical explanation.

---

## 📁 Project Structure

```
fintech-devsecops-showcase/
├── ansible/                          # Infrastructure as Code
│   ├── playbooks/                    # Ansible Playbooks
│   │   ├── setup-stack.yml           # Main infrastructure deployment
│   │   └── reverse-proxy.yml        # Nginx/SSL reverse proxy setup
│   ├── roles/                        # Ansible Roles
│   │   ├── nginx-reverse-proxy/      # Nginx configuration role
│   │   └── ...                       # Other infrastructure roles
│   ├── Jenkins.Dockerfile            # Custom Jenkins image
│   ├── Scanner.Dockerfile            # Security scanner image
│   ├── docker-compose.stack.yml      # Docker stack definition
│   └── inventory.ini                 # Ansible inventory
├── jenkins-shared-library/           # Reusable pipeline logic
│   ├── vars/
│   │   └── fintechPipeline.groovy   # Shared pipeline
│   └── resources/
│       └── export_compliance.sh      # Compliance export script
├── services/
│   ├── payment-gateway/              # Java/Spring Boot service
│   │   ├── pom.xml
│   │   ├── Jenkinsfile
│   │   ├── Makefile
│   │   └── sonar-project.properties
│   ├── ledger-api/                   # Go/Echo service
│   │   ├── go.mod
│   │   ├── Jenkinsfile
│   │   ├── Makefile
│   │   └── sonar-project.properties
│   ├── identity-service/             # Node.js/Express service
│   │   ├── package.json
│   │   ├── Jenkinsfile
│   │   ├── Makefile
│   │   ├── jsconfig.json
│   │   └── sonar-project.properties
│   └── customer-web/                 # React SPA
│       ├── package.json
│       ├── Jenkinsfile
│       ├── Makefile
│       └── sonar-project.properties
├── Jenkinsfile                       # Orchestrator pipeline
├── README.md                         # This file
├── ARCHITECTURE.md                   # Detailed architecture
├── SECURITY.md                       # Security controls
├── WORKAROUNDS.md                    # Known issues
└── LICENSE                           # MIT License
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork** the repository
2. Create a **feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. Open a **Pull Request**

### Development Guidelines

- Follow language-specific best practices
- Add tests for new features
- Update documentation
- Ensure all pipelines pass

---

## 📜 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Jenkins** - Automation server
- **SonarQube** - Code quality platform
- **OWASP** - Security tools & guidance
- **GitLeaks** - Secrets detection
- **Docker** - Containerization

---

## 📞 Contact

**Elvis Zonepoh Mbaapoh**
- GitHub: [@Mbaapoh](https://github.com/Mbaapoh)
- LinkedIn: [Elvis Zonepoh Mbaapoh](https://linkedin.com/in/your-profile)

---

## 🌟 Star History

If you find this project useful, please consider giving it a ⭐!

[![Star History Chart](https://api.star-history.com/svg?repos=Mbaapoh/fintech-devsecops-showcase&type=Date)](https://star-history.com/#Mbaapoh/fintech-devsecops-showcase&Date)

---

**Built with ❤️ for DevSecOps excellence**
