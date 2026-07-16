<p align="center">
  <img src="src/main/resources/logo-silver-60h.webp" alt="PDFDancer logo" height="60">
</p>

# PDFDancer Java Client

## Overview

### PDF used to be hard to automate. We fixed that.

> Programmatically update real-world PDFs. Even ones you didn't create.

PDFDancer gives you pixel-perfect programmatic control over real-world PDF documents from Java. Locate existing elements
by coordinates or selectors, adjust them precisely, add brand-new content, and ship the modified PDF in memory or on disk.
The same API surface is also available for TypeScript and Python so you can work with the same editing model from other
languages when you need it.

### What Makes PDFDancer Different

- **Pixel-perfect positioning**: Move or add elements at exact coordinates and keep the original layout intact.
- **Form manipulation**: Inspect, fill, and update AcroForm fields programmatically.
- **Coordinate-based selection**: Select objects by position or bounding box.
- **Real PDF editing**: Modify the underlying PDF structure instead of merely stamping overlays.

## Highlights

- Locate images, vector paths, form fields, and pages by index, coordinates, or selectors.
- Add content with dedicated builders (`ImageBuilder`, `PathBuilder`, `LineBuilder`, `BezierBuilder`, `RectangleBuilder`).
- Programmatically control third-party PDFs—modify invoices, contracts, and reports you did not author.
- Add fresh content with precise XY positioning, custom fonts (including runtime TTF uploads), and color helpers.
- Export results as bytes for downstream processing or save directly to disk with one call.
- Works anywhere you can run Java 17+: server-side apps, CLI tools, build steps, or desktop utilities.

## Installation

Artifacts are published under `com.pdfdancer.client:pdfdancer-client-java`. The current version matches `version.properties` (e.g., `0.2.5`).

### Maven

```xml
<dependency>
  <groupId>com.pdfdancer.client</groupId>
  <artifactId>pdfdancer-client-java</artifactId>
  <version>0.2.5</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
implementation("com.pdfdancer.client:pdfdancer-client-java:0.2.5")
```

## Requirements

- Java 17 or newer (toolchains configured in `build.gradle.kts`).
- A PDFDancer API token. Set `PDFDANCER_API_TOKEN` (or `PDFDANCER_TOKEN`) or pass the token explicitly.
- Access to the PDFDancer API host (defaults to `https://api.pdfdancer.com`; override via `PdfDancerHttpClient`).

## Quick Start

### Edit an Existing PDF

```java
import com.pdfdancer.client.rest.PDFDancer;
import com.pdfdancer.common.model.Color;

public class EditPdfExample {
    public static void main(String[] args) throws Exception {

        PDFDancer pdf = PDFDancer.createSession("input.pdf");

        pdf.page(1).newLine()
                .from(72, 520)
                .to(260, 520)
                .color(new Color(70, 70, 70))
                .lineWidth(1.0)
                .add();

        pdf.save("output.pdf");
    }
}
```

## Create a Blank PDF

```java
import com.pdfdancer.client.rest.PDFDancer;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageSize;

import java.io.File;
import java.io.IOException;

public class CreatePdfExample {
    public static void main(String[] args) throws IOException {

        PDFDancer pdf = PDFDancer.createNew(
                PageSize.A4,
                Orientation.PORTRAIT,
                1);

        pdf.newImage()
                .fromFile(new File("logo.png"))
                .at(1, 420, 710)
                .add();

        pdf.save("summary.pdf");
    }
}
```

## Page API

Page numbers are 1-based. `pdf.page(1)` returns a page-scoped client, while `pdf.pages()` returns page clients for the
document. Use `getSnapshot()` on a page client for a read-only page snapshot.

```java
PageClient firstPage = pdf.page(1);
List<PageClient> pages = pdf.pages();
PageSnapshot snapshot = firstPage.getSnapshot();
```

Page-scoped selectors, text editing, and builders automatically restrict the operation to that page.

## Selection

Document- and page-scoped selectors return typed references for images, paths, form XObjects, and form fields. Position
selectors use PDF coordinates and a default tolerance of `0.01` point. Singular selectors return the first match as an
`Optional`; plural selectors return all matches.

```java
List<ImageReference> documentImages = pdf.selectImages();
Optional<ImageReference> logo = pdf.page(1).selectImageAt(72, 680);
List<PathReference> pagePaths = pdf.page(1).selectPaths();
```

