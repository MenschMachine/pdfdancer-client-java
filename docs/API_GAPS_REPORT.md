# PDFDancer Java SDK API Gaps Report

**Report Date**: 2025-01-14
**Java SDK Version**: 0.1.3
**Documentation Version**: 3.1 (based on Python 0.2.22 / TypeScript 1.0.17)

## Executive Summary

The PDFDancer Java SDK (v0.1.3) is missing several API methods that are documented and available in the Python (v0.2.22) and TypeScript (v1.0.17) SDKs. This report documents all missing functionality that prevents the Java examples from compiling.

---

## Missing API Methods

### 1. **Page Management**

#### `pages()` method
- **Status**: ❌ Missing
- **Current Workaround**: Use `getPages()` instead
- **Documentation Reference**: `/quickstart`, `/working-with-pages`
- **Expected Signature**: `List<Page> pages()`
- **What Works**: `List<PageRef> getPages()`

**Example from Docs**:
```java
// Documented API (doesn't work)
List<Page> pages = pdf.pages();

// Current working API
List<PageRef> pages = pdf.getPages();
```

#### `newPage()` method
- **Status**: ❌ Missing
- **Current Workaround**: None found
- **Documentation Reference**: `/working-with-pages#adding-pages-to-existing-documents`
- **Expected Signature**: `PageRef newPage()`

**Example from Docs**:
```java
// Documented API (doesn't work)
PDFDancer pdf = PDFDancer.createSession("document.pdf");
PageRef newPageRef = pdf.newPage();

// No working alternative found
```

#### `page().delete()` method
- **Status**: ❌ Missing
- **Current Workaround**: None found
- **Documentation Reference**: `/working-with-pages#deleting-pages`
- **Expected Signature**: `void delete()` on `PageClient`

**Example from Docs**:
```java
// Documented API (doesn't work)
pdf.page(2).delete();

// No working alternative found
```

#### `duplicatePage()` method
- **Status**: ❌ Missing
- **Current Workaround**: None found
- **Documentation Reference**: Examples only
- **Expected Signature**: `void duplicatePage(int pageIndex)`

**Example**:
```java
// Expected API (doesn't work)
pdf.duplicatePage(0);

// No working alternative found
```

---

### 2. **Form Field Operations**

#### `fill()` method on FormFieldReference
- **Status**: ❌ Missing
- **Current Workaround**: Use `edit().value(x).apply()` instead
- **Documentation Reference**: `/working-with-acroforms#filling-form-fields`
- **Expected Signature**: `void fill(String value)`
- **What Works**: `field.edit().value(value).apply()`

**Example from Docs**:
```java
// Documented API (doesn't work)
if (!fields.isEmpty()) {
    fields.get(0).fill("John Doe");
}

// Current working API
if (!fields.isEmpty()) {
    fields.get(0).edit().value("John Doe").apply();
}
```

#### `getName()` method on FormFieldReference
- **Status**: ❌ Missing
- **Current Workaround**: Use `getInternalId()` or other properties
- **Documentation Reference**: `/working-with-acroforms`
- **Expected Signature**: `String getName()`

**Example**:
```java
// Documented API (doesn't work)
String name = field.getName();

// Possible workaround
String id = field.getInternalId();
```

#### `getObjectType()` method on FormFieldReference
- **Status**: ❌ Missing
- **Documentation Reference**: `/working-with-acroforms`
- **Expected Signature**: `ObjectType getObjectType()`

**Example from Docs**:
```java
// Documented API (doesn't work)
if (field.getObjectType() == ObjectType.CHECK_BOX) {
    field.fill("Off");
}

// No working alternative found
```

#### `ObjectType.CHECK_BOX` constant
- **Status**: ❌ Missing
- **Documentation Reference**: `/working-with-acroforms`
- **Expected**: Enum value in `ObjectType`

---

### 3. **Text Operations**

#### `newParagraph()` method
- **Status**: ❌ Missing
- **Documentation Reference**: `/working-with-text#adding-paragraphs`, `/quickstart#adding-new-content`
- **Expected Signature**: `ParagraphBuilder newParagraph()`

**Example from Docs**:
```java
// Documented API (doesn't work)
pdf.newParagraph()
    .text("Hello World")
    .font("Helvetica", 12)
    .at(0, 100, 500)
    .add();

// No working alternative found
```

---

### 4. **Position/Coordinate Methods**

#### `Position.x()` and `Position.y()` methods
- **Status**: ❌ Missing (method name mismatch)
- **Current Workaround**: Use `getX()` and `getY()` instead
- **Documentation Reference**: All positioning examples
- **Expected Signature**: `double x()`, `double y()`
- **What Works**: `double getX()`, `double getY()`

**Example**:
```java
// Documented API (doesn't work)
double x = position.x();
double y = position.y();

// Current working API
double x = position.getX();
double y = position.getY();
```

---

## Comparison with Other SDKs

