# Digital Regulatory Reporting (DRR) Setup Guide

This guide will help you set up the Digital Regulatory Reporting project on your system.

## Prerequisites

### 1. Java 21

The project requires Java 21. We recommend using SDKMAN to manage Java versions:

```bash
# Install SDKMAN (if not already installed)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 21
sdk install java 21.0.2-open
sdk use java 21.0.2-open
```

### 2. Maven 3.9+

The project uses Maven for build management. Install Maven:

```bash
# Using Homebrew (macOS)
brew install maven

# Or using SDKMAN
sdk install maven
```

### 3. Git

Ensure Git is installed for version control.

## Quick Setup

### Option 1: Using the Setup Script (Recommended)

```bash
# Make sure you're in the project root directory
cd /path/to/digital-regulatory-reporting-6.10.0

# Run the setup script
./setup.sh
```

### Option 2: Manual Setup

```bash
# 1. Set Java environment
export JAVA_HOME=/Users/vamsi/.sdkman/candidates/java/21.0.2-open
export PATH=$JAVA_HOME/bin:$PATH

# 2. Verify Java version
java -version  # Should show Java 21

# 3. Verify Maven version
mvn -version   # Should show Maven 3.9+ with Java 21

# 4. Build the project
mvn clean compile
```

## Project Structure

```
digital-regulatory-reporting-6.10.0/
├── rosetta-source/          # Main source code and Rosetta DSL files
│   ├── src/main/rosetta/    # Rosetta DSL files (.rosetta)
│   ├── src/main/java/       # Generated Java code
│   └── src/main/resources/  # Configuration files
├── examples/                # Sample usage examples
├── tests/                   # Test cases
├── distribution/            # Distribution artifacts
├── documentation/           # Project documentation
├── pom.xml                  # Maven project configuration
└── setup.sh                 # Setup script
```

## Key Dependencies

### org.iso20022

The project depends on `org.iso20022:rosetta-source` for:

- ISO 20022 standard compliance
- XML serialization configuration
- Rosetta source files for data transformation
- Generated Java classes from ISO 20022 schemas

This dependency is essential for generating regulatory reports in ISO 20022 format required by various financial regulatory authorities.

## Building and Testing

### Build the Project

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Build Distribution

```bash
mvn package
```

### Run Examples

```bash
cd examples
mvn exec:java
```

## Common Issues

### 1. Java Version Issues

If you see Java version errors, ensure you're using Java 21:

```bash
java -version
```

### 2. Maven Repository Access

The project uses private repositories. If you see repository access errors, this is expected for external users. The core functionality should still work.

### 3. Schema Validation Warnings

During testing, you may see schema validation warnings. These are expected as the tests include various edge cases and invalid data scenarios.

## Next Steps

1. **Explore the Examples**: Check the `examples/` directory for usage examples
2. **Review Documentation**: See the `documentation/` directory for detailed guides
3. **Understand Rosetta DSL**: The `rosetta-source/src/main/rosetta/` directory contains the core business logic
4. **Run Tests**: Execute `mvn test` to verify everything is working

## Support

For issues related to:

- **Build problems**: Check Java and Maven versions
- **Runtime errors**: Review the test output for specific error messages
- **Business logic**: Examine the Rosetta DSL files in `rosetta-source/src/main/rosetta/`

## Regulatory Standards Supported

The project supports multiple regulatory reporting standards:

- **ESMA EMIR**: European Securities and Markets Authority
- **FCA UK EMIR**: UK Financial Conduct Authority
- **HKMA**: Hong Kong Monetary Authority
- **MAS**: Monetary Authority of Singapore
- **ASIC**: Australian Securities and Investments Commission
- **CFTC**: Commodity Futures Trading Commission

Each standard requires specific ISO 20022 message formats, which are handled by the `org.iso20022` dependency.
