# 🔒 Security Policy

## Overview

This document outlines the security controls, threat model, and security practices implemented in the Fintech DevSecOps Showcase project.

---

## 🛡️ Security Architecture

### Defense in Depth Layers

```
┌─────────────────────────────────────────────────────────────┐
│                   Layer 7: Monitoring                        │
│              • Audit logs • Compliance reports               │
├─────────────────────────────────────────────────────────────┤
│                Layer 6: Quality Gates                        │
│         • SonarQube enforcement • Build failure on fail      │
├─────────────────────────────────────────────────────────────┤
│             Layer 5: Static Analysis (SAST)                  │
│    • Code quality scanning • Security hotspot detection      │
├─────────────────────────────────────────────────────────────┤
│    Layer 4: Software Composition Analysis (SCA)              │
│       • Dependency vulnerability scanning • CVE checks       │
├─────────────────────────────────────────────────────────────┤
│              Layer 3: Secrets Detection                      │
│         • GitLeaks scanning • Credential prevention          │
├─────────────────────────────────────────────────────────────┤
│               Layer 2: Build Isolation                       │
│      • Docker containers • Ephemeral build environments      │
├─────────────────────────────────────────────────────────────┤
│             Layer 1: Infrastructure Security                 │
│    • Network isolation • Secure communication • Access control│
└─────────────────────────────────────────────────────────────┘
```

---

## 🔍 Security Scanning Tools

### 1. Secrets Detection (GitLeaks)

**Version**: Latest (Docker image)  
**Execution**: Every commit, every pipeline run  
**Detection Patterns**:

| Pattern Type | Examples | Severity |
|--------------|----------|----------|
| API Keys | AWS, Azure, GCP, Stripe | CRITICAL |
| Database Credentials | Connection strings, passwords | CRITICAL |
| Private Keys | RSA, SSH, PGP | CRITICAL |
| OAuth Tokens | GitHub, GitLab, Bitbucket | HIGH |
| Generic Secrets | `password=`, `secret=`, `token=` | MEDIUM |

**Configuration**:
```bash
# Command:
docker run --rm -v /path/to/repo:/path \
  zricethezav/gitleaks:latest \
  detect --source=/path \
  --report-path=/path/gitleaks-report.json \
  --no-git \
  --redact \
  -v
```

**Output**: Redacted JSON report with line numbers (passwords masked)

**Exit Behavior**:
- ✅ **No secrets found**: Pipeline continues
- ❌ **Secrets detected**: Pipeline halts immediately

---

### 2. Software Composition Analysis - OWASP Dependency-Check

**Version**: Latest (Docker image)  
**Database**: NVD (National Vulnerability Database)  
**Update Frequency**: Every 4 hours (or 31+ new CVEs)

**Analyzed Dependencies**:

| Language | Analyzer | Package Formats |
|----------|----------|-----------------|
| Java | Maven Central | `pom.xml` |
| JavaScript | Node.js / npm | `package.json`, `package-lock.json` |
| JavaScript | RetireJS | Direct file analysis |
| Go | Go Modules | `go.mod`, `go.sum` |

**CVE Severity Mapping**:

```
CRITICAL (9.0-10.0) → ❌ Pipeline Failure (MQR enforcement)
HIGH     (7.0-8.9)  → ⚠️  Warning (logged)
MEDIUM   (4.0-6.9)  → ℹ️  Info (logged)
LOW      (0.1-3.9)  → ✓  Acceptable
```

**Report Formats Generated**:
- HTML (human-readable dashboard)
- JSON (API consumption)
- XML (CI/CD integration)
- CSV (data analysis)
- SARIF (GitHub Code Scanning)
- Jenkins HTML (Jenkins UI)
- JUNIT (test result integration)

**Suppression**: Allows false positive suppression via `dependency-check-suppression.xml`

---

### 3. Static Application Security Testing - SonarQube

**Version**: 25.12.0  
**Quality Profile**: "Sonar way" (default)  
**Language Support**:

| Language | Rules Count | Coverage |
|----------|-------------|----------|
| Java | 600+ | Security, Bugs, Code Smells |
| Go | 100+ | Best practices, Performance |
| JavaScript | 400+ | Security, ES6+ rules |
| TypeScript | 400+ | Type safety, Security |
| HTML/CSS | 50+ | Accessibility, Standards |

**Security Hotspots Detected**:
- SQL Injection vectors
- XSS vulnerabilities
- CSRF weaknesses
- Insecure deserialization
- Hardcoded credentials
- Weak cryptography
- Command injection

**Quality Gate Criteria**:

```yaml
Maintainability:
  - Code Smells: < 100
  - Technical Debt Ratio: < 5%
  - Duplicated Lines: < 3%

Reliability:
  - Bugs: 0
  - Critical Issues: 0

Security:
  - Vulnerabilities: 0
  - Security Hotspots: Reviewed 100%
  - Security Rating: A

Coverage:
  - Code Coverage: > 0% (configurable per project)
```

**Enforcement**: Pipeline **halts** if quality gate fails.

---

## 🔐 Security Best Practices

### Credential Management

**DO**:
- ✅ Use Jenkins Credentials Plugin for secrets
- ✅ Store credentials in environment variables
- ✅ Rotate credentials regularly (30-90 days)
- ✅ Use least-privilege access principles

**DON'T**:
- ❌ Hardcode credentials in source code
- ❌ Commit `.env` files to Git
- ❌ Store passwords in plain text
- ❌ Share credentials across services

