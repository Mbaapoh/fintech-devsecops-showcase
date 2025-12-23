# Fintech DevSecOps Showcase: ISO 27001 & PCI-DSS Compliant Pipeline

## 🚀 Overview
This project serves as a **Senior DevSecOps Showcase**, demonstrating a scalable, polyglot microservices CI/CD environment. It implements strict security gates, automated compliance evidence collection, and an event-driven architecture designed for high-concurrency financial systems.

## 🛡 Security & Compliance (ISO 27001 / PCI-DSS)
Key controls implemented in this pipeline:
- **Control A.12.6.1 (ISO 27001):** Management of technical vulnerabilities (Automated SCA & SAST).
- **PCI-DSS Requirement 6.3:** Secure software development (Automated Quality Gates).
- **Automated Evidence Collection:** Every build generates a signed `compliance_evidence.md` report, archiving the security posture of the release.

---

## 🏗 Event-Driven Architecture
Unlike traditional polling systems, this pipeline uses an **Event-Driven (Push)** model:
1.  **Developer** commits code to GitHub.
2.  **GitHub Webhook** sends an instant notification to Jenkins.
3.  **Jenkins** triggers the pipeline immediately.

*Note: For local lab environments where the server is behind a NAT, SCM Polling is used as a fallback. The `Jenkinsfile` is pre-configured for Webhooks.*

---

## 📂 Project Structure
```text
├── ansible/                  # Infrastructure as Code (Immutable Setup)
├── security/                 # Compliance & Audit Tools
│   └── export_compliance.sh  # Generates PCI-DSS audit evidence
├── services/                 # Scalable Polyglot Microservices
│   ├── payment-gateway/      # Java (Financial Transactions)
│   ├── ledger-api/           # Golang (High-performance ledger)
│   ├── identity-service/     # Node.js (Identity & Access)
│   └── customer-web/         # React (Customer Frontend)
├── Jenkinsfile               # Multi-stage security pipeline
└── sonar-project.properties  # Centralized Quality Gate configuration
```

---

## 🛠 Setup Instructions

### 1. Infrastructure Provisioning
Update `ansible/inventory.ini` and run:
```bash
ansible-playbook -i ansible/inventory.ini ansible/site.yml
```

### 2. Event-Driven Trigger (Webhook) Setup
To enable production-grade triggers:
1.  Ensure Jenkins is accessible at `http://65.21.108.94:8080`.
2.  In GitHub: `Settings` -> `Webhooks` -> `Add Webhook`.
3.  Payload URL: `http://65.21.108.94:8080/github-webhook/`.
4.  Event: `Just the push event`.

### 3. Generating Audit Evidence
After a successful build, the pipeline automatically generates evidence. You can also run it manually:
```bash
./security/export_compliance.sh http://65.21.108.94:9000 fintech-devsecops-showcase <TOKEN>
```

## 📊 CI/CD Security Gates
1.  **Gitleaks Scan:** Prevents credential leakage (fails build on detection).
2.  **Dependency-Check (SCA):** Validates 3rd-party library security.
3.  **SonarQube (SAST):** Enforces strict "PCI-DSS MQR" Quality Gates (A-Rating required).
4.  **Production Packaging:** Docker images are only built from the `main` branch after passing all gates.

---
*Created by **Antigravity DevSecOps Toolkit** for high-compliance fintech environments.*
