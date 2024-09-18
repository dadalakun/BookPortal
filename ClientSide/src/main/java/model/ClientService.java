package model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClientService {
    private static final Logger logger = LogManager.getLogger(ClientService.class);
    private static final Gson gson = JsonUtil.createGson();
    private static ClientService instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String userId;

    public static ClientService getInstance() {
        if (instance == null) {
            synchronized (ClientService.class) {
                if (instance == null) {
                    instance = new ClientService();
                }
            }
        }
        return instance;
    }

    public void startConnection(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info("[startConnection] Connection established with server at {}:{}", ip, port);
        } catch (Exception e) {
            logger.error("[startConnection] Failed to establish connection with {}:{}", ip, port, e);
        }
    }

    public void login(String userName, String password, BiConsumer<Boolean, String> onLoginResult) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("userName", userName);
            params.addProperty("password", password);
            sendRequest("login", params);

            String jsonResponse = in.readLine();
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            boolean success = responseObj.get("success").getAsBoolean();
            String message = responseObj.get("message").getAsString();
            if (success) {
                this.userId = responseObj.get("data").getAsString();
                logger.info("[login] Login successful for user: {}", userName);
                onLoginResult.accept(success, "Login successful.");
            } else {
                logger.warn("[login] Login failed for user: {} - {}", userName, message);
                onLoginResult.accept(success, message);
            }
        } catch (IOException e) {
            logger.error("[login] Error during login process for user: {}", userName, e);
            onLoginResult.accept(false, "Error during login process: " + e.getMessage());
        }
    }

    public void signup(String userName, String password, BiConsumer<Boolean, String> resultCallback) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("userName", userName);
            params.addProperty("password", password);
            sendRequest("signup", params);

            String jsonResponse = in.readLine();
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            boolean success = responseObj.get("success").getAsBoolean();
            String message = responseObj.get("message").getAsString();

            if (success) {
                logger.info("[signup] Signup successful for user: {}", userName);
            } else {
                logger.warn("[signup] Signup failed for user: {} - {}", userName, message);
            }
            // Execute the callback function
            resultCallback.accept(success, message);
        } catch (IOException e) {
            logger.error("[signup] Error during signup process for user: {}", userName, e);
            resultCallback.accept(false, "Error during signup: " + e.getMessage());
        }
    }

    public void logout() {
        logger.info("Logging out user with ID: {}", userId);
        userId = null;
        logger.info("User has been logged out.");
    }

    public void resetPassword(String newPassword, Consumer<Boolean> callback) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("userId", userId);
            params.addProperty("password", newPassword);
            sendRequest("resetPassword", params);

            // Reading and parsing response
            String jsonResponse = in.readLine();
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            boolean success = responseObj.get("success").getAsBoolean();
            String message = responseObj.get("message").getAsString();

            if (success) {
                logger.info("[resetPassword] Password reset successfully.");
            } else {
                logger.warn("[resetPassword] Failed to reset password - {}", message);
            }

            callback.accept(success);
        } catch (IOException e) {
            logger.error("[resetPassword] Error sending reset password request", e);
            callback.accept(false);
        }
    }

    public List<ClientItem> fetchAllItems() {
        try {
            sendRequest("fetchAllItems", null);
            String jsonResponse = in.readLine();
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            boolean success = responseObj.get("success").getAsBoolean();
            String message = responseObj.get("message").getAsString();
            if (success) {
                Type listType = new TypeToken<List<ClientItem>>(){}.getType();
                List<ClientItem> items = gson.fromJson(responseObj.get("data"), listType);
                logger.info("[fetchAllItems] Successfully fetched all items.");
                return items;
            } else {
                logger.warn("[fetchAllItems] Failed to fetch all items: {}", message);
                return Collections.emptyList();
            }
        } catch (IOException e) {
            logger.error("[fetchAllItems] Error fetching all items", e);
            return Collections.emptyList();
        }
    }

    public List<ClientItem> fetchBorrowedItems() {
        try {
            logger.info("[fetchBorrowedItems] Fetching borrowed items for userId: {}", userId);
            JsonObject params = new JsonObject();
            params.addProperty("userId", userId);
            sendRequest("fetchBorrowedItems", params);
            String jsonResponse = in.readLine();
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            boolean success = responseObj.get("success").getAsBoolean();
            String message = responseObj.get("message").getAsString();
            if (success) {
                Type listType = new TypeToken<List<ClientItem>>(){}.getType();
                List<ClientItem> items = gson.fromJson(responseObj.get("data"), listType);
                logger.info("[fetchBorrowedItems] Successfully fetched borrowed items for userId: {}", userId);
                return items;
            } else {
                logger.warn("[fetchBorrowedItems] Failed to fetch borrowed items for userId: {}. Message: {}", userId, message);
                return Collections.emptyList();
            }
        } catch (IOException e) {
            logger.error("[fetchBorrowedItems] Error fetching borrowed items for userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    public void borrowItem(String itemId, BiConsumer<Boolean, String> callback) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("itemId", itemId);
            params.addProperty("userId", userId);
            sendRequest("borrowItem", params);

            // Reading and parsing response
            String jsonResponse = in.readLine();
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            boolean success = responseObj.get("success").getAsBoolean();
            String message = responseObj.get("message").getAsString();

            if (success) {
                logger.info("[borrowItem] Successfully borrowed item {} for user {}", itemId, userId);
            } else {
                logger.warn("[borrowItem] Failed to borrow item - {}", message);
            }
            // Execute the callback function
            callback.accept(success, message);
        } catch (IOException e) {
            logger.error("[borrowItem] Error borrowing item {} for user {}", itemId, userId, e);
            callback.accept(false, "Error borrowing item: " + e.getMessage());
        }
    }

    public void returnItem(String itemId, BiConsumer<Boolean, String> callback) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("itemId", itemId);
            params.addProperty("userId", userId);
            sendRequest("returnItem", params);

            // Reading and parsing response
            String response = in.readLine();
            JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();

            boolean success = responseObj.get("success").getAsBoolean();
            String message = responseObj.get("message").getAsString();

            if (success) {
                logger.info("[returnItem] Successfully returned item {} for user {}", itemId, userId);
            } else {
                logger.warn("[returnItem] Failed to return item - {}", message);
            }
            // Execute the callback function
            callback.accept(success, message);
        } catch (IOException e) {
            logger.error("[returnItem] Error returning item {} for user {}", itemId, userId, e);
            callback.accept(false, "Error returning item: " + e.getMessage());
        }
    }

    public void stopConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            logger.info("[stopConnection] Connection successfully closed.");
        } catch (Exception e) {
            logger.error("[stopConnection] Error when closing connection", e);
        }
    }

    private void sendRequest(String command, JsonObject params) {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("command", command);
            if (params != null) {
                request.add("params", params);
            }
            String jsonRequest = gson.toJson(request);
            out.println(jsonRequest);
            logger.debug("[sendRequest] Sent request: {}", jsonRequest);
        } catch (Exception e) {
            logger.error("[sendRequest] Error sending request command: {}", command, e);
        }

    }
}
