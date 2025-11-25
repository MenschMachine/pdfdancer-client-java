package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Position;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FormXObjectTest extends BaseTest {

    @Override
    protected String getPdfFile() {
        return "Forms-20-30.pdf";
    }

    @Test
    public void findForms() {
        PDFDancer pdf = createClient();

        List<FormXObjectReference> forms = pdf.selectForms();
        assertEquals(8, forms.size());

        boolean allFormsAtOrigin = forms.stream().allMatch(f -> {
            Position pos = f.getPosition();
            return pos.getX() == 0.0 && pos.getY() == 0.0;
        });

        assertFalse(allFormsAtOrigin, "All forms should not be at coordinates (0,0)");

        List<FormXObjectReference> firstPage = pdf.page(5).selectForms();
        assertEquals(2, firstPage.size());
    }

    @Test
    public void deleteForm() {

        PDFDancer pdf = createClient();

        List<FormXObjectReference> forms = pdf.selectForms();
        int total = 8;
        assertEquals(total, forms.size());

        int i = 1;
        for (FormXObjectReference form : forms) {
            form.delete();
            List<FormXObjectReference> remaining = pdf.selectForms();
            assertEquals(total - i, remaining.size());
            i++;
        }

        List<FormXObjectReference> afterDelete = pdf.selectForms();
        assertEquals(0, afterDelete.size());

        byte[] pdfBytes = pdf.getFileBytes();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        pdf.save("/tmp/test.pdf");
    }

    @Test
    public void moveForm() {

        PDFDancer pdf = createClient();
        List<FormXObjectReference> forms = pdf.selectForms();
        FormXObjectReference form = forms.get(2);

        Position pos = form.getPosition();
        assertEquals(98, pos.getX().intValue());
        assertEquals(464, pos.getY().intValue());

        // Move it
        form.moveTo(50.1, 100);

        int page = pos.getPageNumber();
        List<FormXObjectReference> found = pdf.page(page)
                .selectFormsAt(50.1, 100);
        assertEquals(1, found.size());

        FormXObjectReference moved = found.get(0);
        Position newPos = moved.getPosition();
        assertEquals(50.1, newPos.getX(), 0.01);
        assertEquals(100, newPos.getY(), 0.01);
    }

    @Test
    public void findFormByPosition() {
        PDFDancer pdf = createClient();

        List<FormXObjectReference> none = pdf.page(1).selectFormsAt(0, 0);
        assertEquals(0, none.size());

        List<FormXObjectReference> found = pdf.page(5).selectFormsAt(76, 623, 1);
        assertEquals(1, found.size());
        assertEquals("FORM_000001", found.get(0).getInternalId());
    }

    @Test
    public void findSingularFormByPosition() {
        PDFDancer pdf = createClient();

        // Test finding a single form at a known position with sufficient epsilon
        Optional<FormXObjectReference> form = pdf.page(5).selectFormAt(76, 623, 1);
        assertTrue(form.isPresent(), "Should find form at known position");
        assertEquals("FORM_000001", form.get().getInternalId());

        // Test at position with no form
        Optional<FormXObjectReference> emptyResult = pdf.page(1).selectFormAt(0, 0, 1);
        assertFalse(emptyResult.isPresent(), "Should return empty Optional when no form found");
    }

    @Test
    public void addForm() throws IOException {
        // TODO B
    }

    @Test
    public void fillForm() throws IOException {
        // TODO Prio A
    }
}