Use document or page snapshots when you need read-only inspection of the complete object vocabulary, including text-line
data.

## Builders and Vector Paths

All five dedicated builders are available at document and page scope: `ImageBuilder`, `PathBuilder`, `LineBuilder`,
`BezierBuilder`, and `RectangleBuilder`.

```java
pdf.page(1).newRectangle()
        .at(72, 500)
        .size(220, 80)
        .color(Color.BLACK)
        .fillColor(new Color(255, 255, 200))
        .add();

pdf.page(1).newPath()
        .moveTo(72, 450)
        .lineTo(200, 450)
        .bezierTo(230, 450, 230, 390, 260, 390)
        .dash(6, 3)
        .add();
```

`PathBuilder` also provides `closePath()`, `rect(...)`, `circle(...)`, `dashWithPhase(...)`, and `solid()` conveniences.
A circle is a `PathBuilder` convenience, not a separate builder type.

## Images

Create images at document scope with an explicit page or directly from a page client:

```java
pdf.newImage().fromFile(new File("logo.png")).at(1, 72, 700).add();
pdf.page(1).newImage().fromFile(new File("stamp.png")).at(300, 700).add();
```

`ImageReference` exposes dimensions and aspect ratio and supports replacement from a file or `Image`, proportional or
explicit scaling, cropping, opacity, horizontal and vertical flips, region filling, and rotation. Positive rotation
angles are clockwise. Image transformations return `CommandResult`, which exposes `success()`, `message()`, `warning()`,
and `elementId()`.

## Form Fields

Form-field selection uses the same names at document and page scope. Mutate the selected field directly with
`setValue(...)`:

```java
FormFieldReference signature = pdf.selectFormFieldsByName("signature").get(0);
boolean changed = signature.setValue("Signed by Jane Doe");
```

Selectors return typed references (`ImageReference`, `FormFieldReference`, `PathReference`, `PageClient`, …) with
helpers such as `delete()`, `moveTo(x, y)`, `clearClipping()`, and type-specific mutation methods.

## Text Editing

Text editing is selector-based and is available through `pdf.text()` and `pdf.page(pageNumber).text()`. It supports
replace, delete, insert, and style operations.

### Atomic Style Overrides

Replacement text can override selected style properties in the same operation. Omitted style properties continue to
use the corresponding source-text style.

```java
import com.pdfdancer.common.request.PdfColorRequest;
import com.pdfdancer.common.request.TextReplaceRequest;

pdf.text().replace(TextReplaceRequest.literal("{{company_name}}", "Globex Ltd")
        .font("Helvetica-Bold")
        .size(17)
        .fillColor(PdfColorRequest.rgb(0.1, 0.2, 0.3))
        .sourceAnchored()
        .build());
```

The replacement builder also supports `strokeColor(...)`, `characterSpacing(...)`, `wordSpacing(...)`, and
`resetSpacingOverrides()`. Atomic style overrides apply only to text replacements and cannot be combined with
`replaceWithImage(...)`.

### Control Reflow Hyphenation

Reflowing text edits can override dictionary-generated discretionary hyphenation for one operation. Omit the override
to inherit the selected layout profile. The override is available on replace, insert, delete, and style builders.

```java
import com.pdfdancer.common.request.TextLayoutRequest;
import com.pdfdancer.common.request.TextReplaceRequest;

pdf.text().replace(TextReplaceRequest.literal("Benefits", "International membership benefits")
        .requireReflow(TextLayoutRequest.Profile.BODY_TEXT)
        .hyphenationEnabled(false)
        .build());
```

`hyphenationEnabled(...)` is valid only with `reflowWhenSupported(...)` or `requireReflow(...)`; lexical hyphens in
the supplied text are unaffected.

### Replace Text with an Image

Image replacement uses a PDF affine transformation relative to the matched text range's visually left-most boundary
caret. The transform maps the image's normalized unit square into PDF user-space coordinates.

```java
import com.pdfdancer.client.rest.PDFDancer;
import com.pdfdancer.common.model.PdfAffineTransform;
import com.pdfdancer.common.request.TextReplaceRequest;

import java.io.File;

PDFDancer pdf = PDFDancer.createSession(new File("input.pdf"));

PdfAffineTransform placement = PdfAffineTransform.builder()
        .scale(20, 10)
        .translate(3, -2)
        .build();

pdf.text().replace(TextReplaceRequest.builder()
        .literal("{{logo}}")
        .replaceWithImage(new File("logo.png"), placement)
        .build());

pdf.save("output.pdf");
```

