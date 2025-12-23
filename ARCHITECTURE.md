# Fintech DevSecOps Showcase: Architecture & Governance

This document outlines the high-scale, production-grade engineering patterns implemented in this CI/CD toolchain.

## 🏛 1. Distributed Hybrid Architecture
The pipeline is designed for a **Jenkins Controller-Agent** architecture but is "Hybrid-Robust"—it detects when it's running in a local simulation (Docker-in-Docker) and automatically adjusts its mounting strategy.

### The "Hybrid Bridge" Pattern
Standard Docker volume mounts (`-v`) fail when Jenkins itself is containerized. We implemented a dynamic detection logic in our `Makefiles` and `Shared Library`:
- **Bare Metal Slave:** Uses standard bind mounts.
- **Containerized Agent:** Automatically detects the environment and uses `--volumes-from $(hostname)` to bridge the host's filesystem directly to the build containers.

## 🛡 2. Governance-as-a-Service (GaaS)
Security is not a suggestion; it is a mandatory interface. All 4 microservices (Java, Go, Node.js) utilize a **Centralized Shared Library** (`jenkins-shared-library/`).

- **Atomic Jenkinsfiles:** Developers only define metadata (Project Name, Service Dir). The security logic is managed centrally by the DevOps/Security team.
- **Gitleaks & SCA Enforcement:** Every build undergoes mandatory secrets detection and Software Composition Analysis (SCA) before a single line of code is compiled.

## 🚀 3. Multi-Architecture Hardware Support
To support high-performance modern infrastructure (Apple Silicon Mac M1/M2/M3 or ARM Cloud Instances), we built a **Native Multi-Arch Toolchain**:
- **Custom Native Scanner:** Instead of relying on fragile third-party images, we build a `fintech-scanner:latest` natively using our `Scanner.Dockerfile`. 
- **Platform-Aware Execution:** The pipeline detects `x86_64` vs `arm64` and pulls the appropriate binary, eliminating "exec format errors."

## 📜 4. Automated Compliance (ISO 27001 / PCI-DSS)
We have shifted "Audit Pressure" to the left. 
- **Evidence Archiving:** Every successful build generates a `compliance_evidence.md` artifact.
- **Quality Gate Federation:** A build failure in SonarQube instantly halts the pipeline, preventing non-compliant code from reaching the registry.

---
*Created by Antigravity DevSecOps Toolkit*
