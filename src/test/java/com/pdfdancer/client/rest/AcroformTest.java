package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectType;
import com.pdfdancer.common.model.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AcroformTest extends BaseTest {

    @Override
    protected String getPdfFile() {
        return "mixed-form-types.pdf";
    }

    @Test
    public void findFormFields() {
        PDFDancer pdf = createClient();

        List<FormFieldReference> fields = pdf.selectFormFields();
        assertEquals(10, fields.size());
        assertEquals(ObjectType.TEXT_FIELD, fields.get(0).type());
        assertEquals(ObjectType.CHECKBOX, fields.get(5).type());
        assertEquals(ObjectType.RADIO_BUTTON, fields.get(7).type());

        boolean allAtOrigin = fields.stream().allMatch(f -> {
            Position p = f.getPosition();
            return p.getX() == 0.0 && p.getY() == 0.0;
        });
        assertFalse(allAtOrigin, "All forms should not be at coordinates (0,0)");

        List<FormFieldReference> firstPage = pdf.page(0).selectFormFields();
        assertEquals(10, firstPage.size());

        List<FormFieldReference> byPosition =
                pdf.page(0).selectFormFieldsAt(280, 455, 1);
        assertEquals(1, byPosition.size());
        assertEquals(ObjectType.RADIO_BUTTON, byPosition.get(0).type());
        assertEquals("FORM_FIELD_000008", byPosition.get(0).getInternalId());
    }

    @Test
    public void deleteFormFields() {

        PDFDancer pdf = createClient();
        List<FormFieldReference> fields = pdf.selectFormFields();
        assertEquals(10, fields.size());

        FormFieldReference toDelete = fields.get(5);
        toDelete.delete();

        List<FormFieldReference> remaining = pdf.selectFormFields();
        assertEquals(9, remaining.size());
        for (FormFieldReference f : remaining) {
            assertNotEquals(toDelete.getInternalId(), f.getInternalId());
        }
    }

    @Test
    public void moveFormField() {
        // TODO double check coordinates
        PDFDancer pdf = createClient();
        List<FormFieldReference> fields = pdf.page(0)
                .selectFormFieldsAt(380, 455, 10);
        assertEquals(1, fields.size());

        FormFieldReference field = fields.get(0);
        assertEquals(380, field.getPosition().getX(), 0.1);
        assertEquals(455, field.getPosition().getY(), 0.1);

        field.moveTo(30, 40);

        List<FormFieldReference> oldSpot = pdf.page(0).selectFormFieldsAt(380, 455);
        assertEquals(0, oldSpot.size());

        List<FormFieldReference> newSpot = pdf.page(0).selectFormFieldsAt(30, 40);
        assertEquals(1, newSpot.size());
        assertEquals(field.getInternalId(), newSpot.get(0).getInternalId());
    }

    @Test
    public void editFormFields() {
        PDFDancer pdf = createClient();

        List<FormFieldReference> fields = pdf.selectFormFieldsByName("firstName");
        assertEquals(1, fields.size());

        FormFieldReference field = fields.get(0);
        assertEquals("firstName", field.name());
        assertNull(field.value());
        assertEquals(ObjectType.TEXT_FIELD, field.type());
        assertEquals("FORM_FIELD_000001", field.getInternalId());

        assertTrue(field.setValue("Donald Duck"));

        List<FormFieldReference> updated = pdf.selectFormFieldsByName("firstName");
        FormFieldReference updatedField = updated.get(0);
        assertEquals("firstName", updatedField.name());
        assertEquals("Donald Duck", updatedField.value());
    }
}
