package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PageTest extends BaseTest {

    @Test
    public void testGetAllElements() {
        PDFDancer client = createClient();

        List<ObjectRef> allElements = client.selectElements();
        assertTrue(allElements.size() > 500, "Should have more than 500 elements");

        // Verify we can iterate through all pages
        int totalPages = client.getPages().size();
        assertTrue(totalPages > 0, "Should have pages");
        assertFalse(allElements.isEmpty(), "Should have elements");
    }

    @Test
    public void testGetPages() {

        PDFDancer client = createClient();

        List<PageRef> response = client.getPages();
        assertEquals(ObjectType.PAGE, response.get(0).getType());

        assertNotNull(response);
        assertEquals(12, response.size());
    }

    @Test
    public void testGetPage() {

        PDFDancer client = createClient();

        ObjectRef page = client.getPage(3);

        assertNotNull(page);
        assertEquals(3, page.getPosition().getPageNumber());
        assertNotNull(page.getInternalId());
    }


    @Test
    public void testDeletePage() {

        PDFDancer client = createClient();
        ObjectRef pageThree = client.getPage(4);

        Boolean response = client.deletePage(pageThree);

        assertTrue(response);
        List<PageRef> newPageList = client.getPages();
        assertEquals(11, newPageList.size());
    }

    @Test
    public void addPage() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());
        PageRef pageRef = client.addPage();
        assertEquals(13, pageRef.getPosition().getPageNumber());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void addPageWithBuilder() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page().add();

        assertEquals(13, pageRef.getPosition().getPageNumber());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void addPageWithBuilderA4Portrait() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .a4()
                .portrait()
                .add();

        assertEquals(13, pageRef.getPosition().getPageNumber());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void addPageWithBuilderLetterLandscape() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .letter()
                .landscape()
                .add();

        assertEquals(13, pageRef.getPosition().getPageNumber());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void addPageWithBuilderAtIndex() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .atIndex(6)
                .a5()
                .landscape()
                .add();

        assertEquals(7, pageRef.getPosition().getPageNumber());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
        new PDFAssertions(client)
                .assertPageDimension(PageSize.A5.getWidth(), PageSize.A5.getHeight(), Orientation.LANDSCAPE, 7)
                .assertTotalNumberOfElements(0, 7);
    }

    @Test
    public void addPageWithBuilderAtNumber() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .atPage(7)
                .a5()
                .landscape()
                .add();

        assertEquals(7, pageRef.getPosition().getPageNumber());
        assertEquals(PageSize.A5, pageRef.getPageSize());
        assertEquals(Orientation.LANDSCAPE, pageRef.getOrientation());

        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());

        new PDFAssertions(client)
                .assertPageDimension(PageSize.A5.getWidth(), PageSize.A5.getHeight(), Orientation.LANDSCAPE, 7)
                .assertTotalNumberOfElements(0, 7);
    }

    @Test
    public void addPageWithBuilderCustomSize() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .customSize(400, 600)
                .landscape()
                .add();

        assertEquals(13, pageRef.getPosition().getPageNumber());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void addPageWithBuilderAllOptions() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .atPage(4)
                .pageSize(PageSize.A3)
                .orientation(Orientation.LANDSCAPE)
                .add();

        assertEquals(4, pageRef.getPosition().getPageNumber());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void movePage() throws IOException {
        PDFDancer client = createClient();

        List<TextParagraphReference> paragraphs = client.page(1).selectParagraphsStartingWith("The Complete");
        assertEquals(1, paragraphs.size());

        assertTrue(client.movePage(1, 12));
        saveTo(client, "movePage.pdf");

        List<PageRef> newPageList = client.getPages();
        assertEquals(12, newPageList.size());

        paragraphs = client.page(12).selectParagraphsStartingWith("The Complete");
        assertEquals(1, paragraphs.size());
    }

}
