#!/bin/bash

# Clean and rebuild project to ensure view bindings are generated

echo "Cleaning project..."
./gradlew clean

echo "Building project to generate view bindings..."
./gradlew :app:assembleDebug --info

echo "View bindings should now be generated in:"
echo "app/build/generated/data_binding_base_class_source_out/debug/out/"

echo "Common issues if bindings are not generated:"
echo "1. Check that viewBinding = true in build.gradle.kts"
echo "2. Ensure layouts have proper XML structure"
echo "3. Check for compilation errors in logs"
echo "4. Sync project with Gradle files"