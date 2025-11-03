package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;
import com.tfc.pdf.pdfdancer.api.common.model.PDFObject;
public record ModifyRequest(
        ObjectRef ref, PDFObject newObject
) {
}
