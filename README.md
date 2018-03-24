# accessible-pdfbox-example
This example demonstrates generating a PDF with images that will pass the Adobe accessibility test.
## Demonstrates
1. Setting up document title, view preferences, language, and mark info.
2. Setting page tab order option.
3. Creating and building document structure.
4. Adding marked content for text.
5. Adding marked content and setting alt-text for images.

## Build
mvn clean install
## Run 
mvn exec:java -Dexec.mainClass=edu.yale.library.accessiblepdf.App