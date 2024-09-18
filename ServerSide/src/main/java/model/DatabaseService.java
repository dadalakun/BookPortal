package model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseService {
    private static final Logger logger = LogManager.getLogger(DatabaseService.class);
    private static DatabaseService instance;
    private final MongoDatabase database;
    private static final Gson gson = JsonUtil.createGson();

    public DatabaseService(MongoDatabase database) {
        this.database = database;
    }

    public static DatabaseService getInstance(MongoDatabase database) {
        if (instance == null) {
            synchronized (DatabaseService.class) {
                if (instance == null) {
                    instance = new DatabaseService(database);
                }
            }
        }
        return instance;
    }

    public void initializeData(String filepath) {
        try {
            clearCollection("items");
            clearCollection("users");
            loadItems(filepath);

            logger.info("Initialized data: items and users collections cleared and reloaded.");

            String userId1 = signup("User1", "user1");
            String userId2 = signup("User2", "user2");
            String userId3 = signup("User3", "user3");

            ObjectId itemId1 = fetchItemIdByTitle("Becoming");
            ObjectId itemId2 = fetchItemIdByTitle("Cyberpunk 2077");

            borrowItem(itemId1.toString(), userId1);
            borrowItem(itemId1.toString(), userId3);
            borrowItem(itemId2.toString(), userId2);
        } catch (Exception e) {
            logger.error("Could not initialize data: ", e);
        }
    }

    private void loadItems(String filePath) {
        logger.info("Start to load items...");
        MongoCollection<Item> collection = database.getCollection("items", Item.class);
        List<Item> items = loadDataFromFile(filePath, new TypeToken<List<Item>>(){}.getType());
        collection.insertMany(items);
        logger.info("Items loaded successfully");
    }

    private <T> List<T> loadDataFromFile(String filePath, Type type) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ClientItem> fetchAllItems() {
        logger.info("[fetchAllItems]");
        MongoCollection<Item> itemsCollection = database.getCollection("items", Item.class);
        List<Item> items = itemsCollection.find().into(new ArrayList<>());
        return items.stream().map(this::item2ClientItem).collect(Collectors.toList());
    }

    public List<ClientItem> fetchBorrowedItems(String userId) {
        logger.info("[fetchBorrowedItems] userId = {}", userId);
        MongoCollection<User> usersCollection = database.getCollection("users", User.class);

        User user = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();
        if (user != null && user.getCheckedOutItems() != null && !user.getCheckedOutItems().isEmpty()) {
            return user.getCheckedOutItems();
        }
        logger.info("[fetchBorrowedItems] No borrowed items for userId = {}", userId);
        return new ArrayList<>();
    }

    public ObjectId fetchItemIdByTitle(String title) {
        MongoCollection<Item> itemsCollection = database.getCollection("items", Item.class);
        Item item = itemsCollection.find(Filters.eq("title", title)).first();
        return item != null ? item.getId() : null;
    }

    private List<String> getUserNamesFromIds(List<ObjectId> userIds) {
        MongoCollection<User> usersCollection = database.getCollection("users", User.class);
        List<String> names = new ArrayList<>();
        if (userIds != null && !userIds.isEmpty()) {
            usersCollection.find(Filters.in("_id", userIds)).forEach(user -> names.add(user.getName()));
        }
        return names;
    }

    public synchronized String signup(String userName, String password) {
        logger.info("[signup] userName = {}", userName);
        if (userName == null || password == null || userName.isEmpty() || password.isEmpty()) {
            logger.error("[signup] Username and password cannot be empty");
            return "Username and password cannot be empty.";
        }

        MongoCollection<User> usersCollection = database.getCollection("users", User.class);
        // Check if username already exists
        if (usersCollection.find(Filters.eq("name", userName)).first() != null) {
            logger.error("[signup] Username already taken");
            return "Username already taken.";
        }

        // Hash the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Create a new user
        User newUser = new User();
        newUser.setName(userName);
        newUser.setHashedPassword(hashedPassword);

        usersCollection.insertOne(newUser);
        newUser = usersCollection.find(Filters.eq("name", userName)).first();
        logger.info("[signup] user created successfully: {}", newUser);

        // Return the new user's ID
        return newUser.getId().toString();
    }

    public String login(String userName, String password) {
        MongoCollection<User> usersCollection = database.getCollection("users", User.class);
        User user = usersCollection.find(Filters.eq("name", userName)).first();

        if (user == null) {
            logger.error("[login] User not found");
            return "User not found.";
        } else if (BCrypt.checkpw(password, user.getHashedPassword())) {
            // Login successful, return user ID
            logger.info("[login] Login successfully");
            return user.getId().toString();
        } else {
            logger.error("[login] Invalid password");
            return "Invalid password.";
        }
    }

    public synchronized boolean resetPassword(String userId, String newPassword) {
        logger.info("[resetPassword] Attempting to reset password for user ID {}", userId);
        MongoCollection<User> usersCollection = database.getCollection("users", User.class);

        ObjectId userObjectId = new ObjectId(userId);

        // Retrieve the user document
        User user = usersCollection.find(Filters.eq("_id", userObjectId)).first();
        if (user != null) {
            // Hash the new password
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            // Update the user's hashed password
            user.setHashedPassword(hashedPassword);

            // Replace the entire user document to reflect the new password
            usersCollection.replaceOne(Filters.eq("_id", userObjectId), user);

            logger.info("[resetPassword] Password reset successfully for user ID {}", userId);
            return true;
        } else {
            logger.info("[resetPassword] No user found with ID {}", userId);
            return false;
        }
    }

    public synchronized String borrowItem(String itemId, String userId) {
        logger.info("[borrowItem] Attempting to borrow item with ID {} for user {}", itemId, userId);
        MongoCollection<Item> itemsCollection = database.getCollection("items", Item.class);
        MongoCollection<User> usersCollection = database.getCollection("users", User.class);

        ObjectId itemObjectId = new ObjectId(itemId);
        ObjectId userObjectId = new ObjectId(userId);

        // Retrieve the item and user documents
        Item item = itemsCollection.find(Filters.eq("_id", itemObjectId)).first();
        User user = usersCollection.find(Filters.eq("_id", userObjectId)).first();

        if (item == null || user == null) {
            return "Error: Item or user does not exist.";
        }

        if (item.getQuantity() <= item.getBorrowedCount()) {
            return "Error: Item is fully borrowed out.";
        }

        // Increase the borrowed count
        int newBorrowedCount = item.getBorrowedCount() + 1;
        Date now = new Date(); // Current date and time
        // Update the item's borrowed count and current holders
        itemsCollection.updateOne(
                Filters.eq("_id", itemObjectId),
                Updates.combine(
                        Updates.set("borrowedCount", newBorrowedCount),
                        Updates.addToSet("currentHolder", userObjectId),
                        Updates.set("lastCheckedOutDate", now)
                )
        );

        // Check if user already has borrowed this item
        boolean itemFound = false;
        for (ClientItem borrowedItem : user.getCheckedOutItems()) {
            if (borrowedItem.getId().equals(itemId)) {
                // Increment the total number of this item borrowed
                borrowedItem.setTotal(borrowedItem.getTotal() + 1);
                itemFound = true;
                break;
            }
        }

        if (!itemFound) {
            ClientItem clientItem = item2ClientItem(item);
            // Set as the first copy of this item being borrowed
            clientItem.setTotal(1);
            user.getCheckedOutItems().add(clientItem);
        }

        usersCollection.replaceOne(Filters.eq("_id", userObjectId), user);
        logger.info("[borrowItem] Succeeded to borrow item with ID {} for user {}", itemId, userId);
        return "Success: Item borrowed.";
    }

    public synchronized String returnItem(String itemId, String userId) {
        logger.info("[returnItem] Attempting to return item with ID {} for user {}", itemId, userId);
        MongoCollection<Item> itemsCollection = database.getCollection("items", Item.class);
        MongoCollection<User> usersCollection = database.getCollection("users", User.class);

        ObjectId itemObjectId = new ObjectId(itemId);
        ObjectId userObjectId = new ObjectId(userId);

        // Retrieve the item and user documents
        Item item = itemsCollection.find(Filters.eq("_id", itemObjectId)).first();
        User user = usersCollection.find(Filters.eq("_id", userObjectId)).first();

        if (item == null || user == null) {
            return "Error: Item or user does not exist.";
        }

        if (item.getBorrowedCount() > 0) {
            // Decrease the borrowed count
            int newBorrowedCount = item.getBorrowedCount() - 1;

            // Remove the user from the item's current holders list
            itemsCollection.updateOne(
                    Filters.eq("_id", itemObjectId),
                    Updates.combine(
                            Updates.set("borrowedCount", newBorrowedCount),
                            Updates.pull("currentHolder", userObjectId),
                            Updates.addToSet("pastHolders", user.getName())
                    )
            );

            // Update the user's checkedOutItems with the item's id
            boolean itemFound = false;
            for (ClientItem borrowedItem : user.getCheckedOutItems()) {
                if (borrowedItem.getId().equals(itemId)) {
                    if (borrowedItem.getTotal() > 1) {
                        borrowedItem.setTotal(borrowedItem.getTotal() - 1);
                    } else {
                        // If only one copy was borrowed, remove it from the list
                        user.getCheckedOutItems().remove(borrowedItem);
                        logger.info("[returnItem] Last copy");
                    }
                    itemFound = true;
                    break;
                }
            }

            if (!itemFound) {
                logger.info("[returnItem] Item not found in user's checkedOutList");
                return "Error: Item not found in user's checked out list.";
            }

            usersCollection.replaceOne(Filters.eq("_id", userObjectId), user);
            logger.info("[returnItem] Succeeded to return item with ID {} for user {}", itemId, userId);
            return "Success: Item returned.";
        }
        logger.info("[returnItem] Failed to return item with ID {} for user {}", itemId, userId);
        return "Error: No copies of the item are currently borrowed.";
    }

    private ClientItem item2ClientItem(Item item) {
        ClientItem clientItem = new ClientItem();
        clientItem.setId(item.getId().toHexString()); // Convert ObjectId to String
        clientItem.setItemType(item.getItemType());
        clientItem.setTitle(item.getTitle());
        clientItem.setAuthor(item.getAuthor());
        clientItem.setPages(item.getPages());
        clientItem.setSummary(item.getSummary());
        clientItem.setCurrentHolders(getUserNamesFromIds(item.getCurrentHolders())); // Convert ObjectIds to user names
        clientItem.setPastHolders(item.getPastHolders());
        clientItem.setLastCheckedOutDate(item.getLastCheckedOutDate());
        clientItem.setTotal(item.getQuantity());
        clientItem.setRemained(item.getQuantity() - item.getBorrowedCount());

        return clientItem;
    }


    private void clearCollection(String collectionName) {
        logger.info("Start to clear collection = {}", collectionName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.deleteMany(new Document());
        logger.info("Cleared collection = {}", collectionName);
    }

}
