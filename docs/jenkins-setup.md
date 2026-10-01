# Running the Jenkins Pipeline Locally

The `Jenkinsfile` at the repo root mirrors `.github/workflows/ci.yml`. This
is how to run it against a throwaway local Jenkins using Docker.

## 1. Start Jenkins in Docker

```bash
docker run -d --name jenkins \
  -p 8081:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

- Port `8081` (not `8080`) avoids colliding with the SUT itself.
- Mounting the Docker socket lets the pipeline's `docker compose` steps
  control containers on the host — this Jenkins container needs the `docker`
  CLI installed inside it (see step 2).

## 2. Install Docker CLI and Java/Node inside the Jenkins container

The official `jenkins/jenkins:lts` image doesn't ship the `docker`, `java`
(besides its own JVM), or `node` binaries the pipeline shells out to. The
simplest fix for a local trial is to exec into the running container and
install them, or build a small custom image `FROM jenkins/jenkins:lts` that
adds `docker-ce-cli`, a JDK 21, and Node 20. For anything beyond a one-off
local demo, use the official `jenkins/agent` images with those tools
pre-installed, or a Jenkins agent that already has them.

## 3. Unlock Jenkins and create the pipeline job

1. Get the initial admin password: `docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword`
2. Open `http://localhost:8081`, paste the password, install the suggested
   plugins (this includes the plugins the `junit` and `archiveArtifacts`
   steps need).
3. **New Item → Pipeline**, name it `igniters-qa-automation`.
4. Under **Pipeline → Definition**, choose **Pipeline script from SCM**,
   point it at this repository's URL and branch, with script path
   `Jenkinsfile`.

## 4. Run it

Click **Build Now**. The stages match the `Jenkinsfile`: Checkout → Build →
Start Environment → Smoke → Regression (branches/PRs only) → API → Publish
Reports → Teardown. Surefire and Newman results show up under **Test
Result Trend** on the job page; the raw reports and any failure screenshots
are attached as build artifacts.
