<p align="center">
  <img src="src/main/resources/logo-orange-60h.webp" alt="PDFDancer logo" height="60">
</p>

# PDFDancer Java Client

> Edit text in any real-world PDF. Even ones you didn't create.

PDFDancer gives you pixel-perfect programmatic control over real-world PDF documents from Java. Locate existing elements
by coordinates or text, adjust them precisely, add brand-new content, and ship the modified PDF in memory or on disk.
The same API surface is also available for TypeScript and Python so you can work with the same editing model from other
languages when you need it.

> Need the raw API schema? The latest OpenAPI description lives in `docs/openapi.yml` (when included) and is published at
> https://bucket.pdfdancer.com/api-doc/development-0.0.yml.

## Highlights

- Locate paragraphs, text lines, images, vector paths, form fields, and pages by index, coordinates, text prefixes, or selectors.
- Edit existing content in place with fluent builders (`TextParagraphReference#edit`, `ParagraphBuilder`, `ImageBuilder`, `PathBuilder`).
- Programmatically control third-party PDFs—modify invoices, contracts, and reports you did not author.
- Add fresh content with precise XY positioning, custom fonts (including runtime TTF uploads), and color helpers.
- Export results as bytes for downstream processing or save directly to disk with one call.
- Works anywhere you can run Java 11+: server-side apps, CLI tools, build steps, or desktop utilities.

## What Makes PDFDancer Different

- **Edit text in real-world PDFs**: Work with documents from customers, governments, or vendors—not just ones you generated.
- **Pixel-perfect positioning**: Move or add elements at exact coordinates and keep the original layout intact.
- **Surgical text replacement**: Swap, rewrite, or restyle paragraphs without losing the rest of the page.
- **Form manipulation**: Inspect, fill, and update AcroForm fields programmatically.
- **Coordinate-based selection**: Select objects by position, bounding box, or text patterns.
- **Real PDF editing**: Modify the underlying PDF structure instead of merely stamping overlays.

## Installation

Artifacts are published under `com.pdfdancer.client:pdfdancer-client-java`. The current version matches `version.properties` (e.g., `0.1.1`).

### Maven

```xml
<dependency>
  <groupId>com.pdfdancer.client</groupId>
  <artifactId>pdfdancer-client-java</artifactId>
  <version>0.1.1</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
implementation("com.pdfdancer.client:pdfdancer-client-java:0.1.1")
```

### Requirements

- Java 11 or newer (toolchains configured in `build.gradle.kts`).
- A PDFDancer API token. Set `PDFDANCER_TOKEN` or pass the token explicitly.
- Access to the PDFDancer API host (defaults to `https://api.pdfdancer.com`; override via `PdfDancerHttpClient`).

## Quick Start — Edit an Existing PDF

```java
import com.pdfdancer.client.rest.PDFDancer;
import com.pdfdancer.client.rest.TextParagraphReference;
import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.util.StandardFonts;

public class EditPdfExample {
    public static void main(String[] args) throws Exception {

        PDFDancer pdf = PDFDancer.createSession("input.pdf");

        TextParagraphReference heading = pdf.page(0)
                .selectParagraphsStartingWith("Executive Summary")
                .get(0);

        heading.moveTo(72, 680);
        heading.edit()
                .replace("Overview")
                .font(StandardFonts.HELVETICA.getFontName(), 14)
                .lineSpacing(1.3)
                .color(new Color(40, 40, 40))
                .apply();

        pdf.newParagraph()
                .text("Generated with PDFDancer")
                .font(StandardFonts.HELVETICA.getFontName(), 12)
                .color(new Color(70, 70, 70))
                .at(0, 72, 520)
                .add();

        pdf.save("output.pdf");
    }
}
```

## Create a Blank PDF

```java
import com.pdfdancer.client.rest.PDFDancer;
import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageSize;
import com.pdfdancer.common.util.StandardFonts;

import java.io.File;
import java.io.IOException;

public class CreatePdfExample {
    public static void main(String[] args) throws IOException {

        PDFDancer pdf = PDFDancer.createNew(
                PageSize.A4,
                Orientation.PORTRAIT,
                1);

        pdf.newParagraph()
                .text("Quarterly Summary")
                .font(StandardFonts.TIMES_BOLD.getFontName(), 18)
                .color(new Color(10, 10, 80))
                .at(0, 72, 730)
                .add();

        pdf.newImage()
                .fromFile(new File("logo.png"))
                .at(0, 420, 710)
                .add();

        pdf.save("summary.pdf");
    }
}
```

## Work with Forms, Layout, and Geometry

```java
import com.pdfdancer.client.rest.FormFieldReference;
import com.pdfdancer.client.rest.ImageReference;
import com.pdfdancer.client.rest.PDFDancer;

import java.io.File;

public class FormExample {
    public static void main(String[] args) {
        PDFDancer pdf = PDFDancer.createSession(new File("contract.pdf")); // uses env token + default cloud API

        int totalPages = pdf.getPages().size();
        System.out.println("Total pages: " + totalPages);

        FormFieldReference signature = pdf.selectFormFieldsByName("signature").get(0);
        signature.setValue("Signed by Jane Doe");

        for (ImageReference image : pdf.page(1).selectImages()) {
            Double x = image.getPosition().getX();
            if (x != null && x < 100) {
                image.delete();
            }
        }

        pdf.save("contract-updated.pdf");
    }
}
```

