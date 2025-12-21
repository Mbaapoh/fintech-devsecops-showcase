# Fintech DevSecOps Showcase

## Overview
A production-ready infrastructure and boilerplate code for a "Fintech Security Showcase". This project demonstrates a secure, polyglot microservices architecture managed with strict DevSecOps practices governed by ISO 27001 and PCI-DSS principles.

## Collaborative Governance Model
This project enforces a **Collaborative Governance** model where:
- **QA & Security Teams** define the Quality Gates and Compliance Standards.
- **DevOps Engineers** implement these standards as automated gates in the pipeline (`waitForQualityGate`).
- **Developers** receive immediate feedback via the pipeline, ensuring security starts at the source.

This separation of concerns prevents conflict, as rules are codified and transparent.

## Architecture
- **Infrastructure**: Immutable infrastructure managed via Ansible.
- **Orchestration**: Docker Compose (for demo simplicity) running SonarQube (Community) and Jenkins.
- **Services**:
  - `payment-gateway`: Java/Spring Boot (Financial Transactions)
  - `ledger-api`: Golang (Fast, highly concurrent ledger recording)
  - `identity-service`: Node.js (fast I/O for auth checks)
  - `customer-web`: React (Client facing portal)

## Repository Structure
```
.
├── ansible/                  # Infrastructure as Code
│   ├── site.yml              # Main playbook
│   ├── inventory.ini         # Host inventory
│   └── docker-compose.stack.yml # SonarQube + Jenkins stack
├── services/                 # Polyglot Microservices
│   ├── payment-gateway/      # Java
│   ├── ledger-api/           # Golang
│   ├── identity-service/     # Node.js
│   └── customer-web/         # React
├── Jenkinsfile               # Security-First Pipeline definition
└── sonar-project.properties  # SonarQube monorepo configuration
```

## Getting Started

### 1. Provision Infrastructure
1. Update `ansible/inventory.ini` with your target server IP (e.g., Hetzner host).
2. Run the Ansible playbook:
   ```bash
   cd ansible
   ansible-playbook -i inventory.ini site.yml
   ```
   *Note: `vim` is the preferred editor for configuration tasks on the server.*

### 2. Configure Pipeline
1. Access Jenkins at `http://<YOUR_IP>:8080`.
2. Access SonarQube at `http://<YOUR_IP>:9000` (Default: admin/admin).
3. Create a Jenkins job pointing to this repository.
4. Configure the SonarQube webhook in Jenkins.

### 3. Pipeline Stages
The `Jenkinsfile` implements a 4-stage security gate:
1. **Secrets Detection**: Scans for leaked credentials (Gitleaks).
2. **SCA**: Scans dependencies for known CVEs.
3. **SAST**: Static analysis with SonarQube (Enforced Quality Gate).
4. **Build & Push**: Creates production artifacts only if all gates pass.

## Technologies
- **CI/CD**: Jenkins, Gitleaks, SonarQube
- **Infra**: Ansible, Docker
- **App**: Java, Go, Node.js, React
