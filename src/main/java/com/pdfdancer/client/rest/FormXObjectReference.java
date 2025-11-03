package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;

public class FormXObjectReference extends BaseReference {
    public FormXObjectReference(PDFDancer client, ObjectRef objectRef) {
        super(client, objectRef);
    }

    private FormEdit edit() {
        return new FormEdit(client, objectRef);
    }

    public static class FormEdit {

        private final PDFDancer client;
        private final ObjectRef ref;

        public FormEdit(PDFDancer client, ObjectRef ref) {
            this.client = client;
            this.ref = ref;
        }

    }
}
