package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;
public record ModifyTextRequest(
        ObjectRef ref, String newTextLine
) {
}
