package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;
public record ChangeFormFieldRequest(ObjectRef ref, String value) {
}
