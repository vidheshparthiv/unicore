# UnirCore --- Deployment, Hosting, Versioning and Release Engineering Plan

**Document purpose:** Define how UnirCore is built, tested, versioned,
published, maintained, and consumed as a reusable Java library.

**Status:** Recommended initial production plan\
**Primary distribution:** Maven Central\
**Source and automation:** GitHub + GitHub Actions\
**Build system:** Maven\
**Initial release model:** One main artifact, tag-driven releases,
manual approval during early releases

------------------------------------------------------------------------

## 1. Executive Decision

UnirCore is a reusable Java library, not a hosted web application. Its
consumers add it to their Maven or Gradle projects as a dependency.
Therefore, its primary deployment target is **Maven Central**, not an
application hosting service.

Recommended release pipeline:

``` text
Developer
   |
   v
GitHub repository
   |
   +--> Pull request / push
   |       |
   |       v
   |   GitHub Actions: compile, test, verify, dependency checks
   |
   +--> Version tag (for example, v1.0.0)
           |
           v
       Release validation
           |
           v
       Build JAR + sources + JavaDoc + POM + signatures/checksums
           |
           v
       Sonatype Central Publisher Portal
           |
           v
       Maven Central
           |
           v
       Consumer adds UnirCore dependency to pom.xml
```

Use GitHub Actions first. Docker, Jenkins, Kubernetes, Render, and
Vercel are not required to publish a Java library. Add them only if a
concrete later requirement justifies them.

## 2. Hosting and Distribution

  ------------------------------------------------------------------------
  Service or tool         Role in UnirCore         Decision
  ----------------------- ------------------------ -----------------------
  GitHub                  Source code, issues,     Required
                          pull requests, release   
                          notes, tags              

  GitHub Actions          Automated build, tests,  Required
                          release checks and       
                          publishing workflow      

  Maven                   Build system, dependency Required
                          management, packaging    

  Sonatype Central        Validates and publishes  Required for Central
  Publisher Portal        release bundles to Maven publishing
                          Central                  

  Maven Central           Public repository        Primary distribution
                          consumers use to resolve 
                          released artifacts       

  GitHub Pages            Optional API             Optional
                          documentation or project 
                          website                  

  GitHub Packages         Alternative package      Optional; not the
                          registry, useful for     primary public channel
                          GitHub-centric/private   
                          distribution             

  JitPack                 Builds artifacts from    Useful for experiments,
                          Git repositories on      not the recommended
                          demand                   stable release channel

  Docker                  Packages runnable        Not required for the
                          applications and         library itself
                          reproducible service     
                          environments             

  Jenkins                 Self-managed automation  Not required initially
                          server                   

  Kubernetes              Orchestrates             Not required for a
                          containerized services   library

  Render / Vercel         Hosts runnable           Not relevant to
                          backend/frontend         publishing the library
                          applications             itself
  ------------------------------------------------------------------------

**Important distinction:** GitHub hosts the source repository; GitHub
Actions runs automation; Maven Central distributes versioned Java
artifacts. These are different responsibilities.

## 3. Build and Java Compatibility

### Recommended baseline

-   **Language/runtime baseline:** Java 17, provided the implementation
    and intended consumers can support it.
-   **Build tool:** Maven with a pinned, supported Maven Wrapper.
-   **Compilation:** Set `maven.compiler.release` to `17` so the
    produced bytecode and available Java APIs match the declared
    baseline.
-   **Testing:** Run unit and integration tests on the minimum supported
    JDK and selected newer LTS JDKs, such as Java 21 and Java 25, after
    validating toolchain support.
-   **Operating systems:** Run Linux tests on every pull request; run
    Windows, Linux, and macOS compatibility tests on release candidates
    or a scheduled workflow.

Example Maven compiler configuration:

``` xml
<properties>
    <maven.compiler.release>17</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

The Java baseline is a product decision: raising it later can prevent
consumers on older Java versions from using newer UnirCore releases.
Document the minimum Java version clearly.

### Platform independence

Java bytecode is generally portable across operating systems with a
compatible JVM, but portability is not automatic. UnirCore should avoid
assuming:

-   Windows-only or Unix-only file paths and shell commands.
-   Case-insensitive file systems.
-   Specific file permissions, locking behaviour, or directory layouts.
-   A default character encoding, locale, timezone, or line-ending
    convention.
-   Identical thread scheduling, timing, or resource limits on all
    machines.
-   A particular CPU architecture or installed native library.

Use Java APIs such as `Path` and `Files`, explicitly define text
encodings where relevant, close resources reliably, and avoid native
dependencies unless isolated behind an optional integration.
Cross-platform tests are required for file-system, storage, locking, and
concurrency-related components.

**Scope clarification:** UnirCore can provide application-level
implementations and models of scheduling, memory pools, resource
allocation, paging, caching, and similar concepts. It does not replace
the operating system kernel's actual CPU scheduler, physical memory
manager, or hardware disk scheduler.

## 4. Maven Artifact and Dependency Design

### Start with one artifact

Initially publish a single artifact, for example:

-   `artifactId`: `unircore`
-   `version`: `1.0.0`
-   `groupId`: choose a namespace that is verified and controlled by the
    project owner.

An illustrative coordinate is `io.github.premnarayn13:unircore:1.0.0`;
**do not treat it as final until the namespace and publishing identity
are verified in Sonatype Central Publisher Portal**.

Consumer example:

``` xml
<dependency>
    <groupId>io.github.premnarayn13</groupId>
    <artifactId>unircore</artifactId>
    <version>1.0.0</version>
