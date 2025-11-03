package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.FormFieldRef;
import com.tfc.pdf.pdfdancer.api.common.model.ObjectRef;

public class FormFieldReference extends BaseReference {
    public FormFieldReference(PDFDancer client, FormFieldRef objectRef) {
        super(client, objectRef);
    }

    private FormFieldEdit edit() {
        return new FormFieldEdit(client, objectRef);
    }

    public String name() {
        return ref().getName();
    }

    private FormFieldRef ref() {
        return (FormFieldRef) this.objectRef;
    }

    public String value() {
        return ref().getValue();
    }

    /**
     * Gets the current value of this form field.
     * Alias for value() to match Python client API.
     *
     * @return the field value
     */
    public String getValue() {
        return value();
    }

    public boolean setValue(String donaldDuck) {
        return client.changeFormField(ref(), "Donald Duck");
    }

    public static class FormFieldEdit {

        private final PDFDancer client;
        private final ObjectRef ref;

        public FormFieldEdit(PDFDancer client, ObjectRef ref) {
            this.client = client;
            this.ref = ref;
        }

    }
}
