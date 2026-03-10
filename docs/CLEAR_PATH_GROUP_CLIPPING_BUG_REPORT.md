# Bug Report: `PUT /pdf/path-group/clipping/clear` cannot find a freshly created path group

## Summary

The new path-group clipping-clear endpoint fails on the server even when the target path group was created successfully in the same session just beforehand.

The request reaches the v1 controller and uses the correct 1-based `pageNumber`, but the server responds with:

`Path group not found: {groupId}`

## Environment

- Date: 2026-03-10
- API image: `ghcr.io/menschmachine/pdfdancer-api:pr-68`
- API base URL: `http://localhost:8080`
- Auth token: `PDFDANCER_API_TOKEN=42`
- Client repo: `pdfdancer-client-java`

## Reproduction

Run the Java e2e test:

```bash
PDFDANCER_BASE_URL=http://localhost:8080 \
PDFDANCER_API_TOKEN=42 \
./gradlew test --tests com.pdfdancer.client.rest.ClippingTest.clearPathGroupClippingViaPdfApi
```

The failing test is implemented in:

- `src/test/java/com/pdfdancer/client/rest/ClippingTest.java`

## Observed behavior

1. The test uploads `invisible-content-clipping-test.pdf`.
2. It creates a path group on page 1 containing `PATH_0_000004`.
3. It calls `PUT /pdf/path-group/clipping/clear` with the created `groupId` and `pageNumber=1`.
4. The server returns an error: `Path group not found: {groupId}`.

## Expected behavior

The server should clear clipping for the created path group and return `true`.

## Evidence

Relevant server log lines from the PR 68 image:

```text
PDFControllerV1 POST /pdf/path-group/create (v1) - request: CreatePathGroupRequest[pageIndex=0, pathIds=[PATH_0_000004], region=null]
PDFControllerV1 PUT /pdf/path-group/clipping/clear (v1) - request: ClearPathGroupClippingRequestV1[pageNumber=1, groupId=pathgroup-2a8a94d9-b091-420c-b5ca-fae75a06f319]
ControllerOps Error executing clearPathGroupClipping command
HttpStatusException: Path group not found: pathgroup-2a8a94d9-b091-420c-b5ca-fae75a06f319
    at ...PathGroupLookup.findGroup(...)
    at ...ClearPathGroupClippingCommand.apply(...)
```

## Notes

- This is not caused by 0-based/1-based mismatch on the client side. The client was updated to send `pageNumber=1`, and the request reaches `PDFControllerV1` correctly.
- The failure happens after the path group has already been created in the same session, which suggests a server-side lookup or persistence issue in path-group clipping clear specifically.