</dependency>
```

The values above are examples and must be replaced if the verified
coordinates differ. A Gradle consumer can declare the same group,
artifact, and version in its dependency configuration.

### Dependency rules

1.  Keep the core runtime dependency list small. Every runtime
    dependency becomes part of the consumer's dependency graph.
2.  Keep JUnit, Mockito, JMH, test fixtures, and build plugins out of
    consumer runtime dependencies.
3.  Add Spring Boot, logging integrations, or other framework-specific
    support as optional integrations rather than forcing those
    dependencies on every consumer.
4.  Use Maven scopes correctly: `test` for test-only dependencies,
    `provided` only when the runtime is expected to supply a dependency,
    and optional dependencies where consumers should not automatically
    inherit them.
5.  Avoid shading or bundling third-party classes into the main JAR by
    default; it can cause duplicate classes, licensing issues, and
    difficult debugging.
6.  Inspect dependency resolution with `mvn dependency:tree`; test the
    published artifact in a separate sample consumer project.
7.  Scan dependencies for known vulnerabilities and update affected
    versions deliberately.
8.  Do not publish stable releases with `SNAPSHOT` dependencies.

Maven resolves transitive dependencies and applies dependency mediation
rules. If another dependency requests a different version of the same
library, Maven may select a version that is not compatible with every
consumer. Keep dependencies minimal, declare compatible version ranges
only when justified, and test realistic dependency combinations.

### When to split into modules

Do not create many Maven artifacts merely because the code has many
packages. Keep one artifact until consumers need independent
installation, dependency isolation, or release cadence. Later, if
justified, consider:

-   `unircore-core`
-   `unircore-scheduling`
-   `unircore-cache`
-   `unircore-resilience`
-   `unircore-spring-boot-starter`

A multi-module structure adds dependency boundaries and release
coordination. Introduce it only when there is a real benefit. A Bill of
Materials (BOM) is also unnecessary while there is only one published
artifact; it becomes useful when consumers need a coordinated set of
versions across several artifacts.

## 5. Versioning Strategy

Use **Semantic Versioning (SemVer)** with the format
`MAJOR.MINOR.PATCH`.

  -----------------------------------------------------------------------
  Version change          Meaning                 Example
  ----------------------- ----------------------- -----------------------
  MAJOR                   Incompatible public API `1.4.2` → `2.0.0`
                          or behavioural change   

  MINOR                   Backward-compatible     `1.4.2` → `1.5.0`
                          functionality added     

  PATCH                   Backward-compatible bug `1.4.2` → `1.4.3`
                          fix                     
  -----------------------------------------------------------------------

### What counts as a public API?

Treat documented public classes, interfaces, methods, constructors,
enums, configuration options, and extension points as API. Changes to
SPI contracts, result semantics, exception behaviour, or algorithm
defaults may also be breaking even when method signatures stay the same.

### Release rules

-   Use `0.y.z` while the public API is still unstable; consumers should
    not assume full compatibility before `1.0.0`.
-   Use `1.0.0` only when the core public API is sufficiently reviewed,
    documented, and tested.
-   Use stable versions such as `1.0.0`, `1.0.1`, and `1.1.0` for
    published releases.
-   Use `-SNAPSHOT` for local or development iterations, not for stable
    release tags.
-   Do not overwrite or reuse a published release version. Publish a
    corrected patch version instead.
-   Use Git tags such as `v1.0.0`. The Maven project version should be
    `1.0.0`, without the `v` prefix.
-   Keep the release tag, POM version, release notes, and artifact
    version aligned.
-   Maintain a `CHANGELOG.md` or clear GitHub Release notes with added,
    changed, fixed, deprecated, and removed behaviour.

### Deprecation and compatibility

Mark APIs for deprecation before removal where practical. Explain the
replacement and target removal window. Avoid removing or changing public
methods in patch releases. For major releases, document migration steps
and compatibility changes. Consider automated API compatibility checks
once the public API stabilizes.

## 6. Repository and Release Files

Keep standard Maven source layout; package directories belong inside the
source roots, not at the repository root.

``` text
unircore/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       ├── release-check.yml
│       └── publish-central.yml
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
├── benchmarks/                 # Optional, e.g. JMH
├── examples/
├── docs/
├── .gitignore
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── CHANGELOG.md
├── LICENSE
├── CONTRIBUTING.md
└── SECURITY.md
```

Use the agreed UnirCore package hierarchy beneath `src/main/java`. Keep
generated files such as `target/`, local IDE settings, credentials,
signing keys, and `.env` files out of Git.

The POM should include accurate project coordinates, name, description,
URL, license, SCM/repository information, developer/maintainer
information as required, Java version, and appropriate build plugins.
Verify current Sonatype Central requirements before the first release
because publishing rules can evolve.

## 7. CI/CD Workflows

Use three logical workflows. They can be implemented as separate files
or combined if the configuration remains clear.

### A. Continuous Integration --- `ci.yml`

Trigger on pull requests and pushes to the main development branch.

1.  Check out the source.
2.  Set up the required JDK and Maven cache.
3.  Run `./mvnw -B clean verify` (use `mvnw.cmd` on Windows).
4.  Run unit and integration tests.
5.  Run formatting, static analysis, and dependency/security checks
    where configured.
6.  Upload test reports and fail the check if required steps fail.

Every pull request should pass CI before merge. Do not publish artifacts
from ordinary pull requests.

### B. Release Validation --- `release-check.yml`

Trigger on a release candidate or version tag.

-   Confirm the tag matches the project version.
-   Confirm the working tree and release commit are correct.
-   Run the full test suite on the supported JDK matrix.
-   Verify the POM, license, documentation, and release notes.
-   Check for accidental `SNAPSHOT` dependencies.
-   Build and inspect the JAR, sources JAR, and JavaDoc JAR.
-   Verify API and dependency compatibility checks where configured.

### C. Publish --- `publish-central.yml`

Trigger only from an approved release tag or controlled release event.

-   Re-run release validation.
-   Build the required publication bundle.
-   Sign artifacts and produce required checksums/metadata according to
    the current Central publishing requirements.
-   Authenticate to Sonatype using protected repository secrets.
-   Upload to the Sonatype Central Publisher Portal.
-   Begin with a manual review/approval step; automate full publication
    only after the process is reliable.
-   Record the resulting publication status and link in the GitHub
    Release.

Use GitHub Environments and restricted permissions for release jobs
where possible. Do not expose publishing credentials to forked pull
requests or untrusted workflow code.

## 8. Secrets and Supply-Chain Security

Store publishing credentials in GitHub Actions secrets or a suitably
restricted environment, never in source code, the POM, documentation, or
committed workflow literals. Use separate credentials for separate
purposes where supported, restrict who can trigger a release, and rotate
credentials if exposed.

Recommended protections:

-   Protect the default branch and require passing CI checks.
-   Require review for release workflow changes.
-   Restrict release creation/tagging to trusted maintainers.
-   Use least-privilege workflow permissions.
-   Pin or review third-party GitHub Actions and update them
    deliberately.
-   Scan dependencies and review security advisories.
-   Publish a `SECURITY.md` describing private vulnerability reporting.
-   Keep license and third-party notices accurate.
-   Consider provenance or Software Bill of Materials (SBOM) generation
    as the project matures, especially if required by users or release
    policy.

Signing keys and passwords require special care. Follow the current
Sonatype signing/authentication method and store secrets using the
supported secure mechanism; do not assume that a particular signing
workflow will remain unchanged.

## 9. Testing and Quality Gates

A successful compile is not enough for a systems-oriented library.
Before a release, verify:

-   **Unit tests:** expected outputs, edge cases, invalid inputs, and
    error handling.
-   **Algorithm correctness:** invariants and reference cases for each
    implemented algorithm.
-   **Concurrency:** race conditions, safe publication, shutdown
    behaviour, and stress tests where relevant.
-   **Resource behaviour:** bounded queues/caches/pools, cleanup,
    cancellation, and timeout behaviour.
-   **Cross-platform behaviour:** Windows, Linux, and macOS tests for
    OS-sensitive features.
-   **Compatibility:** supported JDKs, public API changes, and consumer
    dependency interactions.
-   **Packaging:** the JAR contains the intended classes/resources and
    excludes tests and build output.
-   **Consumer verification:** install or resolve the published
    candidate in a separate small Maven project.
-   **Performance:** use JMH for reliable microbenchmarks; do not use ad
    hoc wall-clock loops as the sole basis for performance claims.

Keep deterministic correctness tests in every CI run. Expensive stress
tests and broad platform matrices can run on release candidates or a
schedule if they make pull-request CI too slow.

## 10. Release Procedure

Use this checklist for each release.

1.  Complete the feature/fix and merge it through reviewed pull
    requests.
2.  Confirm CI is green and the working tree is clean.
3.  Review public API changes, tests, dependency updates, documentation,
    and `CHANGELOG.md`.
4.  Update the Maven project version to the intended release version.
5.  Run a clean verification build, for example
    `./mvnw -B clean verify`.
6.  Create and push the matching tag, for example `v1.0.0`.
7.  Run release validation against that exact commit.
8.  Generate the binary, sources, JavaDoc, POM, signatures, checksums,
    and other metadata required by the current publisher.
9.  Submit the bundle through Sonatype Central Publisher Portal.
10. Review validation errors and correct the release bundle or
    configuration before publication.
11. Confirm the artifact is available from Maven Central.
12. Create the GitHub Release with notes and links.
13. Test the dependency from a clean consumer project.
14. Start the next development iteration on a new `-SNAPSHOT` version.

Example local commands:

``` bash
./mvnw -B clean verify
./mvnw -B test
./mvnw -B package
```

On Windows PowerShell, use `.\mvnw.cmd` instead of `./mvnw`.

The exact Central publishing command/plugin configuration should follow
the current Sonatype documentation and the chosen Maven publishing
plugin. Do not copy old portal instructions blindly; the publishing
service and requirements can change.

## 11. Failed Releases and Recovery

  --------------------------------------------------------------------------
  Situation                           Response
  ----------------------------------- --------------------------------------
  CI fails before publication         Fix the code/configuration, rerun
                                      tests, and recreate or update the
                                      unreleased candidate.

  Publisher rejects the bundle        Read the validation errors, correct
                                      the
                                      POM/artifacts/signing/configuration,
                                      and resubmit according to portal
                                      rules.

  A release is published with a bug   Do not replace the existing version;
                                      publish a compatible patch such as
                                      `1.0.1`.

  A public API change breaks          Plan a major release and provide
  consumers                           migration notes.

  A credential is exposed             Revoke/rotate it, investigate workflow
                                      logs and repository history, and
                                      publish a security response if needed.

  A release tag points to the wrong   Stop publication if possible; follow
  commit                              repository and publisher rules, then
                                      create a corrected release
                                      version/tag. Do not silently change a
                                      published artifact.
  --------------------------------------------------------------------------

Published versions should be treated as immutable. Reproducible builds,
release tags, and recorded checksums help determine exactly what was
released.

## 12. Docker, Jenkins, Kubernetes and Hosting Decisions

-   **Docker:** A Java library has no server process to host, so a
    Docker image is not needed to distribute it. Docker may be useful
    for integration-test dependencies or a separate demo/service that
    uses UnirCore.
-   **Jenkins:** A capable CI server, but it adds maintenance of
    controllers, agents, plugins, credentials, and upgrades. GitHub
    Actions is the simpler starting point for a GitHub-hosted project.
-   **Kubernetes:** Useful for deploying and operating containerized
    distributed applications, not for publishing a JAR. Do not introduce
    it into the library release pipeline.
-   **Render/Vercel:** Suitable for deployable web applications, not as
    the primary distribution mechanism for a Java library.
-   **GitHub Pages:** Optional for static documentation; Maven Central
    remains the dependency repository.

Revisit these choices only when there is a specific operational
requirement, such as a hosted UnirCore service, a self-hosted CI
requirement, or a separate application demonstrating the library.

## 13. Initial Technology Decisions

  -----------------------------------------------------------------------
  Area                                Decision
  ----------------------------------- -----------------------------------
  Source control                      Git + GitHub

  Language baseline                   Java 17, subject to compatibility
                                      validation

  Build and packaging                 Maven + Maven Wrapper

  Unit/integration tests              JUnit; add other test tools only as
                                      needed

  Performance testing                 JMH for benchmarks

  CI/CD                               GitHub Actions

  Public artifact distribution        Maven Central via Sonatype Central
                                      Publisher Portal

  Versioning                          Semantic Versioning

  Release trigger                     Protected, matching Git tag

  Artifact structure                  One primary artifact initially

  Cross-platform CI                   Linux on pull requests;
                                      Windows/Linux/macOS on release or
                                      scheduled validation

  Secrets                             Restricted GitHub Actions
                                      secrets/environment

  Docker                              Optional, not required for library
                                      distribution

  Jenkins/Kubernetes                  Not required initially

  Website/API docs                    GitHub Pages or another static
                                      documentation host, optional
  -----------------------------------------------------------------------

## 14. Go-Live Checklist

Before publishing `1.0.0`, confirm all of the following:

-   [ ] Final `groupId` is verified and the Sonatype publishing identity
    is configured.
-   [ ] Minimum Java version and supported JDK matrix are documented.
-   [ ] Public API and extension points have been reviewed.
-   [ ] Unit, integration, concurrency, and relevant cross-platform
    tests pass.
-   [ ] POM metadata, license, SCM URL, and project documentation are
    complete.
-   [ ] Binary JAR, sources JAR, and JavaDoc JAR build successfully.
-   [ ] Required signatures, checksums, and publisher metadata are
    produced.
-   [ ] GitHub Actions secrets and permissions are configured securely.
-   [ ] Release tag and POM version match.
-   [ ] No unintended `SNAPSHOT` dependencies remain.
-   [ ] The release workflow has been tested with a non-production or
    trial publication where supported.
-   [ ] The artifact can be consumed from a clean Maven project.
-   [ ] Release notes and a recovery plan are ready.

------------------------------------------------------------------------

## 15. Official References

Consult these sources immediately before implementation and release;
requirements can change.

-   Maven dependency mechanism:
    https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
-   Maven POM reference: https://maven.apache.org/pom.html
-   Maven Compiler Plugin:
    https://maven.apache.org/plugins/maven-compiler-plugin/
-   Sonatype Central publishing requirements:
    https://central.sonatype.org/publish/requirements/
-   Publish with Maven through Central Publisher Portal:
    https://central.sonatype.org/publish/publish-portal-maven/
-   GitHub Actions: build and test Java with Maven:
    https://docs.github.com/en/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-maven
-   Semantic Versioning: https://semver.org/
-   Docker overview:
    https://docs.docker.com/get-started/docker-overview/
-   Jenkins agents: https://www.jenkins.io/doc/book/using/using-agents/

**Final recommendation:** Build UnirCore as a well-tested,
framework-independent Java library; distribute it through Maven Central;
use GitHub Actions for automated quality gates; release only from
protected version tags; apply SemVer; and add infrastructure such as
Docker, Jenkins, Kubernetes, or multiple Maven modules only when a
concrete need emerges.
