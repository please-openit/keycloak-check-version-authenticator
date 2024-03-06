# Please-open.it Keycloak version checker

### Build

Use with Maven (>3) and JDK (>= 11).

```
mvn clean install
```

Copy deployments/version-checker-jar-with-dependencies.jar to /providers/ directory in Keycloak.

### Usage

Add this authenticator to an authentication flow (I.E for the security admin console).