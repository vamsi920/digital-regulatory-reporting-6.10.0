#!/bin/bash

# Digital Regulatory Reporting (DRR) Setup Script
# This script sets up the Java environment and builds the project

echo "Setting up Digital Regulatory Reporting (DRR) project..."

# Check if SDKMAN is available
if command -v sdk &> /dev/null; then
    echo "SDKMAN found. Setting up Java 21..."
    
    # Check if Java 21 is installed
    if sdk list java | grep -q "21.0.2-open"; then
        echo "Java 21.0.2-open is already installed."
    else
        echo "Installing Java 21.0.2-open..."
        sdk install java 21.0.2-open
    fi
    
    # Use Java 21
    sdk use java 21.0.2-open
    
    # Set JAVA_HOME
    export JAVA_HOME=/Users/vamsi/.sdkman/candidates/java/21.0.2-open
    export PATH=$JAVA_HOME/bin:$PATH
    
    echo "Java version:"
    java -version
    
    echo "Maven version:"
    mvn -version
    
else
    echo "SDKMAN not found. Please install SDKMAN first:"
    echo "curl -s \"https://get.sdkman.io\" | bash"
    echo "source \"\$HOME/.sdkman/bin/sdkman-init.sh\""
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found. Please run this script from the project root directory."
    exit 1
fi

echo ""
echo "Building the project..."
echo "This may take several minutes on first build..."

# Clean and build
mvn clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Project setup completed successfully!"
    echo ""
    echo "Next steps:"
    echo "1. Run tests: mvn test"
    echo "2. Build distribution: mvn package"
    echo "3. Run examples: cd examples && mvn exec:java"
    echo ""
    echo "Project structure:"
    echo "- rosetta-source/: Contains Rosetta DSL files and generated Java code"
    echo "- examples/: Sample usage examples"
    echo "- tests/: Test cases"
    echo "- distribution/: Distribution artifacts"
    echo ""
    echo "For more information, see the documentation/ directory."
else
    echo ""
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi
