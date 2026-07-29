# springboot-api — Build Notes

## Project Summary
- **Goal**: Enterprise Spring Boot REST API on EC2 with dual-repo CI/CD architecture
- **Repos**: (1) `springboot-api` (app, IaC, Ansible) + (2) `springboot-cicd-template` (reusable GHA workflow)
- **Cloud**: AWS us-east-1, EC2 t3.small Ubuntu 22.04, Elastic IP, nginx reverse proxy
- **Pipeline**: 12-stage enterprise pipeline via GitHub reusable workflow (workflow_call)

## Architecture Decisions
- **Dual-repo design**: App repo calls the template repo's enterprise-pipeline.yml via `workflow_call` inputs/secrets. Template repo is standalone and reusable across any Spring Boot project.
- **In-memory repository**: No RDS at Tier 1; ConcurrentHashMap for thread-safe access. Can add RDS PostgreSQL later.
- **Docker + nginx**: App runs as Docker container on port 8080; nginx proxies port 80 → 8080. Non-root user inside container.
- **Reusable workflow**: Template defines `workflow_call` with inputs (java_version, image_name, aws_region, instance_type, health_endpoint, run_perf_tests) and secrets. App repo calls it via `.github/workflows/deploy-app.yml` → rendered via `pipelines:` spec entry.
- **Terraform backend**: Empty `backend "s3" {}` — bucket/key/region injected via -backend-config at init per platform contract.
- **EIP over ephemeral IP**: Output `instance_ip` = EIP address, not the instance's ephemeral IP. Verify stage re-reads from terraform output (self-sufficient job rule).
- **Spring Boot 3.2.5**: Downgraded from scaffold's 4.1.0 (non-existent). 3.2.5 is stable on Java 17.
- **OWASP NVD**: First run downloads the vulnerability database (~600MB, 5-10 min). Subsequent runs use cache.
- **k6 perf test**: 50 VUs, 180s, p95 < 500ms threshold. Ramp: 10VU→50VU→hold→0.

## Gotchas / Known Issues
- **Spring Boot 4.1.0 in scaffold**: Invalid — corrected to 3.2.5. Dependency IDs like `spring-boot-starter-webmvc` and `spring-boot-starter-actuator-test` were wrong in the scaffold; replaced with correct ones.
- **Checkstyle WhitespaceAround**: This module can fire on certain annotation patterns. If it fails, add specific suppressions or narrow the scope.
- **OWASP first run**: Can be slow (NVD download). `failBuildOnCVSS=9` means only critical CVEs block — known Spring Boot 3.2.x dependencies are generally clean.
- **Ansible docker login `stdin`**: Uses `args: stdin:` — this passes the token via stdin to avoid echoing it in logs. Some Ansible versions may behave differently; alternative is `--password stdin` via shell pipe.
- **GHCR image visibility**: Package will be created as private by default under the org. The EC2 instance needs `docker login ghcr.io` before pull — wired via Ansible app role with `github_actor` + `github_token` vars.
- **SSH_USER**: Must be `ubuntu` for Ubuntu 22.04. Platform derives this from OS. Never hardcode.
- **IAM propagation**: `depends_on = [aws_iam_instance_profile.app]` on the EC2 instance guards against IAM propagation race on first apply.

## Phase Status
- [x] Project meta approved
- [x] Architecture written (rev 1)
- [x] Pipeline spec written (rev 1)
- [x] Design gate approved
- [x] Plan gate approved
- [x] Scaffold generated (java/spring-boot)
- [x] pom.xml corrected (Spring Boot 3.2.5, correct dependency IDs, all plugins)
- [x] Application code written (model, dto, repository, service, controller, exception handler)
- [x] Unit tests written (ItemServiceTest, ItemControllerTest, ItemRepositoryTest, ApplicationTests)
- [x] Checkstyle config written
- [x] Dockerfile fixed (multi-stage, non-root, HEALTHCHECK, JVM container flags)
- [x] Terraform written (main.tf, variables.tf, outputs.tf, versions.tf)
- [x] Ansible written (site.yml + roles: common, app, nginx_proxy)
- [x] k6 performance test written
- [x] cicd-template repo written (enterprise-pipeline.yml + README)
- [x] README.md written
- [ ] validate_project
- [ ] create_repo_and_push (springboot-api)
- [ ] cicd-template repo push (separate)
- [ ] deploy + wait_for_run
- [ ] recovery if needed

## Recovery Log
(empty — not yet deployed)
