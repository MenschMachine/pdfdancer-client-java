# Clear Clipping Capability

Most of the PDF model classes including container classes like PDFParagraph/PDFTextLine/PDFPathGroup now implement ClippingDetachable with the method clearClipping().
This removes any clipping path which was active for this element.

This is useful in case clients want to move an element but the new position is hidden by the clipping path. clearing it makes the element visible again.

The new backend is version 1.8.6-rc2, available in the local m2 repository

## Api Docs

Explain for all elements and under the clipping-section.

## Website and Marketing

Not to mention there.

## What's new Newsletter

include

## Changelog

include

## Implementation in pdfdancer-client-java

Clear clipping support was added as a first-class operation on reference types:
- `BaseReference.clearClipping()` now delegates to the client so all object references inheriting from `BaseReference` (for example text lines, paragraphs, images, and paths) can clear clipping directly.
- `PathGroupReference.clearClipping()` was added for grouped paths.

Client and service plumbing was added to call backend mutation endpoints:
- `PDFDancer.clearClipping(ObjectRef)` and `PDFDancer.clearPathGroupClipping(int pageIndex, String groupId)` invoke mutation methods and invalidate snapshot caches after successful changes.
- `ModificationService` now calls:
  - `PUT /pdf/clipping/clear` with `ClearClippingRequest`
  - `PUT /pdf/path-group/clipping/clear` with `ClearPathGroupClippingRequest`
  Both requests include bearer auth and `X-Session-Id` like other mutation operations.

Documentation and verification updates in this repo:
- `README.md` selector helper list now includes `clearClipping()`.
- `ClippingTest` was expanded with integration coverage for clearing clipping on path, text line, image, paragraph, and path group references, including move-after-clear behavior.
- `PDFAssertions` gained `assertPathWithIdIsAt(...)` to validate post-move coordinates in clipping scenarios.
