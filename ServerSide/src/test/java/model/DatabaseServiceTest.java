package model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DatabaseServiceTest {
    private DatabaseService databaseService;
    private MongoDatabase testDb;

    @Before
    public void setUp() {
        // Connect to the test database
        String testUri = "mongodb+srv://tjchang:dadalakun25@cluster0.rcits1a.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        String testDbName = "library_test";
        testDb = MongoDBUtil.connectToDB(testUri, testDbName);
        databaseService = DatabaseService.getInstance(testDb);
    }

    private void populateTestData(){
        testDb.getCollection("items").deleteMany(new Document());
        testDb.getCollection("users").deleteMany(new Document());
        databaseService.initializeData("src/test/resources/db/items_test.json");
    }


    @Test
    public void testFetchALlItems() {
        List<ClientItem> items = databaseService.fetchAllItems();
        assertEquals(2, items.size());
        assertEquals("Becoming", items.get(0).getTitle());
        assertEquals("Cyberpunk 2077", items.get(1).getTitle());
    }

    @Test
    public void testFetchBorrowedItems_WithBorrowedItems() {
        String userId = databaseService.login("User1", "user1");
        List<ClientItem> borrowedItems = databaseService.fetchBorrowedItems(userId);
        assertFalse("Borrowed items list should not be empty.", borrowedItems.isEmpty());
        assertTrue("Borrowed items list should contain specific item.", borrowedItems.stream().anyMatch(item -> item.getTitle().equals("Becoming")));
    }

    @Test
    public void testFetchBorrowedItems_NoBorrowedItems() {
        // Fetch borrowed items for a new user who has not borrowed anything
        databaseService.signup("NewUser", "newuser");
        String newUser = databaseService.login("NewUser", "newuser");
        List<ClientItem> borrowedItems = databaseService.fetchBorrowedItems(newUser);

        // The list should be empty
        assertTrue("Borrowed items list should be empty for a new user.", borrowedItems.isEmpty());
    }

    @Test
    public void testSignupSuccess() {
        databaseService.signup("newuser222", "password123");
        String userId = databaseService.login("newuser222", "password123");
        assertNotNull(userId);
        assertFalse(userId.contains("error"));
        populateTestData();
    }

    @Test
    public void testSignupFailureDueToEmptyInput() {
        String result = databaseService.signup("", "");
        assertEquals("Username and password cannot be empty", result);
    }

    @Test
    public void testSignupFailureDueToDuplicateUser() {
        databaseService.signup("user1", "password1");
        String result = databaseService.signup("user1", "password2");
        assertEquals("Username already taken", result);
    }

    @Test
    public void testLoginSuccess() {
        String result = databaseService.login("User1", "user1");
        assertNotEquals("User not found", result);
        assertNotEquals("Invalid password", result);
        assertEquals(24, result.length());
    }

    @Test
    public void testLoginUserNotFound() {
        String result = databaseService.login("adfadf", "dafsdf");
        assertEquals("User not found", result);
    }

    @Test
    public void testLoginInvalidPassword() {
        String result = databaseService.login("User1", "fdg");
        assertEquals("Invalid password", result);
    }

    @Test
    public void testResetPasswordSuccess() {
        String userId = databaseService.login("User1", "user1");
        assertNotNull(userId);

        boolean result = databaseService.resetPassword(userId, "user1_new");
        assertTrue(result);

        // Login with new password
        String loginResult = databaseService.login("User1", "user1_new");
        assertNotEquals("Invalid password", loginResult);
        populateTestData();
    }

    @Test
    public void testResetPasswordFailure() {
        // Use an invalid userId. This user does not exist
        String fakeUserId = new ObjectId().toString();
        boolean result = databaseService.resetPassword(fakeUserId, "newPassword");
        assertFalse(result);
    }

    @Test
    public void testBorrowItemWhenItemDoesNotExist() {
        String fakeItemId = new ObjectId().toString();
        String userId = databaseService.login("User2", "user2");
        String result = databaseService.borrowItem(fakeItemId, userId);
        assertEquals("Error: Item or user does not exist.", result);
    }

    @Test
    public void testBorrowItemWhenUserDoesNotExist() {
        String itemId = databaseService.fetchItemIdByTitle("Becoming").toString();
        String fakeUserId = new ObjectId().toString();
        String result = databaseService.borrowItem(itemId, fakeUserId);
        assertEquals("Error: Item or user does not exist.", result);
    }

    @Test
    public void testBorrowItemWhenFullyBorrowed() {
        String itemId = databaseService.fetchItemIdByTitle("Becoming").toString();
        String userId = databaseService.login("User2", "user2");
        String result = databaseService.borrowItem(itemId, userId);
        assertEquals("Error: Item is fully borrowed out.", result);
    }

    @Test
    public void testSuccessfulBorrowItem() {
        String itemId = databaseService.fetchItemIdByTitle("Cyberpunk 2077").toString();
        String userId = databaseService.login("User2", "user2");
        String result = databaseService.borrowItem(itemId, userId);
        assertEquals("Success: Item borrowed.", result);
        populateTestData();
    }

    @Test
    public void testReturnItemWhenItemOrUserDoesNotExist() {
        String fakeUserId = new ObjectId().toString();
        String fakeItemId = new ObjectId().toString();
        String result = databaseService.returnItem(fakeItemId, fakeUserId);
        assertEquals("Error: Item or user does not exist.", result);
    }

    @Test
    public void testReturnItemWhenNotBorrowed() {
        String itemId = databaseService.fetchItemIdByTitle("Becoming").toString();
        String user1 = databaseService.login("User1", "user1");
        String user3 = databaseService.login("User3", "user3");
        String result = databaseService.returnItem(itemId, user1);
        result = databaseService.returnItem(itemId, user3);
        result = databaseService.returnItem(itemId, user1);
        assertEquals("Error: No copies of the item are currently borrowed.", result);
    }

    @Test
    public void testReturnItemNotFoundInUsersCheckedOutItems() {
        String itemId = databaseService.fetchItemIdByTitle("Cyberpunk 2077").toString();
        String userId = databaseService.login("User1", "user1");
        String result = databaseService.returnItem(itemId, userId);
        assertEquals("Error: Item not found in user's checked out list.", result);
    }

    @Test
    public void testSuccessfulReturnItem() {
        String userId = databaseService.login("User1", "user1");
        String itemId = databaseService.fetchItemIdByTitle("Becoming").toString();

        String result = databaseService.returnItem(itemId, userId);
        assertEquals("Success: Item returned.", result);

        MongoCollection<Item> itemsCollection = testDb.getCollection("items", Item.class);
        MongoCollection<User> usersCollection = testDb.getCollection("users", User.class);

        // Verify that the item's borrowed count is decremented
        Item item = itemsCollection.find(Filters.eq("title", "Becoming")).first();
        assertEquals(1, item.getBorrowedCount());

        // Verify the item is removed from the user's checked out list
        User user = usersCollection.find(Filters.eq("name", "User1")).first();
        assertTrue(user.getCheckedOutItems().isEmpty());
        populateTestData();
    }
}