**Example**:
```groovy
// ✅ GOOD - Jenkins credentials
withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
    sh "sonar-scanner -Dsonar.token=${SONAR_TOKEN}"
}

// ❌ BAD - Hardcoded
sh "sonar-scanner -Dsonar.token=squ_abc123def456"
```

---

### Dependency Management

**Strategy**: Regularly update dependencies to patch vulnerabilities

**Process**:
```bash
# 1. Check for outdated dependencies
npm outdated                # Node.js
mvn versions:display-dependency-updates  # Maven
go list -u -m all           # Go

# 2. Review changelogs for breaking changes

# 3. Update dependencies
npm update                  # Node.js
mvn versions:use-latest-releases  # Maven
go get -u ./...             # Go

# 4. Run tests
npm test / mvn test / go test

# 5. Run security scan
make scan
```

**Automated Updates**: Consider Dependabot or Renovate for automated PRs.

---

### Container Security

**Base Images**:
- Use **official images** from Docker Hub
- Pin **specific versions** (not `latest`)
- Regularly **rebuild** to get security patches

**Example**:
```dockerfile
# ✅ GOOD - Pinned version
FROM node:18.19.0-alpine

# ❌ BAD - Latest tag
FROM node:latest
```

**Scanning**: Consider adding Trivy or Snyk for container scanning.

---

### Network Security

**Isolation**:
- SonarQube requires authentication
- Services communicate via internal Docker networks (`fintech-net`)
- **Traefik Reverse Proxy** handles all external traffic with automated SSL termination.
- No direct exposure of backend services to the host interfaces (ports 8081, 8082, etc. are not mapped to localhost).

**Firewall Rules** (if deploying to cloud):
```bash
# Allow only necessary ports:
- 8080 (Jenkins) - Internal only or VPN
- 9000 (SonarQube) - Internal only or VPN
- 8081-8082, 3000-3001 (Services) - Load balancer only
```

---

## 🚨 Incident Response

### Security Incident Handling

**1. Detection**:
   - Pipeline failure due to security scan
   - Alert from monitoring system
   - User report

**2. Containment**:
   - Stop affected pipeline/service
   - Revoke compromised credentials
   - Isolate affected systems

**3. Investigation**:
   - Review GitLeaks report
   - Check dependency-check findings
   - Analyze SonarQube security hotspots

**4. Remediation**:
   - Fix vulnerable code/dependency
   - Rotate credentials
   - Deploy patch

**5. Post-Incident**:
   - Document lessons learned
   - Update security controls
   - Communicate to stakeholders

---

## 📋 Compliance Mappings

### ISO 27001

| Control | Description | Implementation |
|---------|-------------|----------------|
| A.12.6.1 | Technical vulnerability management | OWASP Dependency-Check, SonarQube |
| A.14.2.8 | System security testing | Automated SAST/SCA in CI/CD |
| A.14.2.9 | System acceptance testing | Quality gate enforcement |
| A.18.2.3 | Technical compliance review | Compliance evidence generation |

### PCI-DSS

| Requirement | Description | Implementation |
|-------------|-------------|----------------|
| 6.3.2 | Review custom code | SonarQube code review |
| 6.5 | Address common coding vulnerabilities | OWASP Top 10 scanning |
| 11.3 | Implement penetration testing | SCA + SAST (DAST future) |

### OWASP Top 10 Coverage

| Risk | Mitigation |
|------|------------|
| A01:2021 - Broken Access Control | SonarQube security rules |
| A02:2021 - Cryptographic Failures | Secrets detection, code analysis |
| A03:2021 - Injection | SQL injection detection in SAST |
| A04:2021 - Insecure Design | Code review + quality gates |
| A05:2021 - Security Misconfiguration | Infrastructure as Code validation |
| A06:2021 - Vulnerable Components | **OWASP Dependency-Check** |
| A07:2021 - Authentication Failures | SAST security hotspots |
| A08:2021 - Data Integrity Failures | Code signing (future) |
| A09:2021 - Security Logging Failures | Audit trail in Jenkins |
| A10:2021 - SSRF | SAST detection rules |

---

## 🔄 Security Update Process

### Monthly Security Review

**Schedule**: First week of every month

**Checklist**:
- [ ] Update NVD database (automatic)
- [ ] Review SonarQube security hotspots
- [ ] Check for new CVEs affecting dependencies
- [ ] Update Docker base images
- [ ] Rotate Jenkins credentials
- [ ] Review access logs
- [ ] Update security documentation

---

## 📊 Security Metrics

### Key Performance Indicators (KPIs)

| Metric | Target | Current |
|--------|--------|---------|
| Secrets Found | 0 | ✅ 0 |
| Critical CVEs in Production | 0 | ✅ 0 |
| Quality Gate Pass Rate | 100% | ✅ 100% |
| Mean Time to Fix (MTTF) | < 24h | - |
| Security Scan Coverage | 100% | ✅ 100% |

**Dashboards**: Available in SonarQube and Jenkins

---

## 🐛 Reporting Security Issues

If you discover a security vulnerability, please:

1. **DO NOT** open a public GitHub issue
2. Email: security@example.com (or your contact)
3. Include:
   - Description of vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

**Response Time**: We aim to respond within 24 hours.

---

## 📚 Security Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CWE/SANS Top 25](https://cwe.mitre.org/top25/)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
- [Jenkins Security Hardening](https://www.jenkins.io/doc/book/security/)
- [SonarQube Security](https://docs.sonarqube.org/latest/user-guide/security/)

---

**Last Updated**: 2025-12-23  
**Next Review**: 2026-01-23

---

**Security is everyone's responsibility. Stay vigilant!** 🛡️