Selectors return typed objects (`TextParagraphReference`, `TextLineReference`, `ImageReference`, `FormFieldReference`,
`PathReference`, `PageClient`, …) with helpers such as `delete()`, `moveTo(x, y)`, `edit()`, or `setValue()` depending on
what you grabbed.

## Configuration

- `PDFDANCER_TOKEN` — preferred way to authenticate; `PDFDancer.createSession(File)` and `PDFDancer.createNew()` will read it automatically.
- `PDFDANCER_BASE_URL` — integration tests (and your code) can override the host via `PdfDancerHttpClient.create(...)`.
- Timeouts — rely on your `HttpClient` instance; defaults to 30 s in `PdfDancerHttpClient.createDefault`.
- Anonymous sessions — `PDFDancer.createSession(File)` will request an ephemeral token when no credentials are provided (useful for local demos).

## Error Handling

All HTTP failures surface as `PdfDancerClientException` (with status/message). Font lookups can raise
`FontNotFoundException`. Most edit operations return booleans so you can fail fast when something could not be applied.
Use `PDFDancer.getDocumentSnapshot()` / `getPageSnapshot()` to inspect the server state when debugging.

## Development

```bash
# Install dependencies and compile
./gradlew build

# Run the full test suite
./gradlew test

# Publish artifacts (requires Sonatype credentials)
./gradlew publish
```

### Publishing to Maven Central

This project uses the new Maven Central publishing process. There are two ways to publish:

#### Option 1: Direct Publishing (Automated)

Publish directly to Maven Central using Gradle:

```bash
# Set your credentials (or add to ~/.gradle/gradle.properties)
export CENTRAL_PORTAL_USERNAME="your-username"
export CENTRAL_PORTAL_PASSWORD="your-password"

# Configure signing (or add to ~/.gradle/gradle.properties)
export SIGNING_KEY_FILE="/path/to/your/private-key.asc"
export SIGNING_PASSWORD="your-key-password"

# Publish to Maven Central
./gradlew publish
```

**gradle.properties example:**
```properties
centralPortalUsername=your-username
centralPortalPassword=your-password
signing.keyFile=/path/to/your/private-key.asc
signing.password=your-key-password
```

#### Option 2: Manual Bundle Upload

Create a bundle.zip and upload it manually to Maven Central:

```bash
# 1. Configure signing credentials
export SIGNING_KEY_FILE="/path/to/your/private-key.asc"
export SIGNING_PASSWORD="your-key-password"

# 2. Create the bundle
./gradlew mavenCentralBundle

# 3. Upload the bundle
# The bundle will be created at: build/distributions/bundle.zip
```

Then upload `build/distributions/bundle.zip` to [Maven Central Portal](https://central.sonatype.com/publishing):

1. Log in to https://central.sonatype.com/
2. Click "Publish" → "Upload Bundle"
3. Select `build/distributions/bundle.zip`
4. Click "Publish"

The bundle contains all required artifacts:
- JAR files (main, sources, javadoc)
- POM file with complete metadata
- Gradle module metadata
- GPG signatures (.asc files)
- MD5 and SHA1 checksums

**Prerequisites for Maven Central:**
- A verified namespace (e.g., `com.pdfdancer.client`)
- A GPG key pair for signing artifacts
- Maven Central account credentials

For more information, see the [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/).

### Integration / E2E Tests

The tests in `src/test/java` exercise the live API. To run them locally:

1. **Start a PDFDancer server** at `http://localhost:8080` (tests default to this host; override with `PDFDANCER_BASE_URL` if needed).
2. **Provide an API token** via `PDFDANCER_TOKEN=your-token` (or `-Dpdfdancer.token=...`).
3. **Ensure fixtures exist** under `src/test/resources/fixtures/` (`ObviouslyAwesome.pdf`, `mixed-form-types.pdf`, `basic-paths.pdf`, `Showcase.pdf`, `logo-80.png`, `DancingScript-Regular.ttf`, `JetBrainsMono-Regular.ttf`, ...).
4. Run `./gradlew test`.

### Project Structure

```
pdfdancer-client-java/
├── src/main/java/com/pdfdancer/   # Client, models, builders, HTTP plumbing
├── src/test/java/com/pdfdancer/   # Integration-style tests against a running API instance
├── build.gradle.kts               # Build, publishing, and signing config
├── version.properties             # Artifact version
└── README.md                      # You are here
```

## Troubleshooting

- **SSL / Network errors** — confirm your `HttpClient` trusts the API endpoint or supply a custom client with the needed SSLContext.
- **Font issues** — use `PDFDancer.findFonts("Helvetica", 12)` or upload a TTF via `pdf.registerFont(new File("MyFont.ttf"))`.
- **Empty selections** — call `pdf.getDocumentSnapshot("PARAGRAPH")` to inspect what the server sees and validate coordinates.
- **Large PDFs** — prefer snapshot APIs to reduce repeated network calls while iterating.

## Related SDKs

- TypeScript client: https://github.com/MenschMachine/pdfdancer-client-typescript
- Python client: https://github.com/MenschMachine/pdfdancer-client-python

## License

Apache License 2.0 © 2025 The Famous Cat Ltd. See `LICENSE` and `NOTICE` for details.
