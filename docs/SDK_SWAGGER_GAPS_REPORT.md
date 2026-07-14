# PDFDancer Java SDK vs Local Swagger Capabilities

Generated: 2026-07-14<br>
Swagger UI: `http://localhost:8080/swagger-ui`  
Primary OpenAPI document: `http://localhost:8080/swagger/pdfdancer-api-0.1.3-2.yml`  
Secondary OpenAPI document inspected: `http://localhost:8080/swagger/pdfdancer-api-0.1.3-internal-2.yml`  
SDK version source: `version.properties` currently reports `DEV`

## Summary

The Java SDK covers the main session lifecycle, object selection, snapshots, page add/delete/move, images, paths, path groups, form-field value changes, clipping, text replace/delete/insert/style, atomic replacement-style overrides, font lookup by name, font registration, and anonymous token creation.

The largest public API gaps are:

1. Session cleanup: `DELETE /session/{sessionId}`.
2. Font catalog endpoints: `GET /font/get`, `GET /font/get/{fontName}`.
3. Partial text styling selector coverage: `range`, `lineRange`, `runs.all`, and hidden-id run filters are not exposed for `POST /pdf/text/style`.
4. Health/version endpoints: `GET /ping`, `GET /version`.
5. User/tenant token management endpoints under `/me`.
6. OAuth and MCP endpoints. These may be intentionally outside the SDK scope.
7. `PUT /pdf/page/modify` and generic `PUT /pdf/modify` have no public high-level wrappers for the current public object model.

Inference: admin, metrics, subscription, product, fontswap, and internal analysis endpoints in the "Internal API" definition are probably not intended for this public client SDK.

## Method

I compared:

- OpenAPI operation list from `pdfdancer-api-0.1.3-2.yml`.
- Internal OpenAPI operation list from `pdfdancer-api-0.1.3-internal-2.yml`.
- SDK source under `src/main/java/com/pdfdancer/client/rest`, `src/main/java/com/pdfdancer/client/rest/mutation`, and `src/main/java/com/pdfdancer/client/rest/session`.

Coverage labels:

- `Covered`: SDK has a direct public method or reference/builder method using the endpoint.
- `Partial`: SDK uses related functionality, but not the whole endpoint contract or not through a public wrapper.
- `Missing`: no SDK wrapper or DTO was found.
- `Out of scope?`: inference; endpoint looks like product/admin/auth infrastructure rather than PDF editing SDK functionality.

## Public API Coverage

### Session And Authentication

| Swagger operation | SDK status | SDK surface |
|---|---:|---|
| `POST /session/create` | Covered | `PDFDancer.createSession(...)` via `SessionService.uploadPdfForSession(...)` |
| `POST /session/new` | Covered | `PDFDancer.createNew(...)` |
| `GET /session/{sessionId}/pdf` | Covered | `PDFDancer.getFileBytes()`, `PDFDancer.save(...)` |
| `DELETE /session/{sessionId}` | Missing | No explicit session close/delete method found |
| `POST /keys/anon` | Covered | `PDFDancer.createSession(File)` and `createNew()` can request anonymous token |

### Fonts

| Swagger operation | SDK status | SDK surface |
|---|---:|---|
| `GET /font/find` | Covered | `PDFDancer.findFonts(String, int)` |
| `POST /font/register` | Covered | `PDFDancer.registerFont(File)` |
| `GET /font/get` | Missing | No list-all-fonts wrapper found |
| `GET /font/get/{fontName}` | Missing | No get-font-by-name wrapper found |

### PDF Object Selection And Snapshots

| Swagger operation | SDK status | SDK surface |
|---|---:|---|
| `POST /pdf/find` | Covered | `selectElements()`, `selectImages()`, `selectPathsAt(...)`, internal `find(...)` |
| `GET /pdf/document/snapshot` | Covered | `getDocumentSnapshot(...)`, typed snapshot cache |
| `GET /pdf/page/{pageNumber}/snapshot` | Covered | `getPageSnapshot(...)`, page-scoped selectors |
| `POST /pdf/page/find` | Covered | `getPages()`, `getPage(int)` |

### PDF Mutation

| Swagger operation | SDK status | SDK surface |
|---|---:|---|
| `POST /pdf/add` | Partial | Public wrappers cover images and path/line/bezier builders. |
| `DELETE /pdf/delete` | Covered | `BaseReference.delete()` and typed references |
| `PUT /pdf/move` | Covered | `BaseReference.moveTo(...)`, `moveX(...)`, `moveY(...)` |
| `PUT /pdf/modify` | Partial | Endpoint exists, but no current generic public wrapper was found for the current public object model |
| `PUT /pdf/modify/formField` | Covered | `FormFieldReference.setValue(...)` |
| `PUT /pdf/modify/path` | Covered | `PathReference.edit().apply()` |
| `PUT /pdf/clipping/clear` | Covered | `BaseReference.clearClipping()`, `PDFDancer.clearClipping(...)` |

### Text Editing

| Swagger operation | SDK status | SDK surface |
|---|---:|---|
| `POST /pdf/text/replace` | Covered | `PDFDancer.text().replace(...)`, `PDFDancer.page(n).text().replace(...)`; supports text replacement, caret-relative bitmap replacement with `PdfAffineTransform`, and atomic replacement-text `style` overrides through `TextStyleSetRequest` or fluent builder methods |
| `POST /pdf/text/delete` | Covered | `PDFDancer.text().delete(...)`, `PDFDancer.page(n).text().delete(...)` |
| `POST /pdf/text/insert` | Covered | `PDFDancer.text().insert(...)`, `PDFDancer.page(n).text().insert(...)`; supports anchor insertion with optional `style.patch` overrides and coordinate insertion with complete style patch |
| `POST /pdf/text/style` | Partial | `PDFDancer.text().style(...)`, `PDFDancer.page(n).text().style(...)`; supports literal/regex selectors, `runs.where` non-id filters, and all style patch fields, but not `range`, `lineRange`, `runs.all`, or hidden-id run filters |