Builder operations affect points in invocation order. The example first maps the unit square to 20 by 10 PDF units,
then offsets it by `(3, -2)` from the caret. The builder also supports `rotateDegrees(...)` and `shear(...)`. For exact
coefficient-level control, use the standard PDF order:

```java
PdfAffineTransform exact = PdfAffineTransform.fromPdfMatrix(
        new double[]{20, 0, 5, 10, 3, -2});
```

For `[a, b, c, d, e, f]`, points are mapped as `x' = a*x + c*y + e` and `y' = b*x + d*y + f`. This supplied transform
is caret-relative; it is not the page's current transformation matrix.

## Shared Models

`Color` uses integral RGBA components in the inclusive range 0–255. Alpha defaults to 255; `BLACK`, `WHITE`, and `RED`
are provided as constants.

`PageSize` provides A0–A6, B4–B5, Letter, Legal, Tabloid, Executive, Postcard, and 3×5 Index sizes. `PageSize.of(...)`
recognizes both portrait and rotated standard dimensions; custom dimensions must be finite and positive.

The exported `ObjectType` vocabulary covers every object type returned by the v2 snapshot and selection APIs.

## Configuration

- `PDFDANCER_API_TOKEN` (or `PDFDANCER_TOKEN`) — preferred way to authenticate; `PDFDancer.createSession(File)` and `PDFDancer.createNew()` will read it automatically.
- `PDFDANCER_BASE_URL` — integration tests (and your code) can override the host via `PdfDancerHttpClient.create(...)`.
- Timeouts — rely on your `HttpClient` instance; defaults to 30 s in `PdfDancerHttpClient.createDefault`.
- Anonymous sessions — `PDFDancer.createSession(File)` will request an ephemeral token when no credentials are provided (useful for local demos).

## Retry and Error Handling

The default HTTP policy makes three total attempts, including the initial request. It uses exponential backoff starting
at one second, a multiplier of two, and a five-second delay cap. Statuses 408, 429, 500, 502, 503, 504, and 520 are
retryable, as are configured timeout and connection failures. `Retry-After` is honored only for HTTP 429; retry delays
do not use jitter. Pass `RetryConfig.noRetry()` to disable retries or build a custom `RetryConfig`.

Failures use the `PdfDancerException` hierarchy: `ValidationException`, `HttpClientException`, `SessionException`,
`SessionNotFoundException`, `FontNotFoundException`, and `RateLimitException`. A rate-limit exception retains a parsed
retry delay when the response supplies one. Use `getDocumentSnapshot()` or `page(n).getSnapshot()` when debugging server
state.

## Development and Testing

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
2. **Provide an API token** via `PDFDANCER_API_TOKEN=your-token` (or `PDFDANCER_TOKEN`, or `-Dpdfdancer.token=...`).
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
- **Empty selections** — call `pdf.getDocumentSnapshot("IMAGE,PATH")` to inspect what the server sees and validate coordinates.
- **Large PDFs** — prefer snapshot APIs to reduce repeated network calls while iterating.

## Contributing

Contributions are welcome through pull requests. Add tests for behavioral changes, run `./gradlew test`, and update the
relevant API documentation with the implementation.

## Helpful Links

- [API documentation](https://docs.pdfdancer.com?utm_source=github&utm_medium=readme&utm_campaign=pdfdancer-java)
- [Product overview](https://www.pdfdancer.com?utm_source=github&utm_medium=readme&utm_campaign=pdfdancer-java)
- [Maven Central](https://central.sonatype.com/artifact/com.pdfdancer.client/pdfdancer-client-java)
- [Changelog](https://www.pdfdancer.com/changelog/?utm_source=github&utm_medium=readme&utm_campaign=pdfdancer-java)
- [Status](https://status.pdfdancer.com?utm_source=github&utm_medium=readme&utm_campaign=pdfdancer-java)
- [Issue tracker](https://github.com/MenschMachine/pdfdancer)

## Related SDKs

- TypeScript client: https://github.com/MenschMachine/pdfdancer-client-typescript
- Python client: https://github.com/MenschMachine/pdfdancer-client-python

## License

Apache License 2.0 © 2025 The Famous Cat Ltd. See `LICENSE` and `NOTICE` for details.
