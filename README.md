# springboot-api

> **Enterprise Spring Boot 3.2 REST API** deployed to AWS EC2 via a 12-stage CI/CD pipeline.
> Powered by the [`springboot-cicd-template`](https://github.com/YOUR_ORG/springboot-cicd-template) reusable workflow.

## Architecture

```
Developer → GitHub Push
  └── springboot-api (this repo)
        └── calls springboot-cicd-template/enterprise-pipeline.yml
              ├── Stage 1:  Checkstyle + SpotBugs
              ├── Stage 2:  Unit Tests (JUnit 5 + JaCoCo)
              ├── Stage 3:  Integration Tests (Failsafe)
              ├── Stage 4:  OWASP Security Scan
              ├── Stage 5:  Maven JAR Build
              ├── Stage 6:  Docker → GHCR
              ├── Stage 7:  Terraform → EC2 + EIP + SG
              ├── Stage 8:  Ansible → Docker + nginx
              ├── Stage 9:  Health Verification
              ├── Stage 10: Smoke Tests
              ├── Stage 11: k6 Performance Tests
              └── Stage 12: Notify + Auto-Rollback
```

```
Internet
  └── EC2 Elastic IP (port 80)
        └── nginx (reverse proxy)
              └── Spring Boot container (port 8080)
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Landing page (UI) |
| `GET` | `/actuator/health` | Spring Boot health check |
| `GET` | `/actuator/info` | Application info |
| `GET` | `/api/v1/info` | API metadata + endpoint list |
| `GET` | `/api/v1/items` | List all items |
| `GET` | `/api/v1/items/{id}` | Get item by ID |
| `POST` | `/api/v1/items` | Create item |
| `PUT` | `/api/v1/items/{id}` | Update item |
| `DELETE` | `/api/v1/items/{id}` | Delete item |

### Example Requests

```bash
# List items
curl http://<YOUR_IP>/api/v1/items

# Create an item
curl -X POST http://<YOUR_IP>/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{"name":"New Widget","description":"A great widget","price":19.99}'

# Health check
curl http://<YOUR_IP>/actuator/health
```

## Local Development

### Prerequisites
- Java 17 (Temurin recommended)
- Maven 3.9+
- Docker (optional)

### Run locally

```bash
# Run with Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw package -DskipTests
java -jar target/*.jar

# Run with Docker
docker build -t springboot-api .
docker run -p 8080:8080 springboot-api
```

### Run tests

```bash
# Unit tests
./mvnw test

# All tests including integration
./mvnw verify

# Checkstyle
./mvnw checkstyle:check

# SpotBugs
./mvnw spotbugs:check

# OWASP dependency check
./mvnw org.owasp:dependency-check-maven:check
```

## Deployment

Deployment is fully automated via the enterprise CI/CD pipeline. Push to `main` triggers the full 12-stage pipeline.

### Pipeline Flow

```
lint → test → security → build → docker-push → terraform → ansible → verify → smoke → perf → notify
                                                                                              ↓ (on failure)
                                                                                          rollback
```

### Infrastructure

Provisioned by Terraform (`infra/`):

- **EC2** `t3.small` on Ubuntu 22.04 LTS
- **Elastic IP** — stable public IP
- **Security Group** — ports 22 (SSH), 80 (HTTP), 443 (HTTPS)
- **IAM Instance Profile** — SSM managed core

Configured by Ansible (`ansible/`):

- Docker CE installed + app container running
- nginx reverse proxy on port 80 → app on 8080
- SSH hardened (password auth disabled)
- Unattended security upgrades enabled

### Required Secrets

Set these in GitHub repository settings → Secrets and variables → Actions:

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | AWS IAM credentials |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM credentials |
| `TF_STATE_BUCKET` | S3 bucket for Terraform state (platform-managed) |
| `PROJECT_NAME` | Unique project name (platform-managed) |
| `SSH_PRIVATE_KEY` | EC2 SSH private key (platform-managed) |
| `SSH_PUBLIC_KEY` | EC2 SSH public key (platform-managed) |
| `SSH_USER` | SSH username — `ubuntu` for Ubuntu 22.04 |

## CI/CD Template Repository

The reusable enterprise pipeline lives at `springboot-cicd-template`. To use it in another Spring Boot project:

```yaml
# .github/workflows/deploy.yml (in your app repo)
jobs:
  pipeline:
    uses: YOUR_ORG/springboot-cicd-template/.github/workflows/enterprise-pipeline.yml@main
    with:
      image_name: "YOUR_ORG/your-app"
    secrets: inherit
```

## Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 (Temurin) |
| Framework | Spring Boot 3.2 |
| Build | Maven 3.9 |
| Container | Docker / GHCR |
| IaC | Terraform 1.6+ / AWS provider 5.x |
| Configuration | Ansible Core |
| Web server | nginx |
| Tests | JUnit 5, Mockito, k6 |
| Security | OWASP Dependency Check, SpotBugs |
| CI/CD | GitHub Actions (reusable workflow) |
| Cloud | AWS EC2 t3.small / us-east-1 |

## Cost

~$20–32/month: EC2 t3.small ($17) + Elastic IP ($3.60) + minimal S3 state storage.

## License

MIT
