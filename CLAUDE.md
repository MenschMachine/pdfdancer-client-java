# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

PDFDancer Java Client — a Java 11+ SDK for programmatically editing PDF documents through the PDFDancer REST API. Published to Maven Central as `com.pdfdancer.client:pdfdancer-client-java`.

## Build & Test Commands

```bash
./gradlew build                          # compile + test
./gradlew compileJava                    # compile only
./gradlew test                           # run all tests
./gradlew test --tests "*TextLineTest"   # run a single test class
./gradlew test --tests "*TextLineTest.modifyLine"  # run a single test method
./gradlew printVersion                   # show current version
./gradlew bumpPatch                      # increment patch version in version.properties
```

**Tests are integration tests** that require a running PDFDancer API server:
- Default URL: `http://localhost:8080/` (override with env `PDFDANCER_BASE_URL` or `-Dpdfdancer.baseUrl=...`)
- Default token: `42` (override with env `PDFDANCER_API_TOKEN` or `PDFDANCER_TOKEN` or `-Dpdfdancer.token=...`)
- Test fixtures live in `src/test/resources/fixtures/`

## Architecture

### Layered Client Design

The client follows a **facade + reference + builder** pattern:

1. **`PDFDancer`** — main entry point. Creates sessions (uploading a PDF or creating blank), returns the facade for all operations. Delegates to service layer.
2. **Service layer** (`SessionService`, `SelectionService`, `ModificationService`) — handles HTTP calls for session management, querying PDF objects, and applying mutations.
3. **Reference objects** (`TextParagraphReference`, `TextLineReference`, `ImageReference`, `FormFieldReference`, `PathReference`) — represent live PDF objects returned by selection queries. Each provides `edit()`, `delete()`, `moveTo()`, etc.
4. **Builders** (`ParagraphBuilder`, `ImageBuilder`, `LineBuilder`, `BezierBuilder`, `PathBuilder`, `ReplaceBuilder`) — fluent APIs for creating new content or performing template replacements. Terminal operations: `.add()` or `.apply()` returning boolean.
5. **`PdfDancerHttpClient`** — custom HTTP abstraction over Java's `HttpClient` with Jackson serialization, retry logic (exponential backoff for 408/429/5xx), and version header injection.
6. **`SnapshotCache`** — caches `DocumentSnapshot`/`PageSnapshot` to avoid repeated network calls when inspecting server state.

### Package Layout

- `com.pdfdancer.client.rest` — core client API (PDFDancer, references, builders, HTTP client, page client)
- `com.pdfdancer.client.http` — low-level HTTP request/response abstractions
- `com.pdfdancer.client.mutation` — ModificationService
- `com.pdfdancer.client.selection` — SelectionService
- `com.pdfdancer.client.session` — SessionService
- `com.pdfdancer.common.model` — DTOs: Paragraph, TextLine, Word, Position, BoundingRect, Color, Font, Path, Form, Image, Page
- `com.pdfdancer.common.model.text` — text hierarchy (Paragraph → TextLine → Word)
- `com.pdfdancer.common.model.path` — vector geometry (Path, Line, Bezier)
- `com.pdfdancer.common.request` — request DTOs (ModifyTextRequest, TemplateReplacement, etc.)
- `com.pdfdancer.common.response` — response DTOs (DocumentSnapshot, PageSnapshot, ErrorResponse)
- `com.pdfdancer.common.util` — helpers (StandardFonts, TextMeasurementUtil, FileUtils)

### Test Infrastructure

All test classes extend `BaseTest`, which provides:
- Static `PdfDancerHttpClient` setup via `@BeforeAll`
- `createClient()` — loads a fixture PDF (`ObviouslyAwesome.pdf` by default)
- `createClient(String fixture)` — loads a specific fixture
- `newPdf()` — creates a blank PDF session
- `saveTo(client, filename)` — writes result to temp dir

Additional test support:
- `TestPDFDancer` — static factory for test session creation
- `PDFAssertions` — snapshot-based fluent assertions (e.g., `assertTextlineExists`, `assertTextlineHasFont`)
- `TestUtil` — assertion helpers like `assertBetween`

### API Schema Updates

New API features are tracked via OpenAPI schemas in `docs/api-schemas/` (v0.yml, v1.yml). The `/implement-new-api-features` command analyzes these for new PDF-related features to implement.

## Key Conventions

- Fluent builders return `this`; terminal operations (`add()`, `apply()`, `delete()`) return `boolean`
- Selection methods return `List<T>` or `Optional<T>`
- All HTTP errors surface as `PdfDancerClientException` (unchecked)
- Logging via SLF4J; use `log.debug()` level
- Version managed in `version.properties`, embedded into JAR via resource filtering of `pdfdancer-client.properties`
