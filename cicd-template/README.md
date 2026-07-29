# springboot-cicd-template

> **Enterprise GitHub Actions Reusable Workflow Library** for Spring Boot REST APIs on AWS EC2.

## Overview

This repository is a **GitHub Actions reusable workflow template** that provides a complete, 12-stage enterprise CI/CD pipeline. Application repositories call this workflow via [`workflow_call`](https://docs.github.com/en/actions/using-workflows/reusing-workflows) and inherit the full pipeline without duplicating CI/CD logic.

## Pipeline Stages

| Stage | Kind | What it does |
|-------|------|-------------|
| 1 | **Code Quality** | Checkstyle (Google style, 120 char limit) + SpotBugs static analysis |
| 2 | **Unit Tests** | JUnit 5 + Mockito via Maven Surefire; JaCoCo coverage report |
| 3 | **Integration Tests** | `@SpringBootTest` via Maven Failsafe |
| 4 | **Security Scan** | OWASP Dependency Check; fails on CVSS ≥ 9 (configurable) |
| 5 | **Build JAR** | Maven package `-DskipTests`; uploads artifact |
| 6 | **Docker Build & Push** | Multi-stage Dockerfile → GHCR (tagged with commit SHA + `latest`) |
| 7 | **Terraform Provision** | EC2 t3.small + EIP + SG via Terraform on AWS |
| 8 | **Ansible Configure** | Docker install, GHCR pull, nginx reverse proxy, systemd |
| 9 | **Deployment Verification** | Health check with 15 retries / 20s delay |
| 10 | **Smoke Tests** | curl-based endpoint assertions (health, info, items API) |
| 11 | **Performance Tests** | k6 load test: 50 VUs, p95 < 500ms threshold |
| 12 | **Notify + Rollback** | Job summary; automatic rollback to previous SHA on failure |

## Usage

In your application repository, create `.github/workflows/deploy.yml`:

```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  pipeline:
    uses: YOUR_ORG/springboot-cicd-template/.github/workflows/enterprise-pipeline.yml@main
    with:
      image_name: "YOUR_ORG/your-app-name"
      java_version: "17"
      aws_region: "us-east-1"
      run_perf_tests: true
    secrets:
      AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
      AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
      TF_STATE_BUCKET: ${{ secrets.TF_STATE_BUCKET }}
      PROJECT_NAME: ${{ secrets.PROJECT_NAME }}
      SSH_PRIVATE_KEY: ${{ secrets.SSH_PRIVATE_KEY }}
      SSH_PUBLIC_KEY: ${{ secrets.SSH_PUBLIC_KEY }}
      SSH_USER: ${{ secrets.SSH_USER }}
```

## Inputs Reference

| Input | Type | Default | Description |
|-------|------|---------|-------------|
| `java_version` | string | `"17"` | Java version for build |
| `maven_args` | string | `"--no-transfer-progress"` | Extra Maven flags |
| `image_name` | string | **required** | Docker image path (e.g. `my-org/my-app`) |
| `aws_region` | string | `"us-east-1"` | AWS deployment region |
| `instance_type` | string | `"t3.small"` | EC2 instance type |
| `health_endpoint` | string | `"/actuator/health"` | Health check path |
| `run_perf_tests` | boolean | `true` | Enable/disable k6 tests |
| `owasp_cvss_threshold` | string | `"9"` | CVSS score to fail the build |

## Required Secrets

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | AWS IAM access key |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM secret key |
| `TF_STATE_BUCKET` | S3 bucket for Terraform state |
| `PROJECT_NAME` | Unique project identifier |
| `SSH_PRIVATE_KEY` | EC2 SSH private key (PEM) |
| `SSH_PUBLIC_KEY` | EC2 SSH public key |
| `SSH_USER` | SSH username (e.g. `ubuntu`) |

## Repository Requirements

The consuming application repository must contain:

```
infra/              # Terraform files (main.tf, variables.tf, outputs.tf, versions.tf)
ansible/            # Ansible playbook (site.yml + roles/)
  roles/
    common/
    app/
    nginx_proxy/
tests/
  performance/
    load-test.js    # k6 script
Dockerfile          # Multi-stage build
checkstyle.xml      # Checkstyle rules
pom.xml             # Maven build with checkstyle + spotbugs + OWASP plugins
```

## Rollback Behavior

If any stage after `terraform-provision` fails, the `rollback` job automatically triggers and re-deploys the Docker image from the **previous commit SHA** (`HEAD~1`). This ensures the live environment is never left in a broken state.

## License

MIT
