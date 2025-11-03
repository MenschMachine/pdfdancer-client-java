package com.pdfdancer.client.rest.mutation;

import com.pdfdancer.client.http.HttpRequest;
import com.pdfdancer.client.http.MediaType;
import com.pdfdancer.client.http.MutableHttpRequest;
import com.pdfdancer.client.rest.PdfDancerHttpClient;
import com.pdfdancer.common.model.*;
import com.pdfdancer.common.request.*;
import com.pdfdancer.common.response.CommandResult;

/**
 * Encapsulates all mutation HTTP operations. Stateless and reusable per session.
 */
public final class ModificationService {
    private final String token;
    private final String sessionId;
    private final PdfDancerHttpClient.Blocking blocking;

    public ModificationService(String token, String sessionId, PdfDancerHttpClient.Blocking blocking) {
        this.token = token;
        this.sessionId = sessionId;
        this.blocking = blocking;
    }

    public Boolean move(ObjectRef objectRef, Position position) {
        String path = "/pdf/move";
        return blocking.retrieve(
                HttpRequest.PUT(path, new MoveRequest(objectRef, position))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Boolean.class
        );
    }

    public boolean addImage(Image image, Position position) {
        image.setPosition(position);
        return addImage(image);
    }

    public boolean addImage(Image image) {
        if (image.getPosition() == null) {
            throw new IllegalArgumentException("Image getPosition is null");
        }
        Boolean result = addObject(image);
        return Boolean.TRUE.equals(result);
    }

    public Boolean addObject(PDFObject object) {
        String path = "/pdf/add";
        MutableHttpRequest<AddRequest> request = HttpRequest.POST(path, new AddRequest(object))
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(token)
                .header("X-Session-Id", sessionId);
        return blocking.retrieve(request, Boolean.class);
    }

    public Boolean delete(ObjectRef objectRef) {
        String path = "/pdf/delete";
        return blocking.retrieve(
                HttpRequest.DELETE(path, new DeleteRequest(objectRef))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Boolean.class
        );
    }

    public Boolean deletePage(ObjectRef pageRef) {
        String path = "/pdf/page/delete";
        return blocking.retrieve(
                HttpRequest.DELETE(path, pageRef)
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Boolean.class
        );
    }

    public boolean modifyParagraph(ObjectRef ref, com.pdfdancer.common.model.text.Paragraph newParagraph) {
        String path = "/pdf/modify";
        MutableHttpRequest<ModifyRequest> request = HttpRequest.PUT(path, new ModifyRequest(ref, newParagraph))
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(token)
                .header("X-Session-Id", sessionId);
        CommandResult result = blocking.retrieve(request, CommandResult.class);
        return result.success();
    }

    public boolean modifyTextLine(ObjectRef ref, String newTextLine) {
        String path = "/pdf/text/line";
        CommandResult result = blocking.retrieve(
                HttpRequest.PUT(path, new ModifyTextRequest(ref, newTextLine))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                CommandResult.class
        );
        return result.success();
    }

    public boolean modifyParagraph(ObjectRef ref, String newText) {
        String path = "/pdf/text/paragraph";
        CommandResult result = blocking.retrieve(
                HttpRequest.PUT(path, new ModifyTextRequest(ref, newText))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                CommandResult.class
        );
        return result.success();
    }

    public Boolean changeFormField(FormFieldRef objectRef, String value) {
        String path = "/pdf/modify/formField";
        MutableHttpRequest<ChangeFormFieldRequest> request = HttpRequest.PUT(path, new ChangeFormFieldRequest(objectRef, value))
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(token)
                .header("X-Session-Id", sessionId);
        return blocking.retrieve(request, Boolean.class);
    }

    public PageRef addPage(AddPageRequest request) {
        String path = "/pdf/page/add";
        return blocking.retrieve(
                HttpRequest.POST(path, request)
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                PageRef.class
        );
    }

    public Boolean movePage(int fromPageIndex, int toPageIndex) {
        String path = "/pdf/page/move";
        return blocking.retrieve(
                HttpRequest.PUT(path, java.util.Map.of("fromPageIndex", fromPageIndex, "toPageIndex", toPageIndex))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                Boolean.class
        );
    }
}
