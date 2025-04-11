# FileVault

A secure file organization and encryption system with a modern JavaFX interface.

## Features

- User authentication with master password
- AES file encryption for secure storage
- Virtual folder organization
- Import, export, rename, and delete files
- Modern dark-mode UI with JavaFX

## Requirements

- Java 17 or higher
- Maven for building

## Building the Project

```bash
mvn clean package
```

## Running the Application

```bash
java -jar target/FileVault-1.0-SNAPSHOT.jar
```

Alternatively, you can use Maven to run the application:

```bash
mvn javafx:run
```

## Security Features

- AES encryption for all stored files
- Password hashing with BCrypt
- Secure key derivation with PBKDF2
- No plaintext files stored on disk

## Project Structure

- `controller`: Application controllers
- `model`: Data models
- `view`: JavaFX view components
- `security`: Encryption and password handling
- `storage`: File storage and database management
- `util`: Utility classes 