| Feature | Python 0.2.22 | TypeScript 1.0.17 | Java 0.1.3 | Status |
|---------|---------------|-------------------|------------|--------|
| `pages()` | ✅ | ✅ | ❌ | Missing |
| `newPage()` | ✅ | ✅ | ❌ | Missing |
| `page.delete()` | ✅ | ✅ | ❌ | Missing |
| `duplicatePage()` | ✅ | ✅ | ❌ | Missing |
| `field.fill()` | ✅ | ✅ | ❌ | Missing |
| `field.getName()` | ✅ | ✅ | ❌ | Missing |
| `field.getObjectType()` | ✅ | ✅ | ❌ | Missing |
| `newParagraph()` | ✅ | ✅ | ❌ | Missing |
| `position.x()` / `position.y()` | ✅ | ✅ | ⚠️ | Use `getX()` / `getY()` |
| Anonymous auth | ✅ | ✅ | ✅ | Working |
| `getPages()` | N/A | N/A | ✅ | Legacy method |

---

## Impact on Examples

The following example files cannot compile due to missing API methods:

### High Priority (Core Features)
1. **ExampleApp.java** - Main example (uses `pages()`)
2. **InspectDocument.java** - Quickstart example (uses `pages()`)
3. **DuplicatePage.java** - Quickstart example (uses `pages()`, `duplicatePage()`)
4. **ExtractText.java** - Quickstart example (works, already using correct API)

### Forms Examples
5. **ListFields.java** - Uses `getName()`, `getObjectType()`
6. **FillFields.java** - Uses `fill()`
7. **CheckBoxes.java** - Uses `fill()`
8. **ClearFields.java** - Uses `fill()`, `getObjectType()`, `ObjectType.CHECK_BOX`

### Page Operations
9. **AddBlankPage.java** - Uses `newPage()`, `pages()`
10. **DeletePages.java** - Uses `pages()`, `page().delete()`
11. **ReorderPages.java** - Uses `pages()`
12. **ExtractPages.java** - Uses `pages()`, `page().delete()`

### Text Operations
13. **AddWatermark.java** - Uses `newParagraph()`, `pages()`
14. **ChangeFont.java** - Uses `pages()`
15. **FindAndReplace.java** - Uses `pages()`
16. **HighlightMatches.java** - Uses `pages()`
17. **MoveText.java** - Uses `pages()`
18. **RedactPhrases.java** - Uses `pages()`

### Image Operations
19. **DeleteImages.java** - Uses `pages()`
20. **ListImages.java** - Uses `pages()`
21. **MoveImage.java** - Uses `pages()`

### Simple Examples
22. **AddPage.java** - Uses `pages()`, `newParagraph()`
23. **InspectPDF.java** - Uses `pages()`
24. **MovePage.java** - Uses `pages()`

### Misc
25. **UploadLargeFile.java** - Uses `pages()`

**Total**: 25 out of 25 example files affected

---

## Recommendations

### For SDK Development Team

1. **Immediate Priority**: Implement missing methods to achieve parity with Python/TypeScript SDKs
   - `pages()` - Most critical, used in nearly every example
   - `newPage()` - Required for page manipulation examples
   - `page().delete()` - Required for page deletion examples
   - `field.fill()` - Simpler API than `edit().value().apply()`

2. **High Priority**: Form field methods
   - `field.getName()`
   - `field.getObjectType()`
   - `ObjectType.CHECK_BOX` enum value

3. **Medium Priority**: Text operations
   - `newParagraph()` builder

4. **Documentation Alignment**:
   - Update Java documentation to reflect actual SDK 0.1.3 API
   - OR release SDK 0.2.x with documented features
   - Add migration guide from legacy methods (`getPages()`) to new methods (`pages()`)

### For Example Developers

**Short-term workarounds**:
1. Replace `pages()` with `getPages()`
2. Replace `position.x()`/`y()` with `position.getX()`/`getY()`
3. Replace `field.fill(x)` with `field.edit().value(x).apply()`
4. Comment out or disable examples requiring `newPage()`, `duplicatePage()`, `newParagraph()`

**Long-term**:
- Wait for SDK 0.2.x release with documented API methods

---

## Version Compatibility Matrix

| Documentation | Python SDK | TypeScript SDK | Java SDK | Compatible? |
|--------------|------------|----------------|----------|-------------|
| 3.1 (Nov 2025) | 0.2.22 ✅ | 1.0.17 ✅ | 0.1.3 ❌ | **No** |

The Java SDK is approximately **2-3 minor versions behind** the Python and TypeScript SDKs in terms of API completeness.

---

## Conclusion

The Java SDK (v0.1.3) requires significant updates to match the documented API and achieve feature parity with Python and TypeScript SDKs. Most critically, the `pages()` method and form field convenience methods are needed for basic example compilation.

**Estimated Gap**: Java SDK is missing approximately **10 documented API methods** that prevent 100% of example files from compiling correctly.
