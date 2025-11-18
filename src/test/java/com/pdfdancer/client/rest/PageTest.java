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

        // ObviouslyAwesome.pdf has 583 total elements across all pages (snapshot-backed API)
        int expectedTotal = 583;
        List<ObjectRef> allElements = client.selectElements();
        assertEquals(expectedTotal, allElements.size(),
                String.format("%d elements found but %d elements expected", allElements.size(), expectedTotal));

        // Verify we can iterate through all pages
        int totalPages = client.getPages().size();
        assertTrue(totalPages > 0, "Should have pages");
        assertTrue(allElements.size() > 0, "Should have elements");
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

        ObjectRef page = client.getPage(2);

        assertNotNull(page);
        assertEquals(2, page.getPosition().getPageIndex());
        assertNotNull(page.getInternalId());
    }


    @Test
    public void testDeletePage() {

        PDFDancer client = createClient();
        ObjectRef pageThree = client.getPage(3);

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
        assertEquals(12, pageRef.getPosition().getPageIndex());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void addPageWithBuilder() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page().add();

        assertEquals(12, pageRef.getPosition().getPageIndex());
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

        assertEquals(12, pageRef.getPosition().getPageIndex());
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

        assertEquals(12, pageRef.getPosition().getPageIndex());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void addPageWithBuilderAtIndex() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .atIndex(5)
                .a5()
                .landscape()
                .add();

        assertEquals(5, pageRef.getPosition().getPageIndex());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
        new PDFAssertions(client)
                .assertPageDimension(PageSize.A5.getWidth(), PageSize.A5.getHeight(), Orientation.PORTRAIT, 5)
                .assertTotalNumberOfElements(0, 5);
    }

    @Test
    public void addPageWithBuilderCustomSize() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .customSize(400, 600)
                .landscape()
                .add();

        assertEquals(12, pageRef.getPosition().getPageIndex());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void addPageWithBuilderAllOptions() throws IOException {
        PDFDancer client = createClient();
        assertEquals(12, client.getPages().size());

        PageRef pageRef = client.page()
                .atIndex(3)
                .pageSize(PageSize.A3)
                .orientation(Orientation.LANDSCAPE)
                .add();

        assertEquals(3, pageRef.getPosition().getPageIndex());
        List<PageRef> newPageList = client.getPages();
        assertEquals(13, newPageList.size());
    }

    @Test
    public void movePage() throws IOException {
        PDFDancer client = createClient();

        List<TextParagraphReference> paragraphs = client.page(0).selectParagraphsStartingWith("The Complete");
        assertEquals(1, paragraphs.size());

        assertTrue(client.movePage(0, 11));
        client.save("/tmp/movePage.client");

        List<PageRef> newPageList = client.getPages();
        assertEquals(12, newPageList.size());

        paragraphs = client.page(11).selectParagraphsStartingWith("The Complete");
        assertEquals(1, paragraphs.size());
    }

}
