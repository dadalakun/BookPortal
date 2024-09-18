package controller;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.After;

import com.mongodb.client.MongoDatabase;
import model.DatabaseService;
import model.MongoDBUtil;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandlerTest {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private DatabaseService databaseService;
    private MongoDatabase testDb;

    @Before
    public void setUp() throws IOException {
        String testUri = "mongodb+srv://tjchang:dadalakun25@cluster0.rcits1a.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        String testDbName = "library_test";
        testDb = MongoDBUtil.connectToDB(testUri, testDbName);
        databaseService = DatabaseService.getInstance(testDb);

        serverSocket = new ServerSocket(0);
        serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket,
                        DatabaseService.getInstance(testDb));
                clientHandler.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    private void populateTestData(){
        testDb.getCollection("items").deleteMany(new Document());
        testDb.getCollection("users").deleteMany(new Document());
        databaseService.initializeData("src/test/resources/db/items_test.json");
    }

    @Test
    public void testFetchAllItemsCommand() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"fetchAllItems\"}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Fetched all items successfully"));
        }
    }

    @Test
    public void testFetchBorrowedItemsCommand() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"fetchBorrowedItems\", \"params\":{\"userId\":\"" + new ObjectId() + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Fetched borrowed items successfully"));
        }
    }

    @Test
    public void testLoginCommandWithExistUser() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"login\", \"params\":{\"userName\":\"" + "User1" + "\", \"password\":\"" + "user1" + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Login successful"));
        }
    }

    @Test
    public void testLoginCommandWithFakeUser() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"login\", \"params\":{\"userName\":\"" + "User666" + "\", \"password\":\"" + "user1" + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("User not found"));
        }
    }

    @Test
    public void testLoginCommandWithWrongPassword() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"login\", \"params\":{\"userName\":\"" + "User1" + "\", \"password\":\"" + "wrong" + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Invalid password"));
        }
    }

    @Test
    public void testSignupCommandSuccess() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"signup\", \"params\":{\"userName\":\"" + "User4" + "\", \"password\":\"" + "user4" + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Signup successful"));
            populateTestData();
        }
    }

    @Test
    public void testSignupCommandWithEmptyInput() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"signup\", \"params\":{\"userName\":\"" + "" + "\", \"password\":\"" + "user1" + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Username and password cannot be empty"));
        }
    }

    @Test
    public void testSignupCommandWithExistUsername() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"signup\", \"params\":{\"userName\":\"" + "User1" + "\", \"password\":\"" + "user1" + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Username already taken"));
        }
    }

    @Test
    public void testResetPasswordCommandWithFakeUserId() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            out.println("{\"command\":\"resetPassword\", \"params\":{\"userId\":\"" + new ObjectId() + "\", \"password\":\"" + "fake" + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Failed to reset password"));
        }
    }

    @Test
    public void testResetPasswordCommandSuccess() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            String userId = databaseService.login("User1", "user1");
            out.println("{\"command\":\"resetPassword\", \"params\":{\"userId\":\"" + userId + "\", \"password\":\"" + "user1_new" + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Password reset successfully"));
            populateTestData();
        }
    }

    @Test
    public void testBorrowItemCommand() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            String userId = databaseService.login("User1", "user1");
            String itemId = databaseService.fetchItemIdByTitle("Becoming").toString();
            out.println("{\"command\":\"borrowItem\", \"params\":{\"userId\":\"" + userId + "\", \"itemId\":\"" + itemId + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Error: Item is fully borrowed out."));
        }
    }

    @Test
    public void testReturnItemCommand() throws IOException {
        try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            String userId = databaseService.login("User1", "user1");
            String itemId = databaseService.fetchItemIdByTitle("Becoming").toString();
            out.println("{\"command\":\"returnItem\", \"params\":{\"userId\":\"" + userId + "\", \"itemId\":\"" + itemId + "\"}}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Success: Item returned."));
            populateTestData();
        }
    }

    @After
    public void clearUp() throws IOException {
        serverSocket.close();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
