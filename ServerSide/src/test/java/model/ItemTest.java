package model;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ItemTest {
    private Item item;
    private ObjectId testId;
    private String testItemType;
    private String testTitle;
    private String testAuthor;
    private int testPages;
    private String testSummary;
    private List<ObjectId> testCurrentHolders;
    private List<String> testPastHolders;
    private Date testLastCheckedOutDate;
    private int testQuantity;
    private int testBorrowedCount;

    @Before
    public void setUp() {
        item = new Item();
        testId = new ObjectId();
        testItemType = "Book";
        testTitle = "ABC";
        testAuthor = "tjchang";
        testPages = 223;
        testSummary = "This is the summary.";
        testCurrentHolders = Arrays.asList(new ObjectId(), new ObjectId());
        testPastHolders = Arrays.asList("User1", "User2");
        testLastCheckedOutDate = new Date();
        testQuantity = 2;
        testBorrowedCount = 1;

        item.setId(testId);
        item.setItemType(testItemType);
        item.setTitle(testTitle);
        item.setAuthor(testAuthor);
        item.setPages(testPages);
        item.setSummary(testSummary);
        item.setCurrentHolders(testCurrentHolders);
        item.setPastHolders(testPastHolders);
        item.setLastCheckedOutDate(testLastCheckedOutDate);
        item.setQuantity(testQuantity);
        item.setBorrowedCount(testBorrowedCount);
    }

    @Test
    public void testGetSetId() {
        assertEquals(testId, item.getId());
        ObjectId newId = new ObjectId();
        item.setId(newId);
        assertSame(newId, item.getId());
    }

    @Test
    public void testGetSetItemType() {
        assertEquals(testItemType, item.getItemType());
        item.setItemType("Game");
        assertEquals("Game", item.getItemType());
    }

    @Test
    public void testGetSetTitle() {
        assertEquals(testTitle, item.getTitle());
        item.setTitle("Java");
        assertEquals("Java", item.getTitle());
    }

    @Test
    public void testGetSetAuthor() {
        assertEquals(testAuthor, item.getAuthor());
        item.setAuthor("New Author");
        assertEquals("New Author", item.getAuthor());
    }

    @Test
    public void testGetSetPages() {
        assertEquals(testPages, item.getPages());
        item.setPages(321);
        assertEquals(321, item.getPages());
    }

    @Test
    public void testGetSetSummary() {
        assertEquals(testSummary, item.getSummary());
        item.setSummary("Detailed summary.");
        assertEquals("Detailed summary.", item.getSummary());
    }

    @Test
    public void testGetSetCurrentHolders() {
        assertEquals(testCurrentHolders, item.getCurrentHolders());
        List<ObjectId> newHolders = Arrays.asList(new ObjectId(), new ObjectId());
        item.setCurrentHolders(newHolders);
        assertEquals(newHolders, item.getCurrentHolders());
    }

    @Test
    public void testGetSetPastHolders() {
        assertEquals(testPastHolders, item.getPastHolders());
        List<String> newPastHolders = Arrays.asList("User3", "User4");
        item.setPastHolders(newPastHolders);
        assertEquals(newPastHolders, item.getPastHolders());
    }

    @Test
    public void testGetSetLastCheckedOutDate() {
        assertEquals(testLastCheckedOutDate, item.getLastCheckedOutDate());
        Date newDate = new Date();
        item.setLastCheckedOutDate(newDate);
        assertEquals(newDate, item.getLastCheckedOutDate());
    }

    @Test
    public void testGetSetQuantity() {
        assertEquals(testQuantity, item.getQuantity());
        item.setQuantity(20);
        assertEquals(20, item.getQuantity());
    }

    @Test
    public void testGetSetBorrowedCount() {
        assertEquals(testBorrowedCount, item.getBorrowedCount());
        item.setBorrowedCount(3);
        assertEquals(3, item.getBorrowedCount());
    }
}
