# Contributing to Jason

Thank you for your interest in contributing to Jason! This document provides guidelines and instructions for contributing.

## Getting Started

1. **Fork** the repository on GitHub
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/<your-username>/jason.git
   cd jason
   ```
3. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Building the Project

Jason uses Gradle as its build system. JDK 21 is required.

```bash
# Build and configure JasonCLI
./gradlew config

# Run Java unit tests
./gradlew :jason-interpreter:test

# Run Jason agent unit tests
./gradlew :jason-interpreter:testJason

# Generate JAR files
./gradlew jar

# Generate documentation (requires Docker for AsciiDoc)
./gradlew doc
```

## Running Examples

```bash
cd examples/domestic-robot
jason DomesticRobot.mas2j
```

## Making Changes

### Code Style
- Follow existing code conventions in the project
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods focused and concise

### Commit Messages
- Use clear, descriptive commit messages
- Start with a verb in the imperative mood (e.g., "Add", "Fix", "Update")
- Reference issue numbers when applicable (e.g., "Fix #123: handle null pointer in parser")

## Submitting a Pull Request

1. **Push** your branch to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
2. Open a **Pull Request** against the `master` branch
3. Provide a clear description of your changes
4. Ensure all CI checks pass

## Reporting Issues

- Use the [GitHub Issues](https://github.com/jason-lang/jason/issues) page
- Search for existing issues before creating a new one
- Include steps to reproduce, expected behavior, and actual behavior
- Mention your Java version and operating system

## Questions?

- Check the [FAQ](https://jason-lang.github.io/jason/doc/faq.html)
- Visit the [Jason website](https://jason-lang.github.io)

Thank you for helping improve Jason! 🎉
