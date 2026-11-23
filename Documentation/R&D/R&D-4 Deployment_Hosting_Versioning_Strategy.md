# UniCore — Deployment, Hosting, and Versioning Strategy

**Status:** Production Release Engineering Plan  
**Distribution Target:** Maven Central  
**Artifact Coordinates:** `com.unicore:unicore`  
**Baseline:** Java 21 LTS  

---

## 1. Distribution Model

UniCore is an embedded Java library. Consumers add it to their `pom.xml` or `build.gradle` without deploying external servers or infrastructure daemons:

```xml
<dependency>
    <groupId>com.unicore</groupId>
    <artifactId>unicore</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 2. Release Pipeline (GitHub Actions & Sonatype)

```text
Developer Push / PR
       │
       ▼
GitHub Actions CI (Compile, JUnit 5 Tests, Javadoc validation)
       │
       ▼
Git Tag (e.g. v1.0.0)
       │
       ▼
Release Workflow: Build JAR + Sources + Javadoc + PGP Signatures
       │
       ▼
Sonatype Central Publisher Portal ──► Maven Central Repository
```

---

## 3. Semantic Versioning (SemVer)

- **MAJOR (X.0.0):** Incompatible public API breaks in scheduling interfaces or PCB models.
- **MINOR (1.X.0):** New algorithms added (e.g., adding an additional disk scheduling variant) in a backward-compatible manner.
- **PATCH (1.0.X):** Bug fixes, performance optimizations, or Javadoc corrections.
