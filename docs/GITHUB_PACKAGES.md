# Using FileVault from GitHub Packages

FileVault is available as a Maven package from GitHub Packages. This document explains how to use it in your projects.

## Authenticating to GitHub Packages

To use GitHub Packages, you need to authenticate. Create or edit your `~/.m2/settings.xml` file:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

Replace `YOUR_GITHUB_USERNAME` with your GitHub username and `YOUR_GITHUB_TOKEN` with a [Personal Access Token](https://github.com/settings/tokens) that has the `read:packages` scope.

## Adding the repository to your project

Add the repository to your project's `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/OWNER/REPOSITORY</url>
  </repository>
</repositories>
```

Replace `OWNER/REPOSITORY` with the actual repository owner and name.

## Adding the dependency

Add the dependency to your project's `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>com.filevault</groupId>
    <artifactId>FileVault</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

Replace `1.0.0` with the version you want to use.

## Using the package in your project

After adding the dependency, you can use FileVault in your code:

```java
import com.filevault.FileVaultApp;

// Your code using FileVault
```

## Versions

We automatically build and publish packages for each GitHub release. You can see all available versions in the [Packages section](https://github.com/OWNER/REPOSITORY/packages) of the repository. 