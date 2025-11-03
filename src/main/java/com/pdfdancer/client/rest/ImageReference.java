package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;

public class ImageReference extends BaseReference {
    public ImageReference(PDFDancer client, ObjectRef objectRef) {
        super(client, objectRef);
    }

    private ImageEdit edit() {
        return new ImageEdit(client, objectRef);
    }

    public static class ImageEdit {

        private final PDFDancer client;
        private final ObjectRef ref;

        public ImageEdit(PDFDancer client, ObjectRef ref) {
            this.client = client;
            this.ref = ref;
        }

    }
}