### Pages

| Swagger operation | SDK status | SDK surface |
|---|---:|---|
| `POST /pdf/page/add` | Covered | `addPage(...)`, `newPage().add()`, deprecated `page().add()` |
| `DELETE /pdf/page/delete` | Covered | `deletePage(...)`, `page(n).delete()` |
| `PUT /pdf/page/move` | Covered | `movePage(int, int)` |
| `PUT /pdf/page/modify` | Missing | No page-modify wrapper found |

### Images

| Swagger operation | SDK status | SDK surface |
|---|---:|---|
| `PUT /pdf/image/transform` | Covered | `ImageReference.scale`, `scaleTo`, `rotate`, `crop`, `opacity`, `flip`, `fillRegion`, `replace` |

### Path Groups

| Swagger operation | SDK status | SDK surface |
|---|---:|---|
| `GET /pdf/page/{pageNumber}/path-groups` | Covered | `getPathGroups(int)`, `page(n).getPathGroups()` |
| `POST /pdf/path-group/create` | Covered | `page(n).groupPaths(...)`, `groupPathsInRegion(...)` |
| `PUT /pdf/path-group/move` | Covered | `PathGroupReference.moveTo(...)` |
| `PUT /pdf/path-group/transform` | Covered | `scale(...)`, `rotate(...)`, `resize(...)` |
| `DELETE /pdf/path-group/remove` | Covered | `PathGroupReference.remove()` |
| `PUT /pdf/path-group/clipping/clear` | Covered | `PathGroupReference.clearClipping()`, `clearPathGroupClipping(...)` |

### Tenant, Token, OAuth, MCP, Proof, Health

| Swagger group | SDK status | Notes |
|---|---:|---|
| `GET /me/tenant` | Missing | No tenant profile wrapper found |
| `/me/tokens/*` | Missing | No API-token management wrapper found |
| `/oauth/*` | Missing / out of scope? | OAuth browser and dynamic registration endpoints |
| `/mcp/*` | Missing / out of scope? | MCP OAuth and upload/download helpers |
| `POST /proof-requests` | Missing / out of scope? | No matching SDK model found |
| `GET /ping` | Missing | Useful for SDK health checks |
| `GET /version` | Missing | Useful for compatibility checks |
| `GET /` | Missing / out of scope? | Landing/root endpoint |
| `/.well-known/*` | Missing / out of scope? | OAuth metadata endpoints |

## Internal API Definition

The Swagger UI also exposes `pdfdancer-api-0.1.3-internal-2.yml`. The Java SDK has no wrappers for these internal groups:

- `/admin/tenants/*`
- `/metrics/*`
- `/fontswap/*`
- `/subscription/*`
- `/product/*`
- `/stripe/webhook`
- `/config`
- `/test`
- `/user`
- `POST /pdf/analyze`

Inference: these endpoints look operational, billing, analytics, admin, or product-internal. They should not be added to the public Java SDK unless the SDK is meant to become an admin/internal client.

## Schema And Model Gaps

Missing public OpenAPI DTO families in the Java SDK:

- Font catalog responses: `FontNamesResponse`.
- Tenant/token management: `UserTenantResponse`, token create/list/reveal response DTOs.
- OAuth/MCP request and response models.
- Health/version: `VersionResponse`.
- Page modify request model for `PUT /pdf/page/modify`.
- Text style selector DTOs for `POST /pdf/text/style`: `TextStyleRangeSelectorRequest`, `TextStyleLineRangeSelectorRequest`, plus hidden-id run filters `runIds`, `reflowUnitIds`, and `elementIdsAny`.

Current alignment:

- `TextReplaceRequest` matches the public replacement contract, including `style.font`, `size`, fill/stroke colors, character/word spacing, and `resetSpacingOverrides`; styled bitmap replacement is rejected as required by the API.
- The SDK still uses Java-specific convenience objects and builders rather than generated OpenAPI types for several covered operations.

## SDK-Only Convenience Surface

These are not gaps; they are Java convenience abstractions over covered endpoints:

- Reference objects: `ImageReference`, `PathReference`, `PathGroupReference`, `FormFieldReference`.
- Builders: `ImageBuilder`, `PathBuilder`, `LineBuilder`, `BezierBuilder`, `PageBuilder`.
- Snapshot caching through `SnapshotCache`.
- Anonymous session fallback when no token environment variable is configured.

## Recommended Priorities

1. Add `PDFDancer.close()` or `deleteSession()` for `DELETE /session/{sessionId}`.
2. Add font catalog methods for `GET /font/get` and `GET /font/get/{fontName}`.
3. Complete text styling selector coverage:
   - add style `range`;
   - add style `lineRange`;
   - add style `runs.all` and hidden-id run filters if the SDK exposes a source for those ids.
4. Add `ping()` and `version()` for diagnostics and backend compatibility checks.
5. Decide whether tenant/token endpoints belong in this SDK. If yes, add a separate auth/account service to avoid mixing them with document editing APIs.
6. Decide whether MCP/OAuth/proof-request endpoints are intentionally out of scope and document that decision.
7. Add page modification support only if `PUT /pdf/page/modify` has a stable use case not already covered by add/delete/move.
