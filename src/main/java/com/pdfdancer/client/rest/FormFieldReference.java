package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.FormFieldRef;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.ObjectType;

public class FormFieldReference extends BaseReference {
    public FormFieldReference(PDFDancer client, FormFieldRef objectRef) {
        super(client, objectRef);
    }

    private FormFieldEdit edit() {
        return new FormFieldEdit(client, objectRef);
    }

    public String getName() {
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

    public boolean setValue(String value) {
        return client.changeFormField(ref(), value);
    }

    public boolean isCheckBox() {
        return ObjectType.CHECKBOX.equals(objectRef.getType());
    }

    public boolean isRadioButton() {
        return ObjectType.RADIO_BUTTON.equals(objectRef.getType());
    }

    public boolean isTextField() {
        return ObjectType.TEXT_FIELD.equals(objectRef.getType());
    }

    public boolean isButton() {
        return ObjectType.BUTTON.equals(objectRef.getType());
    }

    public boolean isDropdown() {
        return ObjectType.DROPDOWN.equals(objectRef.getType());
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
