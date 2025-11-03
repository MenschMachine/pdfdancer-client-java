package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;

public class PathReference extends BaseReference {

    public PathReference(ObjectRef objectRef,
                         PDFDancer client) {
        super(client, objectRef);
    }

    public PathEdit edit() {
        return new PathEdit(client, objectRef);
    }

    public static class PathEdit {

        private final PDFDancer client;
        private final ObjectRef ref;

        public PathEdit(PDFDancer client, ObjectRef ref) {
            this.client = client;
            this.ref = ref;
        }
    }
}